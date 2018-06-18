package jago.compiler.parsing.visitor.statement;

import jago.antlr.JagoBaseVisitor;
import jago.antlr.JagoParser;
import jago.compiler.domain.node.statement.BlockStatement;
import jago.compiler.domain.node.statement.Statement;
import jago.compiler.domain.scope.CallableScope;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.exception.NotAStatementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BlockStatementVisitor extends JagoBaseVisitor<BlockStatement> {

    private static final Logger LOGGER = LogManager.getLogger(BlockStatementVisitor.class);
    private final LocalScope enclosingScope;
    private final CallableScope callableScope;

    public BlockStatementVisitor(LocalScope enclosingScope) {
        this.enclosingScope = enclosingScope;
        callableScope = null;
    }

    public BlockStatementVisitor(CallableScope signature) {
        this.enclosingScope = null;
        this.callableScope = signature;
    }

    @Override
    public BlockStatement visitBlock(JagoParser.BlockContext ctx) {
        List<JagoParser.StatementContext> statementCtx = ctx.statement();
        LocalScope scope = callableScope == null ? LocalScope.fromParent(enclosingScope) : callableScope;

        StatementVisitor statementVisitor = new StatementVisitor(scope);
        List<Statement> statements = new ArrayList<>();
        for (JagoParser.StatementContext sc : statementCtx) {
            Statement accept = sc.accept(statementVisitor);
            if (accept == null) {
                throw new NotAStatementException(sc.getText() + "not a statement");
            }
            statements.add(accept);
        }
        return new BlockStatement(statements, scope);
    }
}
