package jago.domain.type;

import java.util.List;

public class GenericType implements Type {


    private final Type type;
    private final List<Type> genericArguments;

    public GenericType(Type type, List<Type> genericArguments) {
        this.type = type;
        this.genericArguments = genericArguments;
    }

    @Override
    public String getName() {
        return type.getName();
    }

    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return type.getInternalName();
    }
}
