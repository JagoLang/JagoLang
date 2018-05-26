package jago.parsing.visitor.statement;

import com.google.common.collect.Maps;
import jago.JagoBaseVisitor;
import jago.domain.node.statement.BlockStatement;
import jago.domain.node.statement.Statement;
import jago.domain.scope.CallableScope;
import jago.domain.scope.LocalScope;
import jago.exception.NotAStatementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static jago.JagoParser.BlockContext;
import static jago.JagoParser.StatementContext;

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
    public BlockStatement visitBlock(BlockContext ctx) {
        List<StatementContext> statementCtx = ctx.statement();
        LocalScope scope;
        if (callableScope == null) {
            scope = LocalScope.fromParent(enclosingScope);
        } else {
            scope = callableScope;
        }


        StatementVisitor statementVisitor = new StatementVisitor(scope);
        List<Statement> statements = statementCtx.stream()
                .map(sc -> sc.accept(statementVisitor))
                .collect(Collectors.toList());

        List<Integer> faultyStatements = IntStream.range(0, statements.size())
                .boxed()
                .map(i -> Maps.immutableEntry(i, statements.get(i)))
                .filter(e -> e.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (faultyStatements.isEmpty()) {
            return new BlockStatement(statements, scope);
        }

        List<Integer> lines = faultyStatements.stream().map(i -> statementCtx.get(i).getStart().getLine()).collect(Collectors.toList());
        throw new NotAStatementException(lines);
    }
}
