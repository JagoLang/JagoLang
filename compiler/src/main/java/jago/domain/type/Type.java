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

   default Class<?> getTypeClass() {
       return null;
   }

    default String getDescriptor() {
        return 'L' + getInternalName() + ';';
    }

    default String getInternalName() {
        return null;
    }

    default boolean isNullable() {
        return false;
    }

    default GenericParameter getGenericParameter() {
        return null;
    }

    default Type erased() {
       return this;
    }
}