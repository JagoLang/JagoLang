package jago.domain.node.expression.calls;

import jago.domain.node.expression.Expression;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class StaticCall extends CallableCall {

    private final Type ownerNamed;
    public StaticCall(Type ownerNamed,
                      CallableSignature signature,
                      List<Expression> arguments) {
        super(signature, arguments);
        this.ownerNamed = ownerNamed;
    }

    @Override
    public Type getOwnerType() {
       return ownerNamed;
    }
}
