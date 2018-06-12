package jago.domain.node.expression.call;

import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class ConstructorCall extends CallableCall {
    private final Type type;
    public ConstructorCall(CallableSignature signature, Type className, List<Argument> arguments) {
        super(signature, arguments);
        this.type = className;
    }

    public ConstructorCall(CallableSignature signature, Type className, List<Argument> arguments, List<Type> genericArguments) {
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
