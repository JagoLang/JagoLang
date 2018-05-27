package jago.util;


import jago.domain.node.expression.Parameter;
import jago.domain.node.expression.arthimetic.BinaryOperation;
import jago.domain.scope.CallableSignature;
import jago.domain.type.NumericType;
import jago.domain.type.StringType;
import jago.domain.type.Type;
import jago.exception.IllegalReferenceException;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;

/**
 * Resolution of operator overloading
 * <p>
 * The steps of the resolution of operator overloading are as follows:
 * <p>
 * For binary operators:
 * 1. Primitive types overloads most of them always as T.operator(T): T
 * 2. String has operator +
 * 2. Search in the type declaration itself
 * 3. TODO
 * 4. reverse the receiver and the param, try to resolve it like so
 */
public final class OperatorResolver {

    public static CallableSignature resolveBinaryOperation(Type receiver, Type param, BinaryOperation operation) {
        if (receiver instanceof NumericType) {
            if (receiver.equals(param)
                    && !receiver.equals(NumericType.BOOLEAN)
                    && NumericType.isOperationDefinedForNonBoolean(operation)) {
                return new CallableSignature(receiver.getName(),
                        operation.getMethodName(),
                        Collections.singletonList(new Parameter("other", param)), receiver);
            }
            throw new NotImplementedException("Cannot add operations to numeric types");
        }
        if (receiver instanceof StringType && operation.equals(BinaryOperation.ADD)) {
            return new CallableSignature(receiver.getName(), BinaryOperation.ADD.getMethodName(), Collections.singletonList(new Parameter("other", param)), receiver);
        }
        // TODO search the class declaration for the operator (local and compiled)
        // TODO if we add receiver syntax that search receivers (local and compiled)
        if (param instanceof StringType) {
            return new CallableSignature(param.getName(), BinaryOperation.ADD.getMethodName(), Collections.singletonList(new Parameter("other", receiver)), receiver);
        }
        //TODO reverse search

        throw new IllegalReferenceException(Messages.METHOD_DONT_EXIST);
    }

}
