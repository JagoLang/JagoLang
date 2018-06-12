package jago.domain.type;

import jago.domain.generic.GenericParameter;

import java.util.Objects;

public class ArrayType implements CompositeType {

    private final Type componentType;

    private final static GenericParameter GENERIC_PARAMETER = new GenericParameter("T", 0,  NullableType.of(AnyType.INSTANCE));
    public final static ArrayType UNBOUND = new ArrayType(null);

    public ArrayType(Type componentType) {
        this.componentType = componentType;
    }

    public static ArrayType of(Type componentType) {
        return new ArrayType(componentType);
    }


    @Override
    public String getName() {
        if (componentType instanceof NumericType) {
            return ((NumericType) componentType).getArrayName();
        } else return "Array";
    }

    @Override
    public Type getComponentType() {
        return componentType;
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return null;
    }

    @Override
    public GenericParameter getGenericParameter() {
        return GENERIC_PARAMETER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(componentType, arrayType.componentType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(componentType);
    }
}