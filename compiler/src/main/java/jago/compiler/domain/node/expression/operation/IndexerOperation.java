package jago.compiler.domain.node.expression.operation;

import jago.compiler.domain.node.expression.AbstractExpression;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.call.Argument;
import jago.compiler.domain.node.expression.call.CallableCall;
import jago.compiler.domain.type.Type;

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
