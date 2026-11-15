package io.github.ryanpeng69.skgit.elements.expressions.gitcommit;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.github.shanebeee.skr.Registration;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

public class ExprGitCommitAuthor extends SimplePropertyExpression<RevCommit, PersonIdent> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitCommitAuthor.class, PersonIdent.class, "[commit] (author|creator)", "gitcommit")
                .name("Commit - Author")
                .description("Gets the creator of the commit.")
                .examples("send author of {_commit}")
                .since("1.0.0")
                .register();
    }

    @Override
    public PersonIdent convert(RevCommit commit) {
        if (commit == null) return null;
        return commit.getAuthorIdent();
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<PersonIdent> getReturnType() {
        return PersonIdent.class;
    }

    @Override
    public String getPropertyName() {
        return "commit author";
    }
}
