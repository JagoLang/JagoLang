package jago.domain.generic;

import jago.domain.type.AnyType;
import jago.domain.type.Type;

import java.util.Objects;

public class GenericParameter {


    public static final int NON_VARIANT = 0;
    public static final int CO_VARIANT = 1;
    public static final int CONTR_VARIANT = 2;
    private final String name;
    private final int variance;
    private final Type constraint;

    public GenericParameter(String name, int variance, Type constraint) {
        this.name = name;
        this.variance = variance;
        this.constraint = constraint;
    }

    public GenericParameter(String name) {
        this.name = name;
        this.variance = 0;
        this.constraint = AnyType.INSTANCE;
    }

    public String getName() {
        return name;
    }

    public int getVariance() {
        return variance;
    }

    public Type getConstraint() {
        return constraint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericParameter that = (GenericParameter) o;
        return Objects.equals(name, that.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name, variance, constraint);
    }
}
