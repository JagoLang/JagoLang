package jago.bytecodegeneration.intristics;

import jago.domain.type.NumericType;

public enum  JVMNullableNumericEquivalent {

    INT("Int","Integer"),
    CHAR("Char", "Character"),
    LONG("Long"),
    BOOLEAN("Boolean"),
    SHORT("Short"),
    BYTE("Byte"),
    FLOAT("Float"),
    DOUBLE("Double");


    private final String type;
    private final String jvmName;

    JVMNullableNumericEquivalent(String type, String jvmName) {
        this.type = type;
        this.jvmName = jvmName;
    }
    JVMNullableNumericEquivalent(String type) {
        this.type = type;
        this.jvmName = type;
    }

    public static JVMNullableNumericEquivalent fromNumeric(NumericType type) {
       return valueOf(type.name());
    }

    public String getJvmInternalName() {
        return "java/lang/" + jvmName;
    }
    public String getJvmDescriptor() {
        return 'L' + getJvmInternalName() + ';';
    }
}
