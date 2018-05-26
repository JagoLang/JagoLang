package jago.domain.node.expression;

import jago.domain.type.Type;

public class VariableReference extends AbstractExpression{
    private final LocalVariable variable;

    public VariableReference(LocalVariable variable) {
        this.variable = variable;
    }

    @Override
    public Type getType() {
        return variable.getType();
    }

    public LocalVariable getVariable() {
        return variable;
    }

    public String getName(){
        return variable.getName();
    }

}
