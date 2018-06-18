package jago.compiler.domain.node.statement;

import jago.compiler.domain.node.expression.call.CallableCall;

public class CallableCallStatement implements Statement {


    private final CallableCall callableCall;

    public CallableCallStatement(CallableCall callableCall) {
        this.callableCall = callableCall;
    }

    public CallableCall getCallableCall() {
        return callableCall;
    }
}
