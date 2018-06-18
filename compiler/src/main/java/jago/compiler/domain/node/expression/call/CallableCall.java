package jago.compiler.domain.node.expression.call;

import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.type.Type;

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
