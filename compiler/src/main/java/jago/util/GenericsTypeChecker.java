package jago.util;

import jago.domain.Parameter;
import jago.domain.generic.GenericParameter;
import jago.domain.generic.GenericsOwner;
import jago.domain.node.expression.call.Argument;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.GenericCallableSignature;
import jago.domain.type.NullableType;
import jago.domain.type.Type;
import jago.domain.type.generic.BindableType;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import jago.exception.TypeMismatchException;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class GenericsTypeChecker {
    private GenericsTypeChecker() {
    }

    private static boolean genericBindsMatchArguments(GenericsOwner boundType,
                                                      CallableSignature signature,
                                                      List<Argument> arguments) {
        List<Type> parameterTypes = signature.getParameters().stream().map(Parameter::getType).collect(toList());

        List<Type> argumentTypes = ArgumentUtils.sortedArguments(arguments, signature.getParameters()).stream().map(Argument::getType).collect(toList());
        for (int i = 0; i < argumentTypes.size(); i++) {
            Type parameterType = parameterTypes.get(i);
            Type argumentType = argumentTypes.get(i);
            if (parameterType instanceof GenericParameterType) {
                if (!matchGenericParameterToArgument(boundType, parameterType.getGenericParameter(), argumentType)) {
                    return false;
                }
                continue;
            }
            if (parameterType instanceof GenericType
                    && argumentType instanceof GenericType
                    && !matchGenericTypeToArgument(boundType, (GenericType) parameterType, (GenericType) argumentType)) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchGenericTypeToArgument(GenericsOwner boundType,
                                                     GenericType parameterType,
                                                     GenericType argumentType) {
        List<Type> genericArgumentsFromParameter = parameterType.getGenericArguments();
        List<Type> genericArgumentsFromArgument = argumentType.getGenericArguments();
        if (genericArgumentsFromArgument.size() != genericArgumentsFromParameter.size()) {
            return false;
        }
        for (int i = 0; i < genericArgumentsFromParameter.size(); i++) {
            Type typeParam = genericArgumentsFromParameter.get(i);
            Type typeArg = genericArgumentsFromArgument.get(i);
            if (typeParam instanceof GenericParameterType) {
                if (!matchGenericParameterToArgument(boundType, parameterType.getGenericParameter(), argumentType)) {
                    return false;
                }

            } else if (typeParam instanceof GenericType && typeArg instanceof GenericType) {
                if (!matchGenericTypeToArgument(boundType, ((GenericType) typeParam), ((GenericType) typeArg))) {
                    return false;
                }
            }
        }
        return true;

    }

    public static Type bindSignatureOwner(CallableSignature signature,
                                          List<Type> genericArguments,
                                          List<Argument> arguments) {
        GenericType signatureOwnerGeneric = (GenericType) signature.getOwner();
        if (!genericArguments.isEmpty()) {
            GenericType boundType = signatureOwnerGeneric.bind(genericArguments);
            if (!genericBindsMatchArguments(boundType, signature, arguments)) {
                throw new TypeMismatchException();
            }
            return boundType;
        }
        // TODO deduce generic binds from arguments if possible
        return signatureOwnerGeneric;
    }

    public static CallableSignature bindGenericSignature(GenericCallableSignature signature,
                                                                List<Type> genericArguments,
                                                                List<Argument> arguments) {
        if (genericArguments != null) {
            GenericCallableSignature boundSignature = signature.bind(genericArguments);
            if (!genericBindsMatchArguments(boundSignature, signature, arguments)) {
                throw new TypeMismatchException();
            }
            return boundSignature;
        }
        if (signature.getReturnType() instanceof BindableType) {
            return signature;
        }
        return signature;
        // TODO: deduce generic binds from arguments if possible
        // TODO: we are not done yet, is return type generic? we might be able to do a type match later, consider this to be a match, type check will be done later
    }

    public static boolean matchGenericParameterToArgument(GenericsOwner boundType,
                                                          GenericParameter parameter,
                                                          Type argumentType) {
        int i = boundType.getBounds().indexOf(parameter);
        if (i == -1) {
            // it doesn't actually match however it might match to a signature generic,
            // so just bypass it
            return true;
        }
        Type type = boundType.getGenericArguments().get(i);
        return NullableType.isNullableOf(type, argumentType);

    }

    public static Type bindType(CallableSignature signature, Type ownerTypeBound, Type typeToBind) {
        if (typeToBind instanceof GenericParameterType) {
            if (ownerTypeBound instanceof GenericType) {
                GenericType genericType = (GenericType) ownerTypeBound;
                int i = genericType.getBounds().indexOf(typeToBind.getGenericParameter());
                if (i != -1) {
                    return genericType.getGenericArguments().get(i);
                }
            }
            if (signature instanceof GenericCallableSignature) {
                GenericCallableSignature genericCallableSignature = (GenericCallableSignature) signature;
                int i = genericCallableSignature.getBounds().indexOf(typeToBind.getGenericParameter());
                if (i != -1) {
                    return genericCallableSignature.getGenericArguments().get(i);
                }
            }
        } else if (typeToBind instanceof GenericType && ((GenericType) typeToBind).isUnbound()) {
            GenericType genericReturnType = (GenericType) typeToBind;
            List<Type> boundArgs = genericReturnType.getGenericArguments().stream().map(ga -> bindType(signature, ownerTypeBound, ga)).collect(toList());
            return genericReturnType.bind(boundArgs);
        }
        return typeToBind;
    }

    public static void checkGenericBindsForOwner(CallableSignature signature,
                                                 Type ownerType,
                                                 List<Argument> arguments) {
        if (ownerType instanceof GenericType) {
            if (!genericBindsMatchArguments(((GenericType) ownerType), signature, arguments)) {
                throw new TypeMismatchException();
            }
        }
    }

    private static void checkGenericBindsForSignature(CallableSignature genericSignature,
                                                      List<Argument> arguments) {
        if (genericSignature instanceof GenericCallableSignature) {
            if (!genericBindsMatchArguments(((GenericCallableSignature) genericSignature), genericSignature, arguments)) {
                throw new TypeMismatchException();
            }
        }
    }
}
