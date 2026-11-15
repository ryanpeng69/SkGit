package io.github.ryanpeng69.skgit.elements.expressions.repository;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.github.shanebeee.skr.Registration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class ExprRepoGitInstance extends SimplePropertyExpression<Repository, Git> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprRepoGitInstance.class, Git.class, "git [instance]", "gitrepository")
                .name("Repository - Git Instance")
                .description("Gets the Git instance of a repository, used for executing operations like add, commit, status, and push.")
                .examples("""
                        git init
                        set {_repo} to last git repository
                        set {_git} to git of {_repo}""")
                .since("1.0")
                .register();
    }

    @Override
    public Git convert(Repository repo) {
        if (repo == null) return null;
        return new Git(repo);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<Git> getReturnType() {
        return Git.class;
    }

    @Override
    public String getPropertyName() {
        return "git instance";
    }
}
