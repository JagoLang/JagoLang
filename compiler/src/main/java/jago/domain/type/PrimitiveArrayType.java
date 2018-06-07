package jago.domain.type;

import org.apache.commons.lang3.StringUtils;

public class PrimitiveArrayType implements CompositeType {

    private NumericType numericType;

   public PrimitiveArrayType(NumericType numericType) {
        this.numericType = numericType;
    }

    @SuppressWarnings("ConstantConditions")
    public PrimitiveArrayType(String typeName) {
        this.numericType = (NumericType) NumericType.getNumericType(StringUtils.removeEnd(typeName, "Array")).get();
    }

    @Override
    public String getName() {
        return numericType.getArrayName();
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return "jago/" + getName();
    }

    @Override
    public Type getComponentType() {
        return numericType;
    }
}
