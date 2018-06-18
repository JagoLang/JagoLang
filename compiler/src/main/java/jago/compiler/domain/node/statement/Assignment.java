package jago.compiler.domain.node.statement;

import jago.compiler.domain.node.expression.Expression;

public class Assignment implements Statement{
    private final String identifier;
    private final Expression expression;

    public Assignment(String varName, Expression expression) {
        this.identifier = varName;
        this.expression = expression;
    }

    public Assignment(VariableDeclarationStatement declarationStatement) {
        this.identifier = declarationStatement.getName();
        this.expression = declarationStatement.getExpression();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Expression getExpression() {
        return expression;
    }

}