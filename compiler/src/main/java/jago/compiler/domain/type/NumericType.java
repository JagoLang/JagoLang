package jago.compiler.domain.type;

import com.google.common.collect.Maps;
import jago.compiler.domain.node.expression.operation.ArithmeticOperation;

import java.util.*;
import java.util.stream.Collectors;

import static jago.compiler.domain.node.expression.operation.ArithmeticOperation.*;

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

    private static List<ArithmeticOperation> DEFINED_BINARY_OPERATIONS = Arrays.asList(ADD, MUL, DIV, SUB, POW);


    public static Set<String> ARRAY_NAMES = Arrays.stream(values()).map(NumericType::getArrayName).collect(Collectors.toSet());

    public static boolean isOperationDefinedForNonBoolean(ArithmeticOperation o) {
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
    public String toString() {
        return "jago:" + name;
    }
}
