package jago.domain.node.expression.call;

import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public abstract class CallableCall extends Call {

    private final CallableSignature signature;

    CallableCall(CallableSignature signature, List<Argument> arguments) {
        super(arguments, signature.getReturnType());
        this.signature = signature;
    }
    CallableCall(CallableSignature signature, List<Argument> arguments, Type returnType) {
        super(arguments, returnType);
        this.signature = signature;
    }

    public String getIdentifier() {
        return signature.getName();
    }

    public CallableSignature getSignature() {
        return signature;
    }
}
