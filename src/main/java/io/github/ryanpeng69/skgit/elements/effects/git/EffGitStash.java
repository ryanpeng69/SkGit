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

public class EffGitStash extends Effect {
    public static void register(Registration reg) {
        reg.newEffect(EffGitStash.class,
                        "[git] stash %git% [:apply|:drop|save]",
                        "%git% stash [apply:(apply|restore)|drop:(drop|remove)|save]")
                .name("Git - Stash")
                .description("""
                        Temporarily save or restore uncommitted changes.
                        `stash save` or `stash`: Save current uncommitted changes to the stash stack.
                        `stash apply`: Restore the most recent stashed changes to the working directory.
                        `stash drop`: Remove the most recent stash entry from the stack.
                        """)
                .examples("""
                        {_git} stash
                        {_git} stash apply
                        {_git} stash drop
                        """)
                .since("1.0.1")
                .register();
    }

    private Expression<Git> git;
    private int mode; // 0 = stash, 1 = apply, 2 = drop

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.git = (Expression<Git>) expressions[0];
        if (parseResult.hasTag("apply")) {
            this.mode = 1;
        } else if (parseResult.hasTag("drop")) {
            this.mode = 2;
        } else {
            this.mode = 0;
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        Git gitObj = this.git.getSingle(event);
        if (gitObj == null) return;
        try {
            if (this.mode == 0) {
                // Stash current changes
                gitObj.stashCreate().call();
                SkGit.logAction("Stashed current changes on " + gitObj);
            } else if (this.mode == 1) {
                // Apply the most recent stash (stash@{0})
                gitObj.stashApply().setStashRef("stash@{0}").call();
                SkGit.logAction("Applied stashed changes on " + gitObj);
            } else {
                // Drop the most recent stash (index 0)
                gitObj.stashDrop().setStashRef(0).call();
                SkGit.logAction("Dropped stashed changes on " + gitObj);
            }
        } catch (Exception e) {
            SkGit.throwError(e);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        StringBuilder sb = new StringBuilder(this.git.getSingle(event)+" stash ");
        if (this.mode == 1) {
            sb.append("apply");
        } else if (this.mode == 2) {
            sb.append("drop");
        }
        return sb.toString();
    }
}
