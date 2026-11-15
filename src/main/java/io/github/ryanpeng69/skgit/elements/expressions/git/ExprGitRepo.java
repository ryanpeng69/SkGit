package io.github.ryanpeng69.skgit.elements.expressions.git;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class ExprGitRepo extends SimplePropertyExpression<Git, Repository> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitRepo.class, Repository.class, "[git] repo[sitory]", "git")
                .name("Git - Repository")
                .description("Gets the repository of a Git object.")
                .examples("set {_repo} to repo of {_git}")
                .since("1.0.0")
                .register();
    }

    @Override
    public Repository convert(Git git) {
        if (git == null) return null;
        try (Repository repo = git.getRepository()) {
            return repo;
        } catch (Exception e) {
            SkGit.throwError(e);
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<Repository> getReturnType() {
        return Repository.class;
    }

    @Override
    public String getPropertyName() {
        return "git repository";
    }
}
