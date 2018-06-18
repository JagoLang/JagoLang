package jago.compiler.parsing.visitor;


import jago.antlr.JagoBaseVisitor;
import jago.antlr.JagoParser;
import jago.compiler.domain.Parameter;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.statement.BlockStatement;
import jago.compiler.domain.node.statement.ReturnStatement;
import jago.compiler.domain.node.statement.Statement;
import jago.compiler.domain.scope.CallableScope;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.CompilationUnitScope;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.parsing.visitor.expression.ExpressionVisitor;
import jago.compiler.parsing.visitor.statement.BlockStatementVisitor;

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
            ReturnStatement rs = new ReturnStatement(e);
            callableScope.addReturnStatement(rs);
            s = new BlockStatement(Collections.singletonList(rs), callableScope);
        } else {
            s = ctx.block().accept(new BlockStatementVisitor(callableScope));
        }
        if (!signature.isTypeResolved()) {
            signature.resolveReturnType(callableScope.resolveReturnType());
        }
        return s;
    }
}
