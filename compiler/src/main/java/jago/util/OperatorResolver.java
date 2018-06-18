package jago.util;


import jago.domain.Parameter;
import jago.domain.node.expression.operation.ArithmeticOperation;
import jago.domain.node.expression.call.Argument;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.exception.IllegalReferenceException;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public static CallableSignature resolveBinaryOperation(Type receiver,
                                                           Type param,
                                                           ArithmeticOperation operation,
                                                           LocalScope scope) {
        if (receiver instanceof NumericType) {
            if (receiver.equals(param)
                    && !receiver.equals(NumericType.BOOLEAN)
                    && NumericType.isOperationDefinedForNonBoolean(operation)) {
                return new CallableSignature(receiver,
                        operation.getMethodName(),
                        Collections.singletonList(new Parameter("other", param)), receiver);
            }
            throw new NotImplementedException("Cannot add operations to numeric types");
        }
        if (receiver instanceof StringType && operation.equals(ArithmeticOperation.ADD)) {
            return new CallableSignature(receiver, ArithmeticOperation.ADD.getMethodName(), Collections.singletonList(new Parameter("other", param)), receiver);
        }
        // TODO search the class declaration for the operator (local and compiled)
        // TODO if we add receiver syntax that search receivers (local and compiled)
        if (param instanceof StringType) {
            return new CallableSignature(param, ArithmeticOperation.ADD.getMethodName(), Collections.singletonList(new Parameter("other", receiver)), receiver);
        }
        //TODO reverse search

        throw new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, operation.getMethodName(), param.toString()));
    }

    public static CallableSignature resolveGetIndexer(Type receiver, List<Argument> params, LocalScope scope) {
        if (receiver instanceof CompositeType
                && params.size() == 1
                && params.get(0).getType().equals(NumericType.INT)) {
            return new CallableSignature(receiver,
                    "get",
                    Collections.singletonList(new Parameter("index", params.get(0).getType())),
                    ((ArrayType) receiver).getComponentType());
        }
        return SignatureResolver.getMethodSignatureForInstanceCall(receiver, "get", params, scope)
                .orElseThrow(() -> new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, "get", params.toString())));

    }

    public static CallableSignature resolveSetIndexer(Type receiver, List<Argument> arguments, LocalScope scope) {
        if (receiver instanceof CompositeType) {
            CompositeType arrayReceiver = (CompositeType) receiver;
            if (arguments.size() == 2
                    && arguments.get(0).getType().equals(NumericType.INT)
                    && arguments.get(1).getType().erased().equals(arrayReceiver.getComponentType())) {
                return new CallableSignature(receiver,
                        "set",
                        Arrays.asList(
                                new Parameter("index", arguments.get(0).getType()),
                                new Parameter("value", arguments.get(1).getType().erased())),
                        UnitType.INSTANCE);
            }
        }
        return SignatureResolver.getMethodSignatureForInstanceCall(receiver, "set", arguments, scope)
                .orElseThrow(() -> new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, "get", arguments.toString())));
    }

}
