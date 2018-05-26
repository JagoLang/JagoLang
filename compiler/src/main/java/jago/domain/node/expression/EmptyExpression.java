package jago.domain.node.expression;

import jago.domain.type.Type;

public class EmptyExpression extends AbstractExpression{
    private final Type type;

    public EmptyExpression(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

}
