package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.arthimetic.BinaryOperation;
import jago.domain.node.expression.calls.InstanceCall;
import jago.domain.node.expression.initializer.ArrayInitializer;
import jago.domain.scope.LocalScope;
import jago.exception.IllegalReferenceException;
import jago.util.OperatorResolver;
import jago.util.constants.Messages;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionVisitor extends JagoBaseVisitor<Expression> {

    private final ValueExpressionVisitor valueExpressionVisitor;
    private final LocalScope scope;

    public ExpressionVisitor(LocalScope scope) {
        this.valueExpressionVisitor = new ValueExpressionVisitor();
        this.scope = scope;
    }

    @Override
    public Expression visitValue(JagoParser.ValueContext ctx) {
        return valueExpressionVisitor.visitValue(ctx);
    }


    @Override
    public Expression visitMethodCall(JagoParser.MethodCallContext ctx) {
        CallVisitor callVisitor = new CallVisitor(scope, this);
        return callVisitor.visitMethodCall(ctx);
    }

    @Override
    public Expression visitVarReference(JagoParser.VarReferenceContext ctx) {
        LocalVariable var = scope.getLocalVariable(ctx.getText());
        if (var == null) {
            throw new IllegalReferenceException(String.format(Messages.VARIABLE_NOT_DECLARED, ctx.getText()));
        }
        return new VariableReference(var);
    }

    @Override
    public Expression visitAdditive(JagoParser.AdditiveContext ctx) {
        return createArithmetic(ctx.expression(0),
                ctx.expression(1),
                BinaryOperation.getOperation(ctx.op.getText())
        );
    }

    @Override
    public Expression visitMultiplicative(JagoParser.MultiplicativeContext ctx) {

        return createArithmetic(ctx.expression(0),
                ctx.expression(1),
                BinaryOperation.getOperation(ctx.op.getText())
        );
    }

    @Override
    public Expression visitPower(JagoParser.PowerContext ctx) {

        return createArithmetic(ctx.expression(0),
                ctx.expression(1),
                BinaryOperation.POW);
    }

    @Override
    public Expression visitArrayInitializer(JagoParser.ArrayInitializerContext ctx) {
        List<Expression> expressions = ctx.expression().stream().map(this::visit).collect(Collectors.toList());

        return new ArrayInitializer(expressions);
    }

    private Expression createArithmetic(JagoParser.ExpressionContext left,
                                        JagoParser.ExpressionContext right,
                                        BinaryOperation binaryOperation) {
        Expression leftExp = left.accept(this).used();
        Expression rightExp = right.accept(this).used();
        return new InstanceCall(leftExp, OperatorResolver.resolveBinaryOperation(leftExp.getType(), rightExp.getType(), binaryOperation), Collections.singletonList(rightExp));
    }


}
