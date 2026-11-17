package io.github.ryanpeng69.skgit.elements.expressions.gitcommit;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Date;
import com.github.shanebeee.skr.Registration;
import org.eclipse.jgit.revwalk.RevCommit;

public class ExprGitCommitTime extends SimplePropertyExpression<RevCommit, Date> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitCommitTime.class, Date.class, "[commit] creation (time|date)", "gitcommit")
                .name("Commit - Time")
                .description("Gets the time a commit was created.")
                .examples("send creation time of {_commit}")
                .since("1.0.0")
                .register();
    }

    @Override
    public Date convert(RevCommit commit) {
        if (commit == null) return null;
        return new Date(commit.getCommitTime() * 1000L);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<Date> getReturnType() {
        return Date.class;
    }

    @Override
    public String getPropertyName() {
        return "commit creation time";
    }
}
