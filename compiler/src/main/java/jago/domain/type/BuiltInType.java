package jago.domain.type;

import jago.bytecodegeneration.intristics.JvmTypeSpecificInformation;

@Deprecated
public enum BuiltInType implements Type {

    BOOLEAN("boolean",boolean.class,"Z", JvmTypeSpecificInformation.INT),
    INT("int", int.class,"I", JvmTypeSpecificInformation.INT),
    CHAR ("char", char.class,"C", JvmTypeSpecificInformation.INT),
    BYTE ("byte", byte.class,"B", JvmTypeSpecificInformation.INT),
    SHORT ("short", short.class,"S", JvmTypeSpecificInformation.INT),
    LONG ("long", long.class,"J", JvmTypeSpecificInformation.LONG, 2),
    FLOAT ("float", float.class,"F", JvmTypeSpecificInformation.FLOAT),
    DOUBLE ("double", double.class,"D", JvmTypeSpecificInformation.DOUBLE, 2),
    STRING ("string", String.class,"Ljava/lang/String;", JvmTypeSpecificInformation.OBJECT),
    BOOLEAN_ARR("bool[]",boolean[].class,"[B", JvmTypeSpecificInformation.OBJECT),
    INT_ARR ("int[]", int[].class,"[I", JvmTypeSpecificInformation.OBJECT),
    CHAR_ARR ("char[]", char[].class,"[C", JvmTypeSpecificInformation.OBJECT),
    BYTE_ARR ("byte[]", byte[].class,"[B", JvmTypeSpecificInformation.OBJECT),
    SHORT_ARR ("short[]", short[].class,"[S", JvmTypeSpecificInformation.OBJECT),
    LONG_ARR ("long[]", long[].class,"[J", JvmTypeSpecificInformation.OBJECT),
    FLOAT_ARR ("float[]", float[].class,"[F", JvmTypeSpecificInformation.OBJECT),
    DOUBLE_ARR ("double[]", double[].class,"[D", JvmTypeSpecificInformation.OBJECT),
    OBJECT_ARR ("java.lang.Object[]", Object[].class,"[Ljava/lang/Object;", JvmTypeSpecificInformation.OBJECT),
    STRING_ARR ("string[]", String[].class,"[Ljava/lang/String;", JvmTypeSpecificInformation.OBJECT),
    NONE("", null,"", JvmTypeSpecificInformation.OBJECT),
    VOID("void", void.class,"V", JvmTypeSpecificInformation.VOID);

    private final String name;
    private final Class<?> typeClass;
    private final String descriptor;

    BuiltInType(String name, Class<?> typeClass, String descriptor, JvmTypeSpecificInformation opcodes) {
        this.name = name;
        this.typeClass = typeClass;
        this.descriptor = descriptor;
    }

    BuiltInType(String name, Class<?> typeClass, String descriptor, JvmTypeSpecificInformation opcodes, int stackSize) {
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
