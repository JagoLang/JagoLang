package jago.domain.generic;

import jago.domain.type.Type;

import java.util.List;

public class GenericArgument {

    private final Type type;
    private final List<GenericArgument> genericArgumentsList;

    public GenericArgument(Type type, List<GenericArgument> genericArgumentsList) {
        this.type = type;
        this.genericArgumentsList = genericArgumentsList;
    }
    public GenericArgument(Type type) {
        this.type = type;
        this.genericArgumentsList = null;
    }

    public Type getType() {
        return type;
    }

    public List<GenericArgument> getGenericArgumentsList() {
        return genericArgumentsList;
    }
}
