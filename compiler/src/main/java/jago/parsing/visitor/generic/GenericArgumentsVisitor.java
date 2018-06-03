package jago.parsing.visitor.generic;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.generic.GenericArgument;
import jago.domain.imports.Import;

import java.util.List;
import java.util.stream.Collectors;

public class GenericArgumentsVisitor extends JagoBaseVisitor<List<GenericArgument>> {

    private final GenericArgumentVisitor argumentVisitor;

    public GenericArgumentsVisitor(List<Import> imports) {
        argumentVisitor = new GenericArgumentVisitor(imports, this);
    }

    @Override
    public List<GenericArgument> visitGenericArguments(JagoParser.GenericArgumentsContext ctx) {
        return ctx.genericArgument()
                .stream()
                .map(argumentVisitor::visitGenericArgument)
                .collect(Collectors.toList());
    }
}
