package jago.compiler.domain.node.expression.call;

import jago.compiler.domain.node.expression.Expression;

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
