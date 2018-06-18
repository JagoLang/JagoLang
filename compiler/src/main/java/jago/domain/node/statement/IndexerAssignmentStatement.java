package jago.domain.node.statement;

import jago.domain.node.expression.Expression;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.CallableCall;

import java.util.List;

public class IndexerAssignmentStatement extends CallableCallStatement {

    public IndexerAssignmentStatement(CallableCall callableCall) {
        super(callableCall);
    }

    public Expression getOwner() {
        return super.getCallableCall().getOwner();
    }

    public List<Argument> getArguments() {
        return super.getCallableCall().getArguments();
    }
}
