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
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EffGitPush extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitPush.class,
                        "%git% (push [:force]|[:force] push)",
                        "git (push [:force]|[:force] push) %git%",
                        "%git% (push [:force]|[:force] push) from [[the] branch] %string% to [[the] branch] %string% [of [the [remote]] %-string%]",
                        "git (push [:force]|[:force] push) %git% from [[the] branch] %string% to [[the] branch] %string% [of [the [remote]] %-string%]",
                        "%git% (push [:force]|[:force] push) to remote %string%",
                        "git (push [:force]|[:force] push) %git% to remote %string%",
                        "%git% (push [:force]|[:force] push) to branch %string% [of [the [remote]] %-string%]",
                        "git (push [:force]|[:force] push) %git% to branch %string% [of [the [remote]] %-string%]")
                .name("Git - Push")
                .description("""
                        Pushes local commits to a remote repository.
                        `force`: Overwrites the remote, this is needed when you amend a commit.
                        **NOTE**: A remote and [Authentication](https://github.com/ryanpeng69/SkGit/wiki/Authentication) must be configured.""")
                .examples("""
                        add git remote named "origin" with uri "https://github.com/username/reponame.git" to remotes of {_git}
                        {_git} commit "add files"
                        {_git} push
                        {_git} commit "fix" amend
                        {_git} force push to remote "origin"
                        {_git} push from branch "master" to branch "main" of remote "origin"
                        {_git} push to branch "main" of remote "origin\"""")
                .since("1.0.0")
                .register();
    }

    private Expression<Git> git;
    private int pushMode = 0; // 0 = simple push, 1 = branch-to-branch push, 2 = same branch push
    private Expression<String> fromBranch;
    private Expression<String> toBranch;
    private Expression<String> remoteBranch;
    private Expression<String> remote;
    private boolean force;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.git = (Expression<Git>) expressions[0];
        if (matchedPattern == 0 || matchedPattern == 1) {
            // Patterns 0, 1: simple push "%git% push" / "git push %git%"
            this.pushMode = 0;
        } else if (matchedPattern == 2 || matchedPattern == 3) {
            // Patterns 2, 3: branch-to-branch push "from [[the] branch] %string% to [[the] branch] %string% [of [the [remote]] %string%]"
            this.pushMode = 1;
            this.fromBranch = (Expression<String>) expressions[1];
            this.toBranch = (Expression<String>) expressions[2];
            if (expressions[3] != null) this.remote = (Expression<String>) expressions[3];
        } else if (matchedPattern == 4 || matchedPattern == 5) {
            // Patterns 4, 5: push to remote "%git% push to remote %string%"
            this.pushMode = 0;
            this.remote = (Expression<String>) expressions[1];
        } else if (matchedPattern == 6 || matchedPattern == 7) {
            // Patterns 6, 7: push to branch "%git% push to branch %string% [of [the [remote]] %string%]"
            // Pushes local HEAD to the specified remote branch
            this.pushMode = 2;
            this.remoteBranch = (Expression<String>) expressions[1];
            if (expressions[2] != null) this.remote = (Expression<String>) expressions[2];
        }
        this.force = parseResult.hasTag("force");
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git git = this.git.getSingle(event);
        if (git == null) return;
        CredentialsProvider provider = SkGitCredentialsProvider.getProvider();
        if (provider == null) {
            SkGit.throwError("Git credentials is not set");
            return;
        }
        try {
            String remoteName;
            if (this.remote == null) {
                List<RemoteConfig> list = git.remoteList().call();
                if (list.isEmpty()) return;
                remoteName = list.getFirst().getName();
            } else {
                remoteName = this.remote.getSingle(event);
            }
            PushCommand cmd = git.push()
                    .setCredentialsProvider(provider)
                    .setRemote(remoteName)
                    .setForce(force);
            if (pushMode == 1) {
                // Branch-to-branch push: fromBranch:toBranch
                String from = this.fromBranch.getSingle(event);
                String to = this.toBranch.getSingle(event);
                if (from == null || to == null || from.isEmpty() || to.isEmpty()) {
                    SkGit.throwError("Please set both from and to branch.");
                    return;
                }
                String fromRef = from.startsWith("refs/") ? from : "refs/heads/" + from;
                String toRef = to.startsWith("refs/") ? to : "refs/heads/" + to;
                try {
                    var resolved = git.getRepository().resolve(fromRef);
                    if (resolved != null && !resolved.equals(ObjectId.zeroId())) {
                        cmd.setRefSpecs(new RefSpec(fromRef + ":" + toRef));
                    } else {
                        SkGit.throwError("Source branch '" + from + "' does not exist locally. Commit first before pushing.");
                        return;
                    }
                } catch (Exception e) {
                    SkGit.throwError("Source branch '" + from + "' does not exist locally. Commit first before pushing.");
                    return;
                }
            } else if (pushMode == 2) {
                // Push HEAD to remote branch: HEAD:refs/heads/<branch>
                String branch = this.remoteBranch.getSingle(event);
                if (branch == null || branch.isEmpty()) {
                    SkGit.throwError("Please set the branch to push.");
                    return;
                }
                String toRef = branch.startsWith("refs/") ? branch : "refs/heads/" + branch;
                cmd.setRefSpecs(new RefSpec("HEAD:" + toRef));
            }
            cmd.call();
            SkGit.logAction("Git Pushed to " + remoteName);
        } catch (Exception e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.git.getSingle(event)).append(" push");
        sb.append(" force ");
        if (pushMode == 0) {
            if (this.remote != null) {
                String r = this.remote.getSingle(event);
                if (r != null) sb.append("to remote ").append(r);
            }
        } else if (pushMode == 1) {
            String from = this.fromBranch.getSingle(event);
            String to = this.toBranch.getSingle(event);
            sb.append("from branch ").append(from).append(" to branch ").append(to);
            if (this.remote != null) {
                String r = this.remote.getSingle(event);
                if (r != null) sb.append(" of remote ").append(r);
            }
        } else if (pushMode == 2) {
            String branch = this.remoteBranch.getSingle(event);
            sb.append("HEAD to branch ").append(branch);
            if (this.remote != null) {
                String r = this.remote.getSingle(event);
                if (r != null) sb.append(" of remote ").append(r);
            }
        }
        return sb.toString();
    }
}
