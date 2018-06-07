package jago.domain.type;

public class ArrayType implements CompositeType {

    private final Type componentType;

    public ArrayType(Type componentType) {
        this.componentType = componentType;
    }

    static ArrayType of(Type componentType) {
        return new ArrayType(componentType);
    }

    @Override
    public String getName() {
        if (componentType instanceof NumericType) {
            return ((NumericType) componentType).getArrayName();
        } else return "Array";
    }

    @Override
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
