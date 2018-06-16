package jago.domain.type.generic;

import jago.domain.generic.GenericParameter;
import jago.domain.type.Type;

import java.util.Objects;

public class GenericParameterType implements BindableType {


    private final GenericParameter genericParameter;

    public GenericParameterType(GenericParameter genericParameter) {
        this.genericParameter = genericParameter;
    }

    @Override
    public String getName() {
        return genericParameter.getName();
    }

    @Override
    public GenericParameter getGenericParameter() {
        return genericParameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericParameterType that = (GenericParameterType) o;
        return Objects.equals(genericParameter, that.genericParameter);
    }

    @Override
    public String toString() {
        return genericParameter.toString();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(genericParameter);
    }

    @Override
    public Type erased() {
        return genericParameter.getConstraint();
    }

    @Override
    public boolean isUnbound() {
        return true;
    }
}
