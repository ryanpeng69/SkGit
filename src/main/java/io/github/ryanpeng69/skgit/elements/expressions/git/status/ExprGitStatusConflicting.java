package io.github.ryanpeng69.skgit.elements.expressions.git.status;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ExprGitStatusConflicting extends PropertyExpression<Git, String> {
    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitStatusConflicting.class, String.class, "conflicting files", "git")
                .name("Git - Status: Conflicting files")
                .description("""
                        Gets files with merge conflicts.
                        Git prevents committing while conflicts exist.
                        Conflicts occur when both local and remote changes modify the same file region,
                        or when one side deletes a file while the other modifies it.""")
                .examples("send conflicting files of {_git}")
                .since("1.0")
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
                rst.addAll(git.status().call().getConflicting());
            } catch (GitAPIException e) {
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
        return "conflicting files";
    }
}
