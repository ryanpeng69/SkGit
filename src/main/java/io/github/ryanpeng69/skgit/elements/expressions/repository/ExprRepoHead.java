package io.github.ryanpeng69.skgit.elements.expressions.repository;

import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExprRepoHead extends PropertyExpression<Repository, String> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprRepoHead.class, String.class, "head [name]", "gitrepository")
                .name("Repository - HEAD")
                .description("Gets the name of the HEAD of the repository.")
                .examples("send head of {_repo}")
                .since("1.0.0")
                .register();
    }

    @Override
    protected String[] get(Event event, Repository[] source) {
        List<String> list = new ArrayList<>();
        for (Repository repo : source) {
            try {
                Ref ref = repo.findRef(Constants.HEAD);
                if (ref != null) list.add(ref.getName());
            } catch (IOException e) {
                SkGit.throwError(e);
            }
        }
        return list.toArray(new String[0]);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "head";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Repository>) expressions[0]);
        return true;
    }
}
