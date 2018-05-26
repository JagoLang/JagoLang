package jago.domain;

import jago.domain.node.expression.Parameter;
import jago.domain.node.statement.Statement;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.Collections;
import java.util.List;

public class Callable {
    private CallableSignature callableSignature;
    private Statement statement;


    public Callable(CallableSignature callableSignature, Statement statement) {
        this.callableSignature = callableSignature;
        this.statement = statement;
    }

    public String getName() {
        return callableSignature.getName();
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(callableSignature.getParameters());
    }

    public Type getReturnType() {
        return callableSignature.getReturnType();
    }

    public CallableSignature getCallableSignature() {
        return callableSignature;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
