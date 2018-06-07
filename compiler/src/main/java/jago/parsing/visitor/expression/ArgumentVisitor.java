package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.AbstractExpression;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.NamedArgument;

import java.util.List;

import static java.util.stream.Collectors.*;

public class ArgumentVisitor extends JagoBaseVisitor<List<Argument>> {

    private final ExpressionVisitor expressionVisitor;

    public ArgumentVisitor(ExpressionVisitor expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public List<Argument> visitArgumentList(JagoParser.ArgumentListContext ctx) {
        List<Argument> arguments = ctx.argument().stream()
                .map(aCtx -> new Argument(aCtx.accept(expressionVisitor).used())).collect(toList());

        arguments.addAll(ctx.namedArgument().stream()
                .map(aCtx -> new NamedArgument(
                        aCtx.id().getText(),
                        aCtx.accept(expressionVisitor).used()
                )).collect(toList())
        );
        return arguments;

    }

}
