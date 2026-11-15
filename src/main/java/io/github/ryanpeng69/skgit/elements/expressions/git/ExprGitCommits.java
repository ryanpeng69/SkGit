package io.github.ryanpeng69.skgit.elements.expressions.git;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ExprGitCommits extends PropertyExpression<Git, RevCommit> {
    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitCommits.class, RevCommit.class, "[git] commit[s]", "git")
                .name("Git - Commits")
                .description("Gets the commit history of a Git repository (most recent first).")
                .examples("set {_commits::*} to commits of {_git}")
                .since("1.0.0")
                .register();
    }

    @Override
    protected RevCommit[] get(Event event, Git[] source) {
        ArrayList<RevCommit> rst = new ArrayList<>();
        for (Git git : source) {
            try {
                git.log().call().forEach(rst::add);
            } catch (Exception e) {
                SkGit.throwError(e);
            }
        }
        return rst.toArray(new RevCommit[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends RevCommit> getReturnType() {
        return RevCommit.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "commits";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Git>) expressions[0]);
        return true;
    }
}
