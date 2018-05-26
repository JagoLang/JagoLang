package jago.domain.node.expression.calls;

import jago.domain.node.expression.Expression;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;

public class InstanceCall extends CallableCall {

    private final Expression ownerCalced;
    public InstanceCall(Expression ownerCalced,
                        CallableSignature signature,
                        List<Expression> arguments) {
        super(signature, arguments);
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
