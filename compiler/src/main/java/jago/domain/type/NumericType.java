package jago.domain.type;

import com.google.common.collect.Maps;
import jago.domain.node.expression.arthimetic.BinaryOperation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static jago.domain.node.expression.arthimetic.BinaryOperation.*;

public enum NumericType implements Type {

    BOOLEAN("Boolean"),
    INT("Int"),
    CHAR("Char"),
    BYTE("Byte"),
    SHORT("Short"),
    LONG("Long"),
    FLOAT("Float"),
    DOUBLE("Double");

    private String name;

    private static Map<String, NumericType> NUMERIC_TYPES = Maps.uniqueIndex(Arrays.stream(values()).iterator(), NumericType::getName);

    private static List<BinaryOperation> DEFINED_BINARY_OPERATIONS = Arrays.asList(ADD, MUL, DIV, SUB, POW);


    public static boolean isOperationDefinedForNonBoolean(BinaryOperation o) {
        return DEFINED_BINARY_OPERATIONS.contains(o);
    }

    public static Optional<Type> getNumericType(String name) {
        return Optional.ofNullable(NUMERIC_TYPES.get(name));
    }

    NumericType(String name) {
        this.name = name;
    }

    public String getArrayName() {
        return name + "Array";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return null;
    }


}
