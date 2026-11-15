package io.github.ryanpeng69.skgit.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.elements.effects.EffGitInit;
import org.bukkit.event.Event;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.Nullable;

public class ExprLastInitializedGitRepository extends SimpleExpression<Repository> {
    public static void register(Registration reg) {
        reg.newSimpleExpression(ExprLastInitializedGitRepository.class, Repository.class,
                        "last [created|initialized] [git] repo[sitory]")
                .name("Last Initialized Git Repository")
                .description("Gets the most recently initialized Git repository (from the `git init` effect).")
                .examples("""
                        git init
                        set {_repo} to last created git repository""")
                .since("1.0.0")
                .register();
    }

    @Override
    protected Repository @Nullable [] get(Event event) {
        return new Repository[]{EffGitInit.getLastRepository()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Repository> getReturnType() {
        return Repository.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "last created git repository";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}