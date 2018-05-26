package jago.domain.node.statement;

import jago.domain.node.expression.Expression;


public class LogStatement implements Statement {

    private final Expression expression;

    public LogStatement(Expression expression) {

        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

}
