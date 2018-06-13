package jago.domain.type;

import jago.domain.generic.GenericParameter;
import jago.domain.generic.GenericsOwner;

import java.util.Objects;

public class ArrayType implements CompositeType, GenericsOwner {

    private final Type componentType;

    private final static GenericParameter GENERIC_PARAMETER;
    public final static ArrayType UNBOUND;

    public ArrayType(Type componentType) {
        this.componentType = componentType;
    }

    public static ArrayType of(Type componentType) {
        return new ArrayType(componentType);
    }

    static {
        UNBOUND = new ArrayType(null);
        GenericParameter genericParameter = new GenericParameter("T", 0, NullableType.of(AnyType.INSTANCE));
        genericParameter.setOwner(UNBOUND);
        GENERIC_PARAMETER = genericParameter;
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

    @Override
    public String toString() {
        return "jago:Array<" + componentType.toString() + '>';
    }

    @Override
    public String getGenericId() {
        return "Array";
    }
}