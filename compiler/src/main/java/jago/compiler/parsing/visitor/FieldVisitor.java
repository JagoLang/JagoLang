package jago.compiler.parsing.visitor;

import jago.antlr.JagoBaseVisitor;
import jago.compiler.domain.Field;

/**
 *
 */
public class FieldVisitor extends JagoBaseVisitor<Field> {

   /* private final Scope scope;

    public FieldVisitor(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Field visitField(@NotNull JagoParser.FieldContext ctx) {
        Type owner = scope.getClassType();
        Type type = TypeResolver.getFromTypeContext(ctx.type());
        String name = ctx.name().getText();
        return new Field(name, owner, type);
    }*/
}
