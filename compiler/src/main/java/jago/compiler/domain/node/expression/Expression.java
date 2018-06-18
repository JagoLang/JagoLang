package jago.compiler.domain.node.expression;


import jago.compiler.domain.node.Node;
import jago.compiler.domain.type.Type;

public interface Expression extends Node {
    Type getType();

    boolean isUsed();
    void setUsed();

    default Expression used() {
        setUsed();
        return this;
    }
}
