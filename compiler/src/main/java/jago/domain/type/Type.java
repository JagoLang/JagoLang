package jago.domain.type;


import jago.domain.Parameter;
import jago.domain.generic.GenericParameter;
import jago.domain.scope.CallableSignature;

import java.util.Collections;

/**
 *
 */
public interface Type {

    String getName();

    Class<?> getTypeClass();

    default String getDescriptor() {
        return 'L' + getInternalName() + ';';
    }

    String getInternalName();

    default boolean isNullable() {
        return false;
    }

    default CallableSignature getEqualsOperation() {
        return new CallableSignature(getName(), "equals", Collections.singletonList(
                new Parameter("that", NullableType.of(AnyType.INSTANCE))), NumericType.BOOLEAN);
    }

    default GenericParameter getGenericParameter() {
        return null;
    }
}