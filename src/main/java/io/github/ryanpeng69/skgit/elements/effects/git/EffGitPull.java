package io.github.ryanpeng69.skgit.elements.effects.git;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import io.github.ryanpeng69.skgit.SkGitCredentialsProvider;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EffGitPull extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitPull.class,
                        "%git% pull [(:rebase|strategy:[with strategy] :(ours|theirs))]",
                        "git pull %git% [(:rebase|strategy:[with strategy] :(ours|theirs))]",
                        "%git% pull from branch %string% [[of [the]] [remote] %-string%] [(:rebase|strategy:[with strategy] :(ours|theirs))]",
                        "git pull %git% from branch %string% [[of [the]] [remote] %-string%] [(:rebase|strategy:[with strategy] :(ours|theirs))]",
                        "%git% pull from remote %string% [(:rebase|strategy:[with strategy] :(ours|theirs))]",
                        "git pull %git% from remote %string% [(:rebase|strategy:[with strategy] :(ours|theirs))]")
                .name("Git - Pull")
                .description("""
                        Pulls new commits from a remote repository (fetch + merge).
                        Use `rebase` to replay local commits on top of fetched ones instead of merging.
                        Use `with strategy` to control conflict resolution (`ours` keeps local, `theirs` keeps remote, default `recursive` throws error).
                        **WARN**: This may cause you to lose files, always commit your work before pull.
                        **NOTE**: A remote and [Authentication](https://github.com/ryanpeng69/SkGit/wiki/Authentication) must be configured.""")
                .examples("""
                        {_git} pull
                        {_git} pull from remote "origin" rebase
                        {_git} pull from branch "main" of remote "origin" with strategy ours
                        {_git} pull theirs""")
                .since("1.0.0")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> branch;
    private Expression<String> remote;
    private Expression<String> strategy;
    private boolean rebase;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.git = (Expression<Git>) expressions[0];
        this.rebase = parseResult.hasTag("rebase");
        if (parseResult.hasTag("strategy")) {
            this.strategy = (Expression<String>) expressions[expressions.length - 1];
        }
        if (matchedPattern == 2 || matchedPattern == 3) {
            // Patterns 2, 3: pull from branch "%git% pull from branch %string% [[of [the]] [remote] %-string%]"
            this.branch = (Expression<String>) expressions[1];
            if (this.strategy == null && expressions[2] != null) this.remote = (Expression<String>) expressions[2];
        } else if (matchedPattern == 4 || matchedPattern == 5) {
            // Patterns 4, 5: pull from remote "%git% pull from remote %string%"
            this.remote = (Expression<String>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git git = this.git.getSingle(event);
        if (git == null) return;
        try {
            CredentialsProvider provider = SkGitCredentialsProvider.getProvider();
            if (provider == null) {
                SkGit.throwError("Git credentials is not set");
                return;
            }
            String remoteName;
            if (this.remote == null) {
                List<RemoteConfig> list = git.remoteList().call();
                if (list.isEmpty()) return;
                remoteName = list.getFirst().getName();
            } else {
                remoteName = this.remote.getSingle(event);
            }
            String branchName;
            if (this.branch == null) {
                List<Ref> list = git.branchList().call();
                if (list.isEmpty()) return;
                branchName = list.getFirst().getName();
            } else {
                branchName = this.branch.getSingle(event);
            }
            var pull = git.pull()
                    .setCredentialsProvider(provider)
                    .setRemote(remoteName)
                    .setRemoteBranchName(branchName);
            if (this.rebase) {
                pull.setRebase(true);
            } else if (this.strategy != null) {
                String strat = this.strategy.getSingle(event);
                if (strat != null) {
                    switch (strat.toLowerCase()) {
                        case "ours" -> pull.setStrategy(MergeStrategy.OURS);
                        case "theirs" -> pull.setStrategy(MergeStrategy.THEIRS);
                        default -> pull.setStrategy(MergeStrategy.RECURSIVE);
                    }
                }
            }
            pull.call();
            SkGit.logAction("Git Pulled from "+remoteName);
        } catch (GitAPIException e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        StringBuilder sb = new StringBuilder("git pull ");
        if (event != null) {
            sb.append(this.git.getSingle(event));
            if (this.branch != null) {
                String b = this.branch.getSingle(event);
                if (b != null) sb.append(" from branch ").append(b);
            }
            if (this.remote != null) {
                String r = this.remote.getSingle(event);
                if (r != null) sb.append(" from remote ").append(r);
            }
        } else {
            sb.append(this.git);
        }
        if (this.rebase) {
            sb.append(" rebase");
        } else if (this.strategy != null) {
            if (event != null) {
                String s = this.strategy.getSingle(event);
                if (s != null) sb.append(" with strategy ").append(s);
            } else {
                sb.append(" with strategy ").append(this.strategy);
            }
        }
        return sb.toString();
    }

}
