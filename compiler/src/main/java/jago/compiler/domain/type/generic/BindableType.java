package jago.compiler.domain.type.generic;

import jago.compiler.domain.type.Type;

public interface BindableType extends Type {

    boolean isUnbound();

    @Override
    default boolean isGeneric() {
        return true;
    }
}
