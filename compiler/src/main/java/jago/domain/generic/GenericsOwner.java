package jago.domain.generic;

import jago.domain.type.Type;

import java.util.List;

public interface GenericsOwner {


    String getGenericId();

    List<GenericParameter> getBounds();

    List<Type> getGenericArguments();
}
