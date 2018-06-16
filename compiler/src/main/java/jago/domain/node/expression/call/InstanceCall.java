package jago.domain.node.expression.call;

import jago.domain.node.expression.Expression;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class InstanceCall extends CallableCall {

    private final Expression ownerCalced;
    public InstanceCall(Expression ownerCalced,
                        CallableSignature signature,
                        List<Argument> arguments, Type returnType) {
        super(signature, arguments, returnType);
        this.ownerCalced = ownerCalced;
    }

    @Override
    public Type getOwnerType() {
        return ownerCalced.getType();
    }

    @Override
    public Expression getOwner() {
        return ownerCalced;
    }
}
