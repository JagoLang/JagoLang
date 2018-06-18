package jago.compiler.domain.node.statement;

import jago.compiler.domain.node.expression.Expression;

public class VariableDeclarationStatement implements Statement {

    private final String name;
    private final Expression expression;

    public VariableDeclarationStatement(String name, Expression expression) {
        this.expression = expression;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Expression getExpression() {
        return expression;
    }
}
