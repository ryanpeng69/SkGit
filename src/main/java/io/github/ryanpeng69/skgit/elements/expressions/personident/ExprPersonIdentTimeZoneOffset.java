package io.github.ryanpeng69.skgit.elements.expressions.personident;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import com.github.shanebeee.skr.Registration;
import org.eclipse.jgit.lib.PersonIdent;

public class ExprPersonIdentTimeZoneOffset extends SimplePropertyExpression<PersonIdent, Timespan> {

    public static void register(Registration reg) {
        reg.newPropertyExpression(ExprPersonIdentTimeZoneOffset.class, Timespan.class, "[person] time[ ]zone offset", "personident")
                .name("Person Ident - Timezone Offset")
                .description("Get this person's declared time zone offset from UTC.")
                .examples("send timezone offset of {_author}")
                .since("1.0.0")
                .register();
    }

    @Override
    public Timespan convert(PersonIdent o) {
        if (o == null) return null;
        return new Timespan(Timespan.TimePeriod.MINUTE, o.getTimeZoneOffset());
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public String getPropertyName() {
        return "timezone offset";
    }
}
