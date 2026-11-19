package io.github.ryanpeng69.skgit.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.bukkit.event.Event;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

public class ExprGitRepositoryAt extends SimpleExpression<Repository> {
    public static void register(Registration reg) {
        reg.newSimpleExpression(ExprGitRepositoryAt.class, Repository.class,
                        "[the] [local] [git] repo[sitory] (at|in|of) ([path] %-string%|root:root [dir|directory])")
                .name("Git Repository at Path")
                .description("Gets the Git repository at a given path (the directory containing the .git folder).")
                .examples("""
                        git init
                        set {_last} to last created git repo
                        set {_at} to git repository at root
                        send whether {_last} is {_at} # true""")
                .since("1.0.0")
                .register();
    }

    private Expression<String> path;
    private boolean isRoot;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.path = (Expression<String>) expressions[0];
        this.isRoot = parseResult.hasTag("root");
        return true;
    }

    @SuppressWarnings("resource")
    @Override
    protected Repository @Nullable [] get(Event event) {
        String path = null;
        if (!this.isRoot) path = this.path.getSingle(event);
        try {
            File repoFile = new File(Objects.requireNonNullElse(path, "")).getCanonicalFile();
            if (repoFile.isDirectory()) repoFile = new File(repoFile, ".git");
            if (!repoFile.exists()) return new Repository[0];
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(repoFile)
                    .readEnvironment()
                    .build();
            return new Repository[]{repository};
        } catch (Exception e) {
            SkGit.throwError(e);
        }
        return new Repository[0];
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
    public String toString(@Nullable Event event, boolean debug) {
        if (isRoot) return "git repository at root";
        return "git repository at path " + this.path.getSingle(event);
    }
}
