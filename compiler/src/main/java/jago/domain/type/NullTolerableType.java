package jago.domain.type;

import jago.exception.internal.DoubleNullableException;

import java.util.Objects;

public class NullTolerableType implements Type {

    public NullTolerableType(Type type) {
        innerType = type;
    }

    public static NullTolerableType of(Type type) {
        if (type instanceof NullTolerableType) throw new DoubleNullableException();
        return new NullTolerableType(type);
    }
    private Type innerType;


    public Type getInnerType() {
        return innerType;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public String getName() {
        return innerType.getName();
    }

    @Override
    public Class<?> getTypeClass() {
        return innerType.getTypeClass();
    }

    @Override
    public String getInternalName() {
        return innerType.getInternalName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NullTolerableType that = (NullTolerableType) o;
        return Objects.equals(innerType, that.innerType);
    }

    @Override
    public String toString() {
        return "NullTolerable " + innerType.toString() + " !!!";
    }
}
