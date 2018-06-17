package jago.domain.generic;

import jago.domain.type.AnyType;
import jago.domain.type.NullableType;
import jago.domain.type.Type;

import java.util.Objects;

public class GenericParameter {


    public static final int NON_VARIANT = 0;
    public static final int CO_VARIANT = 1;
    public static final int CONTR_VARIANT = 2;
    private final String name;
    private GenericsOwner owner;
    private final int variance;
    private final Type constraint;
    // each generic parameter of every type if different, even if they might share all other characteristics

    public GenericParameter(String name, int variance, Type constraint) {
        this.name = name;
        this.variance = variance;
        this.constraint = constraint;
    }

    public GenericParameter(String name) {
        this.name = name;
        this.variance = 0;
        this.constraint = NullableType.of(AnyType.INSTANCE);
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
        return Objects.equals(owner.getGenericId(), that.owner.getGenericId());

    }

    @Override
    public String toString() {
        return name + ": " + constraint;
    }

    public void setOwner(GenericsOwner owner) {
        if (this.owner != null) {
            throw new IllegalStateException();
        }
        this.owner = owner;

    }

    @Override
    public int hashCode() {
        return Objects.hash(name, variance, constraint);
    }
}
