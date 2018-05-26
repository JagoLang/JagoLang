package jago.domain.node.statement;

import jago.domain.node.expression.Expression;
import jago.domain.type.Type;

public class ReturnStatement implements Statement {
    private final Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public Type getType() {
        return expression.getType();
    }
}
