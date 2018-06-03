package jago.domain.type;

public class ArrayType implements Type {

    private final Type componentType;

    public ArrayType(Type componentType) {
        this.componentType = componentType;
    }


    @Override
    public String getName() {
        if (componentType instanceof NumericType) {
            return ((NumericType) componentType).getArrayName();
        } else return "Array";
    }

    public Type getComponentType() {
        return componentType;
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return null;
    }
}
