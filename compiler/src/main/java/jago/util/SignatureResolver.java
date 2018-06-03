package jago.util;

import jago.bytecodegeneration.intristics.JVMNullableNumericEquivalent;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.node.expression.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Todo: this is probably a decent class, however a signature resolve should consider this:
 * 1. TODO: This is not a Jago class a type revolved should be Null tolerant
 * 2. TODO: A search should be conducted by a possible gradual addition of nullability to every argument,
 * 3. TODO: For external methods, also search by gradually replacing primitives this their class equivalent
 * that is a lot of permutations, however I don't think there is a better way to do a match,
 * in those cases also there should no extra type matching checks
 */
public final class SignatureResolver {

    public static Optional<CallableSignature> getMethodSignatureForInstanceCall(Type owner,
                                                                                String methodName,
                                                                                List<Type> arguments,
                                                                                LocalScope scope) {
        try {
            //Todo: we don't have classes yet we don't need to do a local search for the instance calls

            Class<?> methodOwnerClass = owner.getTypeClass();
            Class<?>[] params = arguments.stream()
                    .map(Type::getTypeClass).toArray(Class<?>[]::new);
            Method method = MethodUtils.getMatchingAccessibleMethod(methodOwnerClass, methodName, params);
            return Optional.of(SignatureMapper.fromMethod(method, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<CallableSignature> getMethodSignatureForStaticCall(Type owner,
                                                                              String methodName,
                                                                              List<Type> arguments,
                                                                              LocalScope scope) {
        try {
            CompilationUnitScope unitScope = CompilationMetadataStorage.compilationUnitScopes.getOrDefault(owner.getInternalName(), null);
            if (unitScope != null) {
                Optional<CallableSignature> first = unitScope.getCallableSignatures().stream()
                        .filter(signature -> signature.getName().equals(methodName))
                        .filter(signature -> {
                            List<Type> paramTypes = signature.getParameters().stream().map(Parameter::getType).collect(toList());
                            return CollectionUtils.isEqualCollection(paramTypes, arguments);
                        }).findFirst();
                return first;
            }
            Class<?> methodOwnerClass = Class.forName(owner.getName());
            // TODO use class for name
            Class<?>[] params = addNullTolerance(arguments).stream()
                    .map(SignatureResolver::getClassFromType).toArray(Class<?>[]::new);
            Method method = MethodUtils.getMatchingAccessibleMethod(methodOwnerClass, methodName, params);
            return Optional.of(SignatureMapper.fromMethod(method, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    private static List<NullTolerableType> addNullTolerance(List<Type> types) {
        List<NullTolerableType> returnList = new ArrayList<>();
        for (Type type : types) {
            returnList.add(NullTolerableType.of(type));
        }
        return returnList;
    }

    public static Class<?> getClassFromType(NullTolerableType nullTolerableType) {
        Type type = nullTolerableType.getInnerType();
        String javaName = type.getName();
        if (type instanceof NumericType) {
            javaName = javaName.toLowerCase();
        }
        if (type instanceof NullableType) {

            Type innerType = ((NullableType) type).getInnerType();
            if (innerType instanceof NumericType) {
                javaName = JVMNullableNumericEquivalent.fromNumeric(((NumericType) innerType)).getJvmInternalName();
            }
        }
        if (type instanceof StringType) {
            return String.class;
        }
        try {
            return ClassUtils.getClass(javaName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }


    public static Optional<CallableSignature> getConstructorSignature(String className,
                                                                      List<Type> arguments,
                                                                      LocalScope scope) {
        try {
            //TODO numerics maybe???

            //TODO generic arrays (probably have to create a separate method for generic type resolution)
            //TODO arrays are by no means covariant and should not be, however sometimes this is useful, so possibly an optional way in the type system to co-variate arrays on call site
            // specialized non boxing arrays
            if (ArrayUtils.contains(NumericType.getArrayNames(), className)) {
                if (arguments.size() == 1) {
                    Type type = arguments.get(0);
                    Parameter parameter = new Parameter("initialSize", type);
                    return Optional.of(CallableSignature.constructor(className, singletonList(parameter)));
                }
            }
            //TODO search types to ctor locally


            Class<?> methodOwnerClass = Class.forName(className);
            Class<?>[] params = addNullTolerance(arguments).stream()
                    .map(SignatureResolver::getClassFromType).toArray(Class<?>[]::new);
            Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(methodOwnerClass, params);
            return Optional.of(SignatureMapper.fromConstructor(constructor, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
