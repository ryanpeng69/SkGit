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
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EffGitFetch extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitFetch.class,
                        "[git] fetch %git% [[from] [the] [remote] %-string%]",
                        "%git% fetch [[from] [the] [remote] %-string%]")
                .name("Git - Fetch")
                .description("""
                        Fetches new commits from a remote repository.
                        **NOTE**: A remote and [Authentication](https://github.com/ryanpeng69/SkGit/wiki/Authentication) must be configured.""")
                .examples("{_git} fetch", "{_git} fetch from remote \"origin\"")
                .since("1.0.0")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> remote;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.git = (Expression<Git>) expressions[0];
        if (expressions.length == 2) this.remote = (Expression<String>) expressions[1];
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
            git.fetch()
                    .setCredentialsProvider(provider)
                    .setRemote(remoteName)
                    .call();
            SkGit.logAction("Git Fetched from "+remoteName);
        } catch (GitAPIException e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (this.remote != null) return "git fetch "+this.git.getSingle(event)+" from remote "+this.remote.getSingle(event);
        return "git fetch "+this.git.getSingle(event);
    }

}
