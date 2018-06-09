package jago.bytecodegeneration.intristics;

import jago.domain.type.NumericType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NullableIntrinsics {


    public static void generateNumericNullableConversion(NumericType type, MethodVisitor mv) {
        // assume that the needed value is already on the evaluation stack
        JvmNumericEquivalent nullableNumericEquivalent = JvmNumericEquivalent.fromNumeric(type);
        String descriptor = '(' +JvmNamingIntrinsics.getJVMDescriptor(type) +')' + nullableNumericEquivalent.getJvmDescriptor();
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, nullableNumericEquivalent.getJvmInternalName(), "valueOf", descriptor,false);
    }

}
