package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.operation.ArithmeticOperationExpression;
import jago.domain.node.expression.operation.ArithmeticOperation;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.InstanceCall;
import jago.domain.node.expression.initializer.ArrayInitializer;
import jago.domain.node.expression.operation.IndexerOperation;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.GenericCallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.Type;
import jago.exception.IllegalReferenceException;
import jago.util.OperatorResolver;
import jago.util.constants.Messages;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static jago.util.GenericsTypeChecker.bindGenericSignature;
import static jago.util.GenericsTypeChecker.bindType;

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
    public Expression visitArithmeticOperation(JagoParser.ArithmeticOperationContext ctx) {
        return createArithmetic(ctx.expression(0),
                ctx.expression(1),
                ArithmeticOperation.getOperation(ctx.op.getText())
        );
    }

    @Override
    public Expression visitPower(JagoParser.PowerContext ctx) {
        return createArithmetic(ctx.expression(0),
                ctx.expression(1),
                ArithmeticOperation.POW);
    }

    @Override
    public Expression visitArrayInitializer(JagoParser.ArrayInitializerContext ctx) {
        List<Expression> expressions = ctx.expression().stream()
                .map(this::visit)
                .map(Expression::used)
                .collect(Collectors.toList());
        return new ArrayInitializer(expressions);
    }

    @Override
    public Expression visitIndexerCall(JagoParser.IndexerCallContext ctx) {
        Expression expression = ctx.expression().accept(this);
        List<Argument> arguments = ctx.argument().stream()
                .map(aCtx -> aCtx.accept(this).used())
                .map(Argument::new)
                .collect(Collectors.toList());

        CallableSignature signature = OperatorResolver.resolveGetIndexer(
                expression.getType(),
                arguments,
                scope);
        // TODO a generic indexer can maybe have explicit arguments?
        if (signature instanceof GenericCallableSignature) {
            signature = bindGenericSignature((GenericCallableSignature) signature,
                    null, // the type must be deduced from argument
                    arguments);
        }
        Type returnType = bindType(signature, expression.getType(), signature.getReturnType());
        return new IndexerOperation(new InstanceCall(expression, signature, arguments, returnType));

    }

    private Expression createArithmetic(JagoParser.ExpressionContext left,
                                        JagoParser.ExpressionContext right,
                                        ArithmeticOperation binaryOperation) {
        Expression leftExp = left.accept(this).used();
        Expression rightExp = right.accept(this).used();
        CallableSignature signature = OperatorResolver.resolveBinaryOperation(
                leftExp.getType().erased(),
                rightExp.getType().erased(),
                binaryOperation,
                scope);
        if (signature instanceof GenericCallableSignature) {
            signature = bindGenericSignature((GenericCallableSignature) signature,
                    null, // the type must be deduced from argument
                    Collections.singletonList(new Argument(rightExp)));
        }
        Type returnType = bindType(signature, leftExp.getType(), signature.getReturnType());
        return new ArithmeticOperationExpression(leftExp,
                signature,
                Collections.singletonList(new Argument(rightExp)),
                returnType);
    }


}
