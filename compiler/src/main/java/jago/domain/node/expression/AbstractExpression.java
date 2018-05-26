package jago.domain.node.expression;

public abstract class AbstractExpression implements Expression {


    private boolean isUsed;

    @Override
    public boolean isUsed() {
        return isUsed;
    }

    @Override
    public void setUsed() {
        isUsed = true;
    }
}
