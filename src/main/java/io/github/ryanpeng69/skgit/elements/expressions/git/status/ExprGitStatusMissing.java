package io.github.ryanpeng69.skgit.elements.expressions.git.status;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ExprGitStatusMissing extends PropertyExpression<Git, String> {
    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitStatusMissing.class, String.class, "missing files", "git")
                .name("Git - Status: Missing files")
                .description("""
                        Gets files tracked by Git that no longer exist on disk.
                        To stage the removal, use `git add` or `{_git} remove and delete <files>`.""")
                .examples("send missing files of {_git}")
                .since("1.0.0")
                .register();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Git>) expressions[0]);
        return true;
    }

    @Override
    protected String[] get(Event event, Git[] source) {
        ArrayList<String> rst = new ArrayList<>();
        for (Git git : source) {
            try {
                rst.addAll(git.status().call().getMissing());
            } catch (Exception e) {
                SkGit.throwError(e);
            }
        }
        return rst.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }


    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "missing files";
    }
}
