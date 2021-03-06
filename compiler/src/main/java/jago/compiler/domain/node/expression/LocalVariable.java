package jago.compiler.domain.node.expression;

import jago.compiler.domain.type.Type;

public class LocalVariable {
    private final String name;
    private final Type type;
    private final boolean isMutable;


    public LocalVariable(String name, Type type) {
        this.name = name;
        this.type = type;
        isMutable = false;
    }

    public LocalVariable(String name, Type type, boolean isMutable) {
        this.name = name;
        this.type = type;
        this.isMutable = isMutable;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isNullable() {
        return type.isNullable();
    }

    public boolean isMutable() {
        return isMutable;
    }


    @Override
    public String toString() {
        return "local variable" + (isMutable ? "mut " : "") + name + " " + type;
    }
}
