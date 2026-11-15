package io.github.ryanpeng69.skgit.elements.effects.git;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.Nullable;

/** @see io.github.ryanpeng69.skgit.elements.expressions.repository.ExprRepoHead */
public class EffGitReset extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitReset.class,
                        "%git% reset [with mode] :(soft|mixed|hard) [and (set|change) [the] (ref|head) to %-string%]",
                        "git reset %git% [with mode] :(soft|mixed|hard) [and (set|change) [the] (ref|head) to %-string%]")
                .name("Git - Reset")
                .description("""
                        Resets HEAD and updates the staging area/working directory.
                        **Modes**:
                        - **soft**: Moves HEAD only. Keeps working directory and staging area intact.
                        - **mixed**: Moves HEAD and clears staging area. Keeps working directory safe.
                        - **hard**: Moves HEAD, clears staging, and overwrites working directory (discards all uncommitted changes).
                        """)
                .examples("""
                        {_git} reset mixed
                        {_git} reset mixed and set ref to "HEAD~3"
                        {_git} reset mixed and set ref to "origin/main"
                        {_git} reset mixed and set ref to "a1b2c3d"
                        """)
                .since("1.0.0")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> newRef;
    private ResetCommand.ResetType mode;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        git = (Expression<Git>) expressions[0];
        if (expressions.length == 2) newRef = (Expression<String>) expressions[1];
        mode = ResetCommand.ResetType.valueOf(parseResult.tags.getFirst().toUpperCase());
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git git = this.git.getSingle(event);
        if (git == null) return;
        try {
            ResetCommand cmd = git.reset().setMode(mode);
            if (newRef != null) cmd.setRef(newRef.getSingle(event));
            cmd.call();
        } catch (GitAPIException e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (newRef == null) return "git reset "+git.getSingle(event)+" with mode "+mode.name();
        return "git reset "+git.getSingle(event)+" with mode "+mode.name()+" and change ref to "+newRef.getSingle(event);
    }
}
