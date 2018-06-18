package jago.compiler.util;

import jago.compiler.domain.Parameter;
import jago.compiler.domain.generic.GenericParameter;
import jago.compiler.domain.generic.GenericsOwner;
import jago.compiler.domain.node.expression.call.Argument;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.GenericCallableSignature;
import jago.compiler.domain.type.*;
import jago.compiler.domain.type.generic.BindableType;
import jago.compiler.domain.type.generic.GenericParameterType;
import jago.compiler.domain.type.generic.GenericType;
import jago.compiler.exception.TypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public final class GenericsTypeChecker {
    private GenericsTypeChecker() {
    }


    private static boolean genericBindsMatchArguments(GenericsOwner boundOwner,
                                                      CallableSignature signature,
                                                      List<Argument> arguments) {
        List<Parameter> parameterTypes = signature.getParameters();
        List<Argument> argumentTypes = ArgumentUtils.sortedArguments(arguments, signature.getParameters());
        for (int i = 0; i < argumentTypes.size(); i++) {
            Type parameterType = parameterTypes.get(i).getType();
            // IDEA thinks argumentType will never be null, this is not the case
            Type argumentType = Optional.ofNullable(argumentTypes.get(i)).map(Argument::getType).orElse(null);
            if (!matchArgumentTypeToParameterType(boundOwner, parameterType, argumentType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchArgumentTypeToParameterType(GenericsOwner genericsOwner,
                                                            Type parameterType,
                                                            Type argumentType) {
        if (argumentType == null) {
            return true;
        }
        if (parameterType instanceof GenericParameterType) {
            if (!matchGenericParameterToArgument(genericsOwner, parameterType.getGenericParameter(), argumentType)) {
                return false;
            }
        }
        if (parameterType instanceof GenericType) {
            if (!(argumentType instanceof GenericType)) {
                return false;
            }
            List<Type> genericArgumentsOfParameter = ((GenericType) parameterType).getGenericArguments();
            List<Type> genericArgumentsOfArgument = ((GenericType) argumentType).getGenericArguments();
            for (int i = 0; i < genericArgumentsOfParameter.size(); i++) {
                Type paramArg = genericArgumentsOfParameter.get(i);
                Type argumentArg = genericArgumentsOfArgument.get(i);
                if (!matchArgumentTypeToParameterType(genericsOwner, paramArg, argumentArg)) {
                    return false;
                }
            }
        }
        if (parameterType instanceof DecoratorType) {
            if (argumentType instanceof DecoratorType) {
                if (!matchArgumentTypeToParameterType(genericsOwner,
                        ((DecoratorType) parameterType).getInnerType(),
                        ((DecoratorType) argumentType).getInnerType()))
                    return false;
            }
            if (!matchArgumentTypeToParameterType(genericsOwner,
                    ((DecoratorType) parameterType).getInnerType(),
                    argumentType)) {
                return false;
            }
        }
        if (parameterType instanceof ArrayType) {
            if (!(argumentType instanceof ArrayType)) {
                return false;
            }
            return matchArgumentTypeToParameterType(genericsOwner,
                    ((ArrayType) parameterType).getComponentType(),
                    ((ArrayType) argumentType).getComponentType());
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
        List<GenericParameter> bounds = signature.getBounds();
        Type[] genericBinds = new Type[bounds.size()];
        return signature;
        // TODO: deduce generic binds from arguments if possible
        // we are not done yet, is return type generic? we might be able to do a type match later, consider this to be a match, type check will be done later
    }

    private static void tryDeduceArgsFromArguments(GenericsOwner owner,
                                                   List<Argument> sortedArguments,
                                                   List<Parameter> parameters,
                                                   Type[] genericBinds) {
        List<GenericParameter> bounds = owner.getBounds();
        List<Type> parameterTypes = parameters.stream().map(Parameter::getType).collect(toList());
        for (int i = 0; i < sortedArguments.size(); i++) {
            // this has to be recursive in the same way as {@link GenericsTypeChecker#genericBindsMatchArguments}
        }
    }

    private static boolean matchGenericParameterToArgument(GenericsOwner boundType,
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
        } else if (typeToBind instanceof ArrayType && ((ArrayType) typeToBind).isUnbound()) {
            Type type = ((ArrayType) typeToBind).getGenericArguments().get(0);
            return ((ArrayType) typeToBind).bind(Collections.singletonList(bindType(signature, ownerTypeBound, type)));
        } else if (typeToBind instanceof DecoratorType) {
            if (typeToBind instanceof NullableType) {
                return NullableType.of(bindType(signature, ownerTypeBound, ((NullableType) typeToBind).getInnerType()));
            }
            if (typeToBind instanceof NullTolerableType) {
                return NullTolerableType.of(bindType(signature, ownerTypeBound, ((NullTolerableType) typeToBind).getInnerType()));
            }
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
