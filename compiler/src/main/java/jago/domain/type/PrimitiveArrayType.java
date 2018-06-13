package jago.domain.type;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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
    public NumericType getComponentType() {
        return numericType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveArrayType that = (PrimitiveArrayType) o;
        return numericType == that.numericType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(numericType);
    }


    @Override
    public String toString() {
        return numericType.name() + "Array";
    }
}
