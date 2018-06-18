package jago.compiler.domain.node.expression.call;

import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.type.Type;

import java.util.List;

public class StaticCall extends CallableCall {

    private final Type ownerNamed;
    public StaticCall(Type ownerNamed,
                      CallableSignature signature,
                      List<Argument> arguments, Type returnType) {
        super(signature, arguments, returnType);
        this.ownerNamed = ownerNamed;
    }

    @Override
    public Type getOwnerType() {
       return ownerNamed;
    }
}
