package jago.domain.node.expression.operation;

import jago.domain.node.expression.AbstractExpression;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.CallableCall;
import jago.domain.type.Type;

import java.util.List;

public class IndexerOperation extends AbstractExpression {


    private final CallableCall callableCall;


    public IndexerOperation(CallableCall callableCall) {
        this.callableCall = callableCall;
    }


    public Expression getOwner() {
        return callableCall.getOwner();
    }

    @Override
    public Type getType() {
        return callableCall.getType();
    }

    public CallableCall getCallableCall() {
        return callableCall;
    }

    public List<Argument> getArguments() {
        return callableCall.getArguments();
    }
}
