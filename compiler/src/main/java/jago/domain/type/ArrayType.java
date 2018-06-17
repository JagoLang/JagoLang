package jago.domain.type;

import jago.domain.generic.GenericParameter;
import jago.domain.generic.GenericsOwner;
import jago.domain.type.generic.BindableType;
import jago.domain.type.generic.GenericParameterType;
import jago.exception.internal.InternalException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArrayType implements CompositeType, BindableType, GenericsOwner {

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
        GenericParameter genericParameter = new GenericParameter("T", 0, NullableType.of(AnyType.INSTANCE));
        UNBOUND = new ArrayType(new GenericParameterType(genericParameter));
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

    @Override
    public boolean isGeneric() {
        return componentType.isGeneric();
    }

    public ArrayType bind(List<Type> typesToBind) {
        if (typesToBind.size() != 1) {
            throw new InternalException("Array is bound with one of more args");
        }
        return new ArrayType(typesToBind.get(0));
    }

    @Override
    public List<GenericParameter> getBounds() {
        return Collections.singletonList(GENERIC_PARAMETER);
    }

    @Override
    public List<Type> getGenericArguments() {
        return Collections.singletonList(componentType);
    }

    @Override
    public boolean isUnbound() {
        return this != UNBOUND;
    }
}