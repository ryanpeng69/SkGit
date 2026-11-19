package io.github.ryanpeng69.skgit.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import com.github.shanebeee.skr.Registration;
import io.github.ryanpeng69.skgit.SkGit;
import org.eclipse.jgit.api.Git;

public class CondGitIsClean extends PropertyCondition<Git> {

    public static void register(Registration reg) {
        reg.newPropertyCondition(CondGitIsClean.class, "clean", "git")
                .name("Git - Is Clean")
                .description("Checks whether the working tree, index, and HEAD have no differences (i.e., the repository is clean).")
                .examples("""
                        set {_git} to git of {_repo}
                        send whether {_git} is clean""")
                .since("1.0.0")
                .register();
    }

    @Override
    public boolean check(Git git) {
        try {
            return git.status().call().isClean();
        } catch (Exception e) {
            SkGit.throwError(e);
            return false;
        }
    }

    @Override
    protected String getPropertyName() {
        return "clean";
    }
}
