package jago.parsing.visitor.generic;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.generic.GenericArgument;
import jago.domain.imports.Import;
import jago.domain.type.Type;
import jago.util.TypeResolver;

import java.util.List;

public class GenericArgumentVisitor extends JagoBaseVisitor<GenericArgument> {


    private final List<Import> imports;
    private final GenericArgumentsVisitor genericArgumentsVisitor;

    public GenericArgumentVisitor(List<Import> imports, GenericArgumentsVisitor genericArgumentsVisitor) {
        this.imports = imports;
        this.genericArgumentsVisitor = genericArgumentsVisitor;
    }

    @Override
    public GenericArgument visitGenericArgument(JagoParser.GenericArgumentContext ctx) {
        Type type = TypeResolver.getFromTypeContext(ctx.type(), imports);
        return ctx.genericArguments() == null
                ? new GenericArgument(type)
                : new GenericArgument(type, genericArgumentsVisitor.visitGenericArguments(ctx.genericArguments()));
    }
}
