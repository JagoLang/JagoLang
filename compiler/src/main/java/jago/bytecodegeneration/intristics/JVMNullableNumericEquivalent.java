package jago.bytecodegeneration.intristics;

import com.google.common.collect.Maps;
import jago.domain.type.NumericType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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

    private static Map<String, JVMNullableNumericEquivalent> INTERNAL_NAMES_TO_NUMERIC_MAP =
            Maps.uniqueIndex(Arrays.stream(values()).iterator(), JVMNullableNumericEquivalent::getJvmInternalName);

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


    public NumericType toNumeric() {
        return NumericType.valueOf(type.toUpperCase());
    }

    public static Optional<NumericType> fromInternalName(String internalName) {
       return Optional.ofNullable(INTERNAL_NAMES_TO_NUMERIC_MAP.get(internalName).toNumeric());
    }
    public String getJvmInternalName() {
        return "java/lang/" + jvmName;
    }

    public String getJvmDescriptor() {
        return 'L' + getJvmInternalName() + ';';
    }
}
