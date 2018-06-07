package jago.bytecodegeneration.intristics;

import jago.domain.type.*;
import org.apache.commons.lang3.NotImplementedException;

public class JVMNamingIntrinsics {


    public static String getJVMDescriptor(Type type) {
         if (type instanceof NumericType) {
             switch ((NumericType) type) {
                 case BOOLEAN:
                     return "Z";
                 case INT:
                     return "I";
                 case CHAR:
                     return "C";
                 case BYTE:
                     return  "B";
                 case SHORT:
                     return "S";
                 case LONG:
                     return "J";
                 case FLOAT:
                     return "F";
                 case DOUBLE:
                     return "D";
             }
         }
         if (type instanceof BuiltInType) {
             return type.getDescriptor();
         }
         return "L" + getJVMInternalName(type) + ';';

    }
    public static String getJVMInternalName(Type type) {
        if (type instanceof ClassType) {
            return type.getName().replace('.', '/');
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
        if (type instanceof BuiltInType) {
            //TODO remove this once the arrays are added properly
            return type.getInternalName();
        }
        if (type instanceof NumericType) {
            return JVMNullableNumericEquivalent.fromNumeric(((NumericType) type)).getJvmInternalName();
        }
        if (type instanceof NullableType) {
            Type innerType = ((NullableType) type).getComponentType();
            if (innerType instanceof NumericType) {
                  return JVMNullableNumericEquivalent.fromNumeric(((NumericType) innerType))
                          .getJvmInternalName();
            }
            return getJVMInternalName(innerType);
        }
        throw new NotImplementedException(type.toString() + " type has no internal name");
    }
    public static String getJVMIReturnDescriptor(Type type) {
        return type.equals(UnitType.INSTANCE) ? "V" : getJVMDescriptor(type);
    }
}
