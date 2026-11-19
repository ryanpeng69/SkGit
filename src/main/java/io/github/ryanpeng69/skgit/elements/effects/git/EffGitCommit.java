package io.github.ryanpeng69.skgit.elements.effects.git;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.Nullable;

public class EffGitCommit extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitCommit.class,
                        "[git] commit %git% [with [message|msg] %-string%] [:amend]",
                        "%git% commit [with [message|msg]|-m] %-string% [:amend]")
                .name("Git - Commit")
                .description("""
                        Creates a commit locally.
                        `amend`: Replace the last commit""")
                .examples("""
                        {_git} commit "Initial commit"
                        {_git} commit "Fix bug" amend
                        {_git} push""")
                .since("1.0.0")
                .register();
    }

    private Expression<Git> git;
    private Expression<String> msg;
    private boolean amend;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.git = (Expression<Git>) expressions[0];
        this.msg = (Expression<String>) expressions[1];
        this.amend = parseResult.hasTag("amend");
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git git = this.git.getSingle(event);
        if (git == null) return;
        String msg = (this.msg == null) ? "Commit" : this.msg.getSingle(event);
        try {
            git.commit()
                    .setMessage(msg)
                    .setAmend(amend).call();
            SkGit.logAction("Committed to "+git+" with message:\n"+msg);
        } catch (Exception e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        String msg = (this.msg == null) ? "Commit" : this.msg.getSingle(event);
        StringBuilder sb = new StringBuilder("git commit ").append(msg).append(" to ").append(this.git.getSingle(event));
        if (amend) sb.append(" amend");
        return sb.toString();
    }

}
