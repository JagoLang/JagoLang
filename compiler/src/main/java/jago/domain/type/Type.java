package jago.domain.type;


import jago.domain.generic.GenericParameter;

/**
 *
 */
public interface Type {

    String getName();

    default boolean isNullable() {
        return false;
    }

    default GenericParameter getGenericParameter() {
        return null;
    }

    default boolean isGeneric() {
        return false;
    }

    default Type erased() {
       return this;
    }
}