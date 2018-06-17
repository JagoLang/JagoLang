package jago.bytecodegeneration.intristics;

import jago.domain.type.*;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import org.apache.commons.lang3.NotImplementedException;

public class JvmNamingIntrinsics {


    public static String getJVMDescriptor(Type type) {
        if (type instanceof NumericType) {
            return getNumericDescriptor((NumericType) type);
        }
        if (type instanceof DecoratorType) {
            Type innerType = ((DecoratorType) type).getInnerType();
            if (innerType instanceof NumericType) {
                return JvmNumericEquivalent.fromNumeric(((NumericType) innerType)).getJvmDescriptor();
            }
            return getJVMDescriptor(innerType);
        }
        if (type instanceof ArrayType || type instanceof PrimitiveArrayType) {
            return getJVMInternalName(type);
        }
        return "L" + getJVMInternalName(type) + ';';

    }

    public static String getNumericDescriptor(NumericType type) {
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
                return "I";
        }
    }

    public static String getJVMInternalName(Type type) {
        if (type instanceof ClassType || type instanceof NonInstantiatableType) {
            return type.getName().replace('.', '/');
        }
        if (type instanceof NumericType) {
            return JvmNumericEquivalent.fromNumeric(((NumericType) type)).getJvmInternalName();
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
            return getJVMInternalName(type.getGenericParameter().getConstraint());
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
