package jago.domain.node.expression.call;

import jago.domain.node.expression.Expression;

public class NamedArgument extends Argument {

    private final String name;

    public NamedArgument(String name, Expression expression) {
        super(expression);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
