package jago.domain.node.expression.call;

import jago.domain.node.expression.AbstractExpression;
import jago.domain.node.expression.Expression;
import jago.domain.type.Type;

import java.util.Collections;
import java.util.List;

public abstract class Call extends AbstractExpression {


    private final List<Argument> arguments;
    private Type type;

    Call(List<Argument> arguments, Type type) {
        this.arguments = arguments;
        this.type = type;
    }

    public List<Argument> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public abstract Type getOwnerType();

    public Expression getOwner() {
        return null;
    }

    @Override
    public Type getType() {
        return type;
    }

    public Call lateBindType(Type type) {
        this.type = type;
        return this;
    }


}
