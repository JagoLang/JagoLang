package jago.domain.type.generic;

import jago.domain.generic.GenericParameter;
import jago.domain.generic.GenericsOwner;
import jago.domain.type.Type;
import jago.exception.internal.InternalException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class GenericType implements BindableType, GenericsOwner {


    private final Type type;
    private final List<Type> genericArguments;
    private final List<GenericParameter> bounds;
    private boolean isBound = false;

    public GenericType(Type type, List<Type> genericArguments, List<GenericParameter> bounds) {
        this.type = type;
        this.genericArguments = genericArguments;
        this.bounds = bounds;
    }

    @Override
    public String getName() {
        return type.getName();
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean isUnbound() {
        return !isBound;
    }

    public List<Type> getGenericArguments() {
        return genericArguments;
    }

    public List<GenericParameter> getBounds() {
        return bounds;
    }

    public GenericType bind(List<Type> genericArguments) {
        if (genericArguments.size() != this.genericArguments.size()) {
            throw new InternalException("Bounds are not the same size");
        }
        GenericType genericType = new GenericType(type, genericArguments, bounds);
        genericType.isBound = true;
        return genericType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericType that = (GenericType) o;
        return Objects.equals(type, that.type)
                && CollectionUtils.isEqualCollection(genericArguments, that.genericArguments)
                && CollectionUtils.isEqualCollection(bounds, that.bounds);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, genericArguments, bounds);
    }

    @Override
    public Type erased() {
        return type;
    }

    @Override
    public String getGenericId() {
        return type.getName();
    }
}
