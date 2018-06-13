package jago.domain.type.generic;

import jago.domain.generic.GenericParameter;
import jago.domain.type.Type;
import jago.exception.internal.InternalException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class GenericType implements BindableType {


    private final Type type;
    private final List<Type> genericArguments;
    private final List<GenericParameter> bounds;

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
        for (int i = 0; i < genericArguments.size(); i++) {
            Type genericArgument = genericArguments.get(i);
            if (genericArgument instanceof GenericParameterType) {
                if (genericArgument.getGenericParameter().equals(bounds.get(i))) {
                    return true;
                }
            }
            if (genericArgument instanceof GenericType) {
                return ((GenericType) genericArgument).isUnbound();
            }
        }
        return false;
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
        return new GenericType(type, genericArguments, bounds);
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
    public String getInternalName() {
        return type.getInternalName();
    }

    @Override
    public Type erased() {
        return type;
    }
}
