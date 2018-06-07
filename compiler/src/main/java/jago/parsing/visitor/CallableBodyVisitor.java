package jago.parsing.visitor;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.EmptyExpression;
import jago.domain.node.expression.Expression;
import jago.domain.Parameter;
import jago.domain.node.statement.BlockStatement;
import jago.domain.node.statement.ReturnStatement;
import jago.domain.node.statement.Statement;
import jago.domain.scope.CallableScope;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.UnitType;
import jago.parsing.visitor.expression.ExpressionVisitor;
import jago.parsing.visitor.statement.BlockStatementVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CallableBodyVisitor extends JagoBaseVisitor<Statement> {


    private final LocalScope enclosingScope;
    private final CallableScope callableScope;
    private final List<Parameter> parameters;
    private final CallableSignature signature;

    public CallableBodyVisitor(CompilationUnitScope compilationUnitScope, CallableSignature signature) {
        this.enclosingScope = null;
        this.signature = signature;
        callableScope = new CallableScope(compilationUnitScope, null, signature);
        parameters = signature.getParameters();
    }

    @Override
    public Statement visitCallableBody(JagoParser.CallableBodyContext ctx) {
        if (parameters != null) callableScope.addParameters(parameters);
        Statement s;
        if (ctx.block() == null) {
            Expression e = ctx.expression().accept(new ExpressionVisitor(callableScope)).used();
            if (e.getType().equals(UnitType.INSTANCE)) {
                s = new BlockStatement(Arrays.asList(e, new ReturnStatement(new EmptyExpression(UnitType.INSTANCE))), callableScope);
            } else {
                ReturnStatement rs = new ReturnStatement(e);
                callableScope.addReturnStatement(rs);
                s = new BlockStatement(Collections.singletonList(rs), callableScope);
            }
        } else {
            s = ctx.block().accept(new BlockStatementVisitor(callableScope));
        }
        if (!signature.isTypeResolved()) {
            signature.resolveReturnType(callableScope.resolveReturnType());
        }
        return s;
    }
}
