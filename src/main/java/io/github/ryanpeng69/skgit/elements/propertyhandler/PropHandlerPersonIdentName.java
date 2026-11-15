package io.github.ryanpeng69.skgit.elements.propertyhandler;

import org.eclipse.jgit.lib.PersonIdent;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public class PropHandlerPersonIdentName implements ExpressionPropertyHandler<PersonIdent, String> {

    @Override
    public String convert(PersonIdent o) {
        return o.getName();
    }

    @Override
    public @NotNull Class<String> returnType() {
        return String.class;
    }
}