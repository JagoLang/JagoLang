package jago.compiler.domain.generic;

import jago.compiler.domain.type.Type;

import java.util.List;

public interface GenericsOwner {


    String getGenericId();

    List<GenericParameter> getBounds();

    List<Type> getGenericArguments();
}
