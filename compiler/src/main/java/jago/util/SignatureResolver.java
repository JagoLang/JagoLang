package jago.util;

import jago.compiler.CompilationMetadataStorage;
import jago.domain.node.expression.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.NumericType;
import jago.domain.type.Type;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

//Todo: WTF this is doing here?
public final class SignatureResolver {

    public static Optional<CallableSignature> getMethodSignatureForInstanceCall(Type owner, String methodName, List<Type> arguments, LocalScope scope) {
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

    public static Optional<CallableSignature> getMethodSignatureForStaticCall(Type owner, String methodName, List<Type> arguments, LocalScope scope) {
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
            Class<?> methodOwnerClass = owner.getTypeClass();

            // TODO use class for name
            Class<?>[] params = arguments.stream()
                    .map(SignatureResolver::getClassFromType).toArray(Class<?>[]::new);
            Method method = MethodUtils.getMatchingAccessibleMethod(methodOwnerClass, methodName, params);
            return Optional.of(SignatureMapper.fromMethod(method, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public static Class<?> getClassFromType(Type type) {

        try {
            return Class.forName(type.getName());
        } catch (ClassNotFoundException e) {
            //TODO : add error
           return null;
        }
    }


    public static Optional<CallableSignature> getConstructorSignature(String className, List<Type> arguments, LocalScope scope) {
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
            Class<?>[] params = arguments.stream()
                    .map(SignatureResolver::getClassFromType).toArray(Class<?>[]::new);
            Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(methodOwnerClass, params);
            return Optional.of(SignatureMapper.fromConstructor(constructor, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
