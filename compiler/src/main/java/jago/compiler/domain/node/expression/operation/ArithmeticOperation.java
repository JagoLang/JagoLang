package jago.compiler.domain.node.expression.operation;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 *
 */
public enum ArithmeticOperation {


    SUB("-", "minus"),
    ADD("+", "plus"),
    MUL("*", "multiply"),
    DIV("/", "divide"),
    POW("**", "toPower"),
    REM("%", "modulo");
    private final String stringRepresentation;
    private final String methodName;
    ArithmeticOperation(String stringRepresentation, String methodName) {
        this.stringRepresentation = stringRepresentation;
        this.methodName = methodName;
    }

    private static final Map<String, ArithmeticOperation> MAP_SYMBOL_TO_OPERATION = Maps.uniqueIndex(Arrays.stream(values()).iterator(), v -> v.stringRepresentation);
    private static final Map<String, ArithmeticOperation> MAP_METHOD_TO_OPERATION = Maps.uniqueIndex(Arrays.stream(values()).iterator(), v -> v.methodName);

    public static ArithmeticOperation getOperation(String value) {
        return MAP_SYMBOL_TO_OPERATION.getOrDefault(value, null);
    }

    public static ArithmeticOperation getOperationFromMethodName(String name) {
        return MAP_METHOD_TO_OPERATION.getOrDefault(name, null);
    }

    public String getMethodName() {
        return methodName;
    }
}
