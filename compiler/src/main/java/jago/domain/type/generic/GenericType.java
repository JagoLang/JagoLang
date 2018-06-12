package jago.domain.type.generic;

import jago.domain.generic.GenericParameter;
import jago.domain.type.Type;
import jago.exception.internal.InternalException;

import java.util.List;

public class GenericType implements Type {


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

    public boolean isUnbound() {
        for (Type genericArgument : genericArguments) {
            if (genericArgument instanceof GenericParameterType) {
                return true;
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
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return type.getInternalName();
    }
}
