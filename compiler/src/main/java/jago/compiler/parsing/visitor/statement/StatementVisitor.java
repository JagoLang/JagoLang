package jago.compiler.parsing.visitor.statement;

import jago.antlr.JagoBaseVisitor;
import jago.antlr.JagoParser;
import jago.compiler.domain.node.expression.EmptyExpression;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.LocalVariable;
import jago.compiler.domain.node.expression.VariableReference;
import jago.compiler.domain.node.expression.call.Argument;
import jago.compiler.domain.node.expression.call.InstanceCall;
import jago.compiler.domain.node.statement.*;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.GenericCallableSignature;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.domain.type.NullableType;
import jago.compiler.domain.type.NumericType;
import jago.compiler.domain.type.Type;
import jago.compiler.domain.type.UnitType;
import jago.compiler.exception.IllegalReferenceException;
import jago.compiler.exception.ReturnTypeMismatchException;
import jago.compiler.exception.TypeMismatchException;
import jago.compiler.exception.VariableImmutableException;
import jago.compiler.parsing.visitor.expression.CallVisitor;
import jago.compiler.parsing.visitor.expression.ExpressionVisitor;
import jago.compiler.util.OperatorResolver;
import jago.compiler.util.constants.Messages;

import java.util.List;
import java.util.stream.Collectors;

import static jago.compiler.util.GenericsTypeChecker.bindGenericSignature;
import static jago.compiler.util.GenericsTypeChecker.bindType;

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
