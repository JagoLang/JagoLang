package jago.domain.node.expression;


import jago.domain.node.Node;
import jago.domain.type.Type;

public interface Expression extends Node {
    Type getType();

    boolean isUsed();
    void setUsed();

    default Expression used() {
        setUsed();
        return this;
    }
}
