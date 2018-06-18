package jago.compiler.domain.node.expression;

import jago.compiler.domain.type.Type;

public class ValueExpression extends AbstractExpression {

    private final String value;
    private final Type type;


    public ValueExpression(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }
}
