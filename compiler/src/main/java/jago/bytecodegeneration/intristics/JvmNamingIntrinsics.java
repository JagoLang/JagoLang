package jago.bytecodegeneration.intristics;

import jago.domain.type.*;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

public class JvmNamingIntrinsics {


    public static String getJVMDescriptor(Type type) {
        if (type instanceof NumericType) {
            String nd = getNumericDescriptor((NumericType) type);
            return nd != null ? nd : "I";
        }
        if (type instanceof DecoratorType) {
            Type innerType = ((DecoratorType) type).getInnerType();
            if (innerType instanceof NumericType) {
                return JvmNumericEquivalent.fromNumeric(((NumericType) innerType)).getJvmDescriptor();
            }
            return "L" + getJVMInternalName(type) + ';';
        }
        if (type instanceof ArrayType || type instanceof PrimitiveArrayType) {
            return getJVMInternalName(type);
        }
        return "L" + getJVMInternalName(type) + ';';

    }

    private static String getNumericDescriptor(NumericType type) {
        switch (type) {
            case BOOLEAN:
                return "Z";
            case INT:
                return "I";
            case CHAR:
                return "C";
            case BYTE:
                return "B";
            case SHORT:
                return "S";
            case LONG:
                return "J";
            case FLOAT:
                return "F";
            case DOUBLE:
                return "D";
            default:
                return null;
        }
    }

    public static String getJVMInternalName(Type type) {
        if (type instanceof ClassType) {
            return type.getName().replace('.', '/');
        }
        if (type instanceof NumericType) {
            return StringUtils.uncapitalize(type.getName());
        }
        if (type instanceof AnyType) {
            return "java/lang/Object";
        }
        if (type instanceof UnitType) {
            throw new NotImplementedException("Unit not implemented");
        }
        if (type instanceof StringType) {
            return "java/lang/String";
        }
        if (type instanceof PrimitiveArrayType) {
            return '[' + getNumericDescriptor(((PrimitiveArrayType) type).getComponentType());
        }
        if (type instanceof ArrayType) {
            Type componentType = ((ArrayType) type).getComponentType();
            return '[' + (getJVMDescriptor(componentType));
        }
        if (type instanceof DecoratorType) {
            Type innerType = ((DecoratorType) type).getInnerType();
            if (innerType instanceof NumericType) {
                return JvmNumericEquivalent.fromNumeric(((NumericType) innerType))
                        .getJvmInternalName();
            }
            return getJVMInternalName(innerType);
        }
        if (type instanceof GenericParameterType) {
            return "java/lang/Object";
        }
        if (type instanceof GenericType) {
            return getJVMInternalName(((GenericType) type).getType());
        }
        throw new NotImplementedException(type.toString() + " type has no internal name");
    }

    public static String getJvmName(Type type) {
        return getJVMInternalName(type).replace('/', '.');
    }

    public static String getJVMIReturnDescriptor(Type type) {
        return type.equals(UnitType.INSTANCE) ? "V" : getJVMDescriptor(type);
    }
}
