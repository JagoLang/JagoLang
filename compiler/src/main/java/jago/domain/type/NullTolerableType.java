package jago.domain.type;

import jago.exception.internal.DoubleNullableException;

import java.util.Objects;

public class NullTolerableType implements CompositeType {

    private Type innerType;

    private NullTolerableType(Type type) {
        innerType = type;
    }

    public static NullTolerableType of(Type type) {
        if (type instanceof NullTolerableType) throw new DoubleNullableException();
        return new NullTolerableType(type);
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

    @Override
    public Type getComponentType() {
        return innerType;
    }
}
