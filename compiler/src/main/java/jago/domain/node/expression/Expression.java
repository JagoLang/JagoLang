package jago.domain.node.expression;


import jago.domain.node.statement.Statement;
import jago.domain.type.Type;


public interface Expression extends Statement {
    Type getType();

    boolean isUsed();
    void setUsed();

    default Expression used() {
        setUsed();
        return this;
    }
}
