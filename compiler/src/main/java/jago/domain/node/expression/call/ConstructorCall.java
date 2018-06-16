package jago.domain.node.expression.call;

import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class ConstructorCall extends CallableCall {
    private final Type ownerType;
    public ConstructorCall(CallableSignature signature, Type typeToCtor, List<Argument> arguments) {
        super(signature, arguments, typeToCtor);
        this.ownerType = typeToCtor;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    public String getIdentifier() {
        return ownerType.getName();
    }

}
