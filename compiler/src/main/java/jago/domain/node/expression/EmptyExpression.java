package jago.domain.node.expression;

import jago.domain.type.Type;
import jago.domain.type.UnitType;

public class EmptyExpression extends AbstractExpression {
    private final Type type;

    public EmptyExpression(Type type) {
        this.type = type;
    }

    public static EmptyExpression unit() {
        return new EmptyExpression(UnitType.INSTANCE);
    }

    @Override
    public Type getType() {
        return type;
    }

}
