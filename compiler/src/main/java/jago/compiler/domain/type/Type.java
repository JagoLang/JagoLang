package jago.compiler.domain.type;


import jago.compiler.domain.generic.GenericParameter;

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