package jago.parsing.visitor.statement;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.EmptyExpression;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.InstanceCall;
import jago.domain.node.statement.*;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.GenericCallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.NullableType;
import jago.domain.type.NumericType;
import jago.domain.type.Type;
import jago.domain.type.UnitType;
import jago.exception.IllegalReferenceException;
import jago.exception.ReturnTypeMismatchException;
import jago.exception.TypeMismatchException;
import jago.exception.VariableImmutableException;
import jago.parsing.visitor.expression.CallVisitor;
import jago.parsing.visitor.expression.ExpressionVisitor;
import jago.util.OperatorResolver;
import jago.util.constants.Messages;

import java.util.List;
import java.util.stream.Collectors;

import static jago.util.GenericsTypeChecker.bindGenericSignature;
import static jago.util.GenericsTypeChecker.bindType;

public class StatementVisitor extends JagoBaseVisitor<Statement> {


    private final BlockStatementVisitor blockStatementVisitor;
    private final VariableDeclarationStatementVisitor variableDeclarationStatementVisitor;
    private final ExpressionVisitor expressionVisitor;
    private final LocalScope localScope;

    public StatementVisitor(LocalScope scope) {
        this.localScope = scope;
        expressionVisitor = new ExpressionVisitor(scope);
        blockStatementVisitor = new BlockStatementVisitor(scope);
        variableDeclarationStatementVisitor = new VariableDeclarationStatementVisitor(expressionVisitor, scope);
    }

    @Override
    public Statement visitReturnVoid(JagoParser.ReturnVoidContext ctx) {
        ReturnStatement returnStatement = new ReturnStatement(new EmptyExpression(UnitType.INSTANCE));
        localScope.getCallableScope().addReturnStatement(returnStatement);
        return returnStatement;
    }

    @Override
    public Statement visitReturnWithValue(JagoParser.ReturnWithValueContext ctx) {
        Expression expression = ctx.expression().accept(expressionVisitor).used();
        if (localScope.getCallable().isTypeResolved()) {
            if (expression.getType() instanceof NumericType) {
                if (!expression.getType().equals(localScope.getCallable().getReturnType())) {
                    throw new ReturnTypeMismatchException(Messages.PRIMITIVE_RETURN_ERROR, localScope, expression);
                }
            }
        }
        ReturnStatement returnStatement = new ReturnStatement(expression);
        localScope.getCallableScope().addReturnStatement(returnStatement);
        return returnStatement;
    }

    @Override
    public Statement visitAssignment(JagoParser.AssignmentContext ctx) {
        LocalVariable lv = localScope.getLocalVariable(ctx.id().getText());
        if (lv == null) {
            throw new IllegalReferenceException(String.format(Messages.VARIABLE_NOT_DECLARED, ctx.getText()));
        }
        if (!lv.isMutable()) {
            throw new VariableImmutableException(lv);
        }
        Expression expr = ctx.expression().accept(expressionVisitor).used();
        if (!expr.getType().equals(lv.getType()) && !NullableType.isNullableOf(lv.getType(), expr.getType())) {
            throw new TypeMismatchException();
        }
        return new Assignment(lv.getName(), expr);
    }

    @Override
    public Statement visitIndexerAssignment(JagoParser.IndexerAssignmentContext ctx) {
        String lvName = ctx.id().getText();
        LocalVariable lv = localScope.getLocalVariable(lvName);
        if (lv == null) {
            throw new IllegalReferenceException(String.format(Messages.VARIABLE_NOT_DECLARED, lvName));
        }
        List<Argument> arguments = ctx.argument().stream()
                .map(aCtx -> aCtx.accept(expressionVisitor).used())
                .map(Argument::new)
                .collect(Collectors.toList());
        arguments.add(new Argument(ctx.expression().accept(expressionVisitor).used()));
        CallableSignature signature = OperatorResolver.resolveSetIndexer(lv.getType(), arguments, localScope);
        // TODO a generic indexer can maybe have explicit arguments?
        if (signature instanceof GenericCallableSignature) {
            signature = bindGenericSignature((GenericCallableSignature) signature,
                    null, // the types must be deduced from argument
                    arguments);
        }
        Type returnType = bindType(signature, lv.getType(), signature.getReturnType());
        return new CallableCallStatement(new InstanceCall(new VariableReference(lv).used(), signature, arguments, returnType));
    }

    @Override
    public Statement visitBlock(JagoParser.BlockContext ctx) {
        return blockStatementVisitor.visitBlock(ctx);
    }

    @Override
    public Statement visitVariableDeclaration(JagoParser.VariableDeclarationContext ctx) {
        return variableDeclarationStatementVisitor.visitVariableDeclaration(ctx);
    }

    @Override
    public Statement visitMethodCall(JagoParser.MethodCallContext ctx) {
        return new CallableCallStatement(new CallVisitor(localScope, expressionVisitor).visitMethodCall(ctx));
    }

    @Override
    public Statement visitLogStatement(JagoParser.LogStatementContext ctx) {
        Expression printVal = ctx.expression().accept(expressionVisitor).used();
        return new LogStatement(printVal);
    }
}
