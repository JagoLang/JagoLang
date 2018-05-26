package jago.domain.type;

import jago.bytecodegeneration.intristics.JVMTypeSpecificInformation;

@Deprecated
public enum BuiltInType implements Type {

    BOOLEAN("boolean",boolean.class,"Z", JVMTypeSpecificInformation.INT),
    INT("int", int.class,"I", JVMTypeSpecificInformation.INT),
    CHAR ("char", char.class,"C", JVMTypeSpecificInformation.INT),
    BYTE ("byte", byte.class,"B", JVMTypeSpecificInformation.INT),
    SHORT ("short", short.class,"S", JVMTypeSpecificInformation.INT),
    LONG ("long", long.class,"J", JVMTypeSpecificInformation.LONG, 2),
    FLOAT ("float", float.class,"F", JVMTypeSpecificInformation.FLOAT),
    DOUBLE ("double", double.class,"D", JVMTypeSpecificInformation.DOUBLE, 2),
    STRING ("string", String.class,"Ljava/lang/String;", JVMTypeSpecificInformation.OBJECT),
    BOOLEAN_ARR("bool[]",boolean[].class,"[B", JVMTypeSpecificInformation.OBJECT),
    INT_ARR ("int[]", int[].class,"[I", JVMTypeSpecificInformation.OBJECT),
    CHAR_ARR ("char[]", char[].class,"[C", JVMTypeSpecificInformation.OBJECT),
    BYTE_ARR ("byte[]", byte[].class,"[B", JVMTypeSpecificInformation.OBJECT),
    SHORT_ARR ("short[]", short[].class,"[S", JVMTypeSpecificInformation.OBJECT),
    LONG_ARR ("long[]", long[].class,"[J", JVMTypeSpecificInformation.OBJECT),
    FLOAT_ARR ("float[]", float[].class,"[F", JVMTypeSpecificInformation.OBJECT),
    DOUBLE_ARR ("double[]", double[].class,"[D", JVMTypeSpecificInformation.OBJECT),
    OBJECT_ARR ("java.lang.Object[]", Object[].class,"[Ljava/lang/Object;", JVMTypeSpecificInformation.OBJECT),
    STRING_ARR ("string[]", String[].class,"[Ljava/lang/String;", JVMTypeSpecificInformation.OBJECT),
    NONE("", null,"", JVMTypeSpecificInformation.OBJECT),
    VOID("void", void.class,"V", JVMTypeSpecificInformation.VOID);

    private final String name;
    private final Class<?> typeClass;
    private final String descriptor;

    BuiltInType(String name, Class<?> typeClass, String descriptor, JVMTypeSpecificInformation opcodes) {
        this.name = name;
        this.typeClass = typeClass;
        this.descriptor = descriptor;
    }

    BuiltInType(String name, Class<?> typeClass, String descriptor, JVMTypeSpecificInformation opcodes, int stackSize) {
        this.name = name;
        this.typeClass = typeClass;
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getTypeClass() {
        return typeClass;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public String getInternalName() {
        if (getTypeClass().equals(String.class)) {
            return "java/lang/String";
        }
        return getDescriptor();
    }


}
