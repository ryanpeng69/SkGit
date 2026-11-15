package io.github.ryanpeng69.skgit.elements.expressions.personident;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.github.shanebeee.skr.Registration;
import org.eclipse.jgit.lib.PersonIdent;

public class ExprPersonIdentEmail extends SimplePropertyExpression<PersonIdent, String> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprPersonIdentEmail.class, String.class, "[person] email", "personident")
                .name("Person Ident - Email")
                .description("Gets the email of a person.")
                .examples("send email of {_author}")
                .since("1.0.0")
                .register();
    }

    @Override
    public String convert(PersonIdent o) {
        if (o == null) return null;
        return o.getEmailAddress();
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
        return "email";
    }
}
