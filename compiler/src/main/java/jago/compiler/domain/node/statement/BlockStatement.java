package jago.compiler.domain.node.statement;

import jago.compiler.domain.scope.LocalScope;

import java.util.List;

public class BlockStatement implements Statement {

    private final List<Statement> statements;
    private final LocalScope localScope;

    public BlockStatement(List<Statement> statements, LocalScope localScope) {
        this.statements = statements;
        this.localScope = localScope;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public LocalScope getLocalScope() {
        return localScope;
    }
}
