package jago.bytecodegeneration.intristics;

import jago.domain.type.Type;

public class TypeOpcodesIntrinsics {

    public static int getReturnOpcode(Type type) {
        return JVMTypeSpecificInformation.of(type).getReturn();
    }

    public static int getStoreOpcode(Type type) {
        return JVMTypeSpecificInformation.of(type).getStore();
    }

    public static int getLoadOpcode(Type type) {
        return JVMTypeSpecificInformation.of(type).getLoad();
    }

    public static int getTypeStackSize(Type type) {
        return JVMTypeSpecificInformation.of(type).getStackSize();
    }
}
