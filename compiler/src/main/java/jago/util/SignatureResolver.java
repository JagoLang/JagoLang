package jago.util;

import jago.compiler.CompilationMetadataStorage;
import jago.domain.node.expression.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import jago.domain.scope.LocalScope;
import jago.domain.type.Type;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                            List<Type> paramTypes = signature.getParameters().stream().map(Parameter::getType).collect(Collectors.toList());
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
            Class<?> methodOwnerClass = Class.forName(className);
            Class<?>[] params = arguments.stream()
                    .map(SignatureResolver::getClassFromType).toArray(Class<?>[]::new);
            Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(methodOwnerClass, params);
            return Optional.of(SignatureMapper.fromConstructor(constructor, scope));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static boolean doesConstructorExist(String className, List<Type> arguments, LocalScope scope) {
        try {
            Class<?> methodOwnerClass = Class.forName(className);
            Class<?>[] params = arguments.stream()
                    .map(SignatureResolver::getClassFromType).toArray(Class<?>[]::new);
            Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(methodOwnerClass, params);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
