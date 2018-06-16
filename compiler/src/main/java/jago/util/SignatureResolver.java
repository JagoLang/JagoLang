package jago.util;

import jago.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.Parameter;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.NamedArgument;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Todo: this is probably a decent class, however a signature resolve should consider this:
 * 1. TODO: This is not a Jago class a type revolved should be Null tolerant
 * 2. TODO: A search should be conducted by a possible gradual addition of nullability to every argument,
 * 3. (This is done by {@link MethodUtils#getMatchingAccessibleMethod(Class, String, Class[])}) For external methods, also search by gradually replacing primitives this their class equivalent
 * that is a lot of permutations, however I don't think there is a better way to do a match,
 * in those cases also there should no extra type matching checks
 */
public final class SignatureResolver {

    public static Optional<CallableSignature> getMethodSignatureForInstanceCall(Type owner,
                                                                                String methodName,
                                                                                List<Argument> arguments,
                                                                                LocalScope scope) {
        try {
            //TODO: any method of numeric and have a bytecode implementation must be returned here
            //Todo: we don't have classes yet we don't need to do a local search for the instance calls
            Class<?> methodOwnerClass = ClassUtils.getClass(owner.getName(), false);
            Class<?>[] params = arguments.stream()
                    .map(a -> getClassFromType(a.getType())).toArray(Class<?>[]::new);
            Method method = MethodUtils.getMatchingAccessibleMethod(methodOwnerClass, methodName, params);
            return Optional.of(SignatureMapper.fromMethod(method));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<CallableSignature> getMethodSignatureForStaticCall(Type owner,
                                                                              String methodName,
                                                                              List<Argument> arguments,
                                                                              LocalScope scope) {
        try {
            CompilationUnitScope unitScope = CompilationMetadataStorage.compilationUnitScopes.getOrDefault(owner.getName().replace('.', '/'), null);
            if (unitScope != null) {
                List<CallableSignature> callableSignatureStream = unitScope
                        .getCallableSignatures().stream()
                        .filter(signature -> signature.getName().equals(methodName)).collect(toList());

                return getMatchingLocalFunction(methodName, callableSignatureStream, arguments);
            }
            Class<?> methodOwnerClass = ClassUtils.getClass(owner.getName(), false);
            Class<?>[] params = arguments.stream()
                    .map(a -> getClassFromType(a.getType())).toArray(Class<?>[]::new);
            Method method = MethodUtils.getMatchingAccessibleMethod(methodOwnerClass, methodName, params);
            return Optional.of(SignatureMapper.fromMethod(method));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Class<?> getClassFromType(Type type) {
        String javaName = JvmNamingIntrinsics.getJvmName(type);
        try {
            return ClassUtils.getClass(javaName, false);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }


    public static Optional<CallableSignature> getConstructorSignature(String className,
                                                                      List<Argument> arguments,
                                                                      LocalScope scope) {
        if (arguments.stream().anyMatch(a -> a instanceof NamedArgument)) {
            throw new NotImplementedException("named ctor is not supported");
        }
        try {
            //TODO numerics maybe???

            //TODO generic arrays (probably have to create a separate method for generic type resolution)
            /*TODO: arrays are by no means covariant and should not be, however sometimes this is useful,
              so possibly an optional way in the type system to co-variate arrays on call site
            */
            // specialized non boxing arrays
            if (NumericType.ARRAY_NAMES.contains(className)) {
                if (arguments.size() == 1) {
                    Parameter parameter = new Parameter("size", arguments.get(0).getType());
                    return Optional.of(CallableSignature.constructor(new PrimitiveArrayType(className), singletonList(parameter)));
                }
            }
            //TODO search types to ctor locally


            Class<?> methodOwnerClass = ClassUtils.getClass(className, false);
            Class<?>[] params = arguments.stream()
                    .map(a -> getClassFromType(a.getType())).toArray(Class<?>[]::new);
            Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(methodOwnerClass, params);
            CallableSignature callableSignature = SignatureMapper.fromConstructor(constructor);

            return Optional.of(callableSignature);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static final BiPredicate<Argument, Parameter> NON_EXACT_MATCHER = (a, p) -> {
        Type parameterType = p.getType().erased();
        Type argumentType = a.getType().erased();
        if (NullableType.isNullableOf(parameterType, argumentType)) {
            return true;
        }
        //TODO consider this to maybe be a third-phase matcher
        if (parameterType == AnyType.INSTANCE && !argumentType.isNullable()) {
            return true;
        }
        if (parameterType instanceof DecoratorType
                && ((DecoratorType) parameterType).getInnerType() == AnyType.INSTANCE) {
            return true;
        }
        return false;
    };

    private static final BiPredicate<Argument, Parameter> EXACT_MATCHER = (a, p) -> Objects.equals(p.getType().erased(), a.getType().erased());

    public static Optional<CallableSignature> getMatchingLocalFunction(String name,
                                                                       List<CallableSignature> signatures,
                                                                       List<Argument> arguments) {

        if (arguments.stream().anyMatch(a -> a instanceof NamedArgument)) {
            List<CallableSignature> matches = tryToMatchNamedArgList(signatures, arguments, EXACT_MATCHER);
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }
            matches = tryToMatchNamedArgList(signatures, arguments, NON_EXACT_MATCHER);
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }

        } else {
            List<CallableSignature> matches = new ArrayList<>();
            for (CallableSignature signature : signatures) {
                if (signature.matchesBy(name, arguments, EXACT_MATCHER)) {
                    matches.add(signature);
                }
            }
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }
            // there was no exact match, try to find the next best match
            matches.clear();
            for (CallableSignature signature : signatures) {
                if (signature.matchesBy(name, arguments, NON_EXACT_MATCHER)) {
                    matches.add(signature);
                }
            }
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }
            //last effort: is this a vararg?
            List<CallableSignature> varargSignatures = signatures.stream().filter(CallableSignature::isVararg).collect(toList());
            // first try to figure out how many arguments are varargs
            // since varargs are always last there is not defaulting possible, unless named params are used
            matches = tryToMatchVararg(arguments, varargSignatures, EXACT_MATCHER);
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }

            matches = tryToMatchVararg(arguments, varargSignatures, NON_EXACT_MATCHER);
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }

        }
        return Optional.empty();
    }

    private static List<CallableSignature> tryToMatchNamedArgList(List<CallableSignature> signatures,
                                                                  List<Argument> arguments,
                                                                  BiPredicate<Argument, Parameter> matcher) {
        List<CallableSignature> matches = new ArrayList<>();
        for (CallableSignature signature : signatures) {
            int i = 0;
            boolean nonNamedArgsMatch = true;
            while (!(arguments.get(i) instanceof NamedArgument)) {
                Argument argument = arguments.get(i);
                Parameter parameter = signature.getParameters().get(i);
                if (!EXACT_MATCHER.test(argument, parameter)) {
                    nonNamedArgsMatch = false;
                    break;
                }
                ++i;
            }
            if (nonNamedArgsMatch) {
                // all other arguments are named make sure that all of them match to something in the paramList after i
                boolean namedArgsMatch = true;
                List<Parameter> parameterThatCanBeMatched = signature.getParameters().subList(i, signature.getParameters().size());
                for (int j = i; j < arguments.size(); j++) {
                    Argument a = arguments.get(i);
                    Parameter p = parameterThatCanBeMatched.get(i);
                    if (!matcher.test(a, p) && !(((NamedArgument) a).getName().equals(p.getName()))) {
                        namedArgsMatch = false;
                        break;
                    }
                }
                if (namedArgsMatch) {
                    matches.add(signature);
                }
            }
        }
        return matches;
    }

    private static List<CallableSignature> tryToMatchVararg(List<Argument> arguments,
                                                            List<CallableSignature> varargSignatures,
                                                            BiPredicate<Argument, Parameter> matcher) {
        List<CallableSignature> matches = new ArrayList<>();
        for (CallableSignature signature : varargSignatures) {
            boolean allNonVarargsMatch = true;
            for (int i = 0; i < signature.getParameters().size() - 1; i++) {
                Parameter p = signature.getParameters().get(i);
                Argument a = arguments.get(i);
                if (!matcher.test(a, p)) {
                    allNonVarargsMatch = false;
                    break;
                }
            }
            if (allNonVarargsMatch) {
                //TODO:Coerce the last types, this requires classes and inheritance to be added
                Type varargType = signature.getVarargParameter().getComponentType();
                if (arguments.stream()
                        .skip(signature.getParameters().size() - 1)
                        .allMatch(a -> Objects.equals(a.getType(), varargType))) {
                    matches.add(signature);
                }
            }
        }
        return matches;
    }


}
