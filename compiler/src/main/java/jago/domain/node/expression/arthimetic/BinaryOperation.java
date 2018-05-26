package jago.domain.node.expression.arthimetic;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public enum BinaryOperation {


    SUB("-", "minus"),
    ADD("+", "plus"),
    MUL("*", "multiply"),
    DIV("/", "divide"),
    POW("**", "toPower"),
    REM("%", "modulo");
    private final String strRepr;
    private final String methodName;
    BinaryOperation(String strRepr, String methodName) {
        this.strRepr = strRepr;
        this.methodName = methodName;
    }

    private static final Map<String, BinaryOperation> MAP_SYMBOL_TO_OPERATION = Maps.uniqueIndex(Arrays.stream(values()).iterator(), v -> v.strRepr);
    private static final Map<String, BinaryOperation> MAP_METHOD_TO_OPERATION = Maps.uniqueIndex(Arrays.stream(values()).iterator(), v -> v.methodName);
    // TODO: add a map so iteration over the list is redundant
    public static BinaryOperation getOperation(String value) {
        return Optional.ofNullable(MAP_SYMBOL_TO_OPERATION.get(value)).orElseThrow(() -> new NotImplementedException("BinaryOperation code doesnt exists"));
    }
    public static BinaryOperation getOperationFromMethodName(String name) {
        return MAP_METHOD_TO_OPERATION.getOrDefault(name, null);
    }
    public String getMethodName() {
        return methodName;
    }
}
