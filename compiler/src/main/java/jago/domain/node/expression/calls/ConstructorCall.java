package jago.domain.node.expression.calls;

import jago.domain.node.expression.Expression;
import jago.domain.scope.CallableSignature;
import jago.domain.type.ClassType;
import jago.domain.type.Type;

import java.util.List;

public class ConstructorCall extends CallableCall {
    private final Type type;

    public ConstructorCall(CallableSignature signature, ClassType className, List<Expression> arguments) {
        super(signature, arguments);
        this.type = className;
    }



    @Override
    public Type getOwnerType() {
        return type;
    }

    public String getIdentifier() {
        return type.getName();
    }

    @Override
    public Type getType() {
        return type;
    }
}
