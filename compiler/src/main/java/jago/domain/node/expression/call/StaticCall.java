package jago.domain.node.expression.call;

import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class StaticCall extends CallableCall {

    private final Type ownerNamed;
    public StaticCall(Type ownerNamed,
                      CallableSignature signature,
                      List<Argument> arguments) {
        super(signature, arguments);
        this.ownerNamed = ownerNamed;
    }

    @Override
    public Type getOwnerType() {
       return ownerNamed;
    }
}
