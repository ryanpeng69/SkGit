package io.github.ryanpeng69.skgit.elements.expressions.gitcommit;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.github.shanebeee.skr.Registration;
import org.eclipse.jgit.revwalk.RevCommit;

public class ExprGitCommitMessage extends SimplePropertyExpression<RevCommit, String> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprGitCommitMessage.class, String.class, "[commit] (message|msg)", "gitcommit")
                .name("Commit - Message")
                .description("""
                        Gets the message of a commit.
                        **NOTE**: when you do `send message of {_commit}`, the syntax conflicts with `(message|send [message[s]]) %objects% [to %audiences%]`""")
                .examples("send msg of {_commit}")
                .since("1.0.0")
                .register();
    }

    @Override
    public String convert(RevCommit commit) {
        if (commit == null) return null;
        return commit.getFullMessage();
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
    public String getPropertyName() {
        return "commit message";
    }
}
