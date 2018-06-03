package jago.domain.type;

public class ArrayType implements Type {

    private final Type componentType;
    private final int length;

    public ArrayType(Type componentType, int length) {
        this.componentType = componentType;
        this.length = length;
    }


    @Override
    public String getName() {
        if (componentType instanceof NumericType) {
            return ((NumericType) componentType).getArrayName();
        } else return "Array";
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
