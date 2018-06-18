package jago.compiler.domain.node.expression.call;

import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.type.Type;

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
