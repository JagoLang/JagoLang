package jago.domain.node.expression.calls;

import jago.domain.node.expression.AbstractExpression;
import jago.domain.node.expression.Expression;
import jago.domain.type.Type;

import java.util.Collections;
import java.util.List;

public abstract class Call extends AbstractExpression {


    private final List<Expression> arguments;
    private final Type type;

    Call(List<Expression> arguments, Type type) {
        this.arguments = arguments;
        this.type = type;
    }

    public List<Expression> getArguments() {
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


}
