package jago.compiler.domain.node.statement;

import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.type.Type;

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
