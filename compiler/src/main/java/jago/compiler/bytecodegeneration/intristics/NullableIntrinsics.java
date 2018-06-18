package jago.compiler.bytecodegeneration.intristics;

import jago.compiler.domain.type.NumericType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NullableIntrinsics {


    public static void generateNumericNullableConversion(NumericType type, MethodVisitor mv) {
        // assume that the needed value is already on the evaluation stack
        JvmNumericEquivalent nullableNumericEquivalent = JvmNumericEquivalent.fromNumeric(type);
        String descriptor = '(' + JvmNamingIntrinsics.getJVMDescriptor(type) + ')' + nullableNumericEquivalent.getJvmDescriptor();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, nullableNumericEquivalent.getJvmInternalName(), "valueOf", descriptor, false);
    }

    public static void generateNumericNullableBackwardsConversion(NumericType type, MethodVisitor mv) {
        // assume that the needed value is already on the evaluation stack
        JvmNumericEquivalent nullableNumericEquivalent = JvmNumericEquivalent.fromNumeric(type);
        String descriptor = "()" + JvmNamingIntrinsics.getNumericDescriptor(type);
        String method = type.getName().toLowerCase() + "Value";
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, nullableNumericEquivalent.getJvmInternalName(), method, descriptor, false);
    }
}
