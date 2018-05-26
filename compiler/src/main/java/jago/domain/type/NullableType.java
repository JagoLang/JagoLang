package jago.domain.type;

import java.util.Objects;

public class NullableType implements Type {

    private NullableType(Type type) {
        innerType = type;
    }


    public static NullableType of(Type type) {
        return new NullableType(type);
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

    public static boolean isNullableOf(Type t1, Type t2) {
        if (t1.equals(t2)) {
            return true;
        }
        if (t1 instanceof NullableType) {
            return ((NullableType) t1).innerType.equals(t2) || t2 instanceof NullType;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NullableType that = (NullableType) o;
        return Objects.equals(innerType, that.innerType);
    }

    @Override
    public String toString() {
        return "Nullable " + innerType.toString() + "?";
    }
}
