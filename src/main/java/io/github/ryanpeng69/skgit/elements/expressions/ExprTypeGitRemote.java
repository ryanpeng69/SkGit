package io.github.ryanpeng69.skgit.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.transport.URIish;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;

public class ExprTypeGitRemote extends SimpleExpression<ExprTypeGitRemote.TypeGitRemote> {
    public record TypeGitRemote(String name, URIish uri) {}

    public static void register(Registration reg) {
        reg.newSimpleExpression(ExprTypeGitRemote.class, TypeGitRemote.class,
                "[the|a|new|a new] [git] remote (named|with name) %string% with (uri|url|link) %string%")
                .name("Git Remote")
                .description("Creates a Git remote instance with the specified name and URI.")
                .examples("set remote of {_git} to git remote named \"origin\" with uri \"https://github.com/username/reponame.git\"")
                .since("1.0.0")
                .register();
    }

    private Expression<String> name;
    private Expression<String> uri;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.name = (Expression<String>) expressions[0];
        this.uri = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected TypeGitRemote @Nullable [] get(Event event) {
        String name = this.name.getSingle(event);
        String uri = this.uri.getSingle(event);
        if (name == null || uri == null || name.isEmpty() || uri.isEmpty()) return new TypeGitRemote[0];
        try {
            return new TypeGitRemote[]{new TypeGitRemote(name, new URIish(uri))};
        } catch (URISyntaxException e) {
            SkGit.throwError(e);
        }
        return new TypeGitRemote[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends TypeGitRemote> getReturnType() {
        return TypeGitRemote.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "git remote named "+this.name+" with uri "+this.uri;
    }

}
