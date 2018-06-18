package jago.compiler.domain.scope;

import jago.compiler.domain.Parameter;
import jago.compiler.domain.generic.GenericParameter;
import jago.compiler.domain.generic.GenericsOwner;
import jago.compiler.domain.type.Type;
import jago.compiler.exception.internal.InternalException;

import java.util.List;
import java.util.stream.Collectors;

public class GenericCallableSignature extends CallableSignature implements GenericsOwner {


    private final List<GenericParameter> bounds;
    private final List<Type> genericArguments;

    private boolean isBound = false;

    public GenericCallableSignature(Type owner,
                                    String name,
                                    List<Parameter> parameters,
                                    Type returnType,
                                    List<Type> genericArguments,
                                    List<GenericParameter> bounds) {
        super(owner, name, parameters, returnType);
        this.bounds = bounds;
        this.genericArguments = genericArguments;
    }

    public List<GenericParameter> getBounds() {
        return bounds;
    }

    public GenericCallableSignature bind(List<Type> genericArguments) {
        if (genericArguments.size() != this.genericArguments.size()) {
            throw new InternalException("Bounds are not the same size");
        }
        GenericCallableSignature genericCallableSignature = new GenericCallableSignature(getOwner(), getName(), getParameters(), getReturnType(), genericArguments, bounds);
        genericCallableSignature.isBound = true;
        return genericCallableSignature;
    }

    public boolean isUnbound() {
        return !isBound;
    }


    @Override
    public boolean hasGenericSignature() {
        return true;
    }

    @Override
    public List<Type> getGenericArguments() {
        return genericArguments;
    }



    @Override
    public String getGenericId() {
        return getOwner().getName() + "." + getName() +
                getParameters().stream()
                        .map(Parameter::getType)
                        .map(Type::getName)
                        .collect(Collectors.joining(", "));
    }
}
