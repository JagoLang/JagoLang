package jago.domain.node.expression.call;

import jago.domain.node.expression.Expression;
import jago.domain.type.Type;

public class Argument {

   private final Expression expression;

    public Argument(Expression expression) {
        this.expression = expression;
    }

    public Type getType() {
        return expression.getType();
    }

    public Expression getExpression() {
        return expression;
    }
}
