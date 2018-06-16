package jago.domain.type.generic;

import jago.domain.type.Type;

public interface BindableType extends Type {

    boolean isUnbound();

    @Override
    default boolean isGeneric() {
        return true;
    }
}
