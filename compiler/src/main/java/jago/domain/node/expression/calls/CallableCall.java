package jago.domain.node.expression.calls;

import jago.domain.node.expression.Expression;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public abstract class CallableCall extends Call {

    private final CallableSignature signature;

    CallableCall(CallableSignature signature, List<Expression> arguments) {
        super(arguments, signature.getReturnType());
        this.signature = signature;
    }


    public String getIdentifier() {
        return signature.getName();
    }

    public CallableSignature getSignature() {
        return signature;
    }

    // Base case => no real owner
    @Override
    public Type getOwnerType() {
        return null;
    }
}
