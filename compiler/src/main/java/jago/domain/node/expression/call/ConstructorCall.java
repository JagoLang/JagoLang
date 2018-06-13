package jago.domain.node.expression.call;

import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class ConstructorCall extends CallableCall {
    private final Type ownerType;
    public ConstructorCall(CallableSignature signature, Type className, List<Argument> arguments) {
        super(signature, arguments, className);
        this.ownerType = className;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    public String getIdentifier() {
        return ownerType.getName();
    }

}
