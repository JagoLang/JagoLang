package jago.compiler.domain.node.statement;

import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.call.Argument;
import jago.compiler.domain.node.expression.call.CallableCall;

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
