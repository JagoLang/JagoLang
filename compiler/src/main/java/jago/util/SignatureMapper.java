package jago.util;

import jago.domain.node.expression.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.NumericType;
import jago.domain.type.Type;
import jago.domain.type.UnitType;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

//Todo:UPD:nice shit mate=D
public final class SignatureMapper {


    public static CallableSignature fromMethod(Method method, LocalScope scope) {

        String name = method.getName();
        List<Parameter> parameters = Arrays.stream(method.getParameters())
                .map(p -> getParameter(p, scope))
                .collect(toList());
        Class<?> returnType = method.getReturnType();
        if (method.getDeclaringClass().equals(Object.class)
                && !Arrays.asList("equals", "hashCode", "toString").contains(method.getName())) {
            throw new NotImplementedException("Those method you cannot call yet");
        }
        return new CallableSignature(method.getDeclaringClass().getCanonicalName(), name, parameters, TypeResolver.getFromTypeNameOrThrow(returnType.getCanonicalName(), scope));
    }

    private static Parameter getParameter(java.lang.reflect.Parameter p, LocalScope scope) {
        Type parameterType = p.getType().isPrimitive()
                ? NumericType.valueOf(p.getType().getName().toUpperCase())
                : TypeResolver.getFromTypeNameOrThrow(p.getType().getCanonicalName(), scope);
        return new Parameter(p.getName(), parameterType);
    }

    public static CallableSignature fromConstructor(Constructor constructor, LocalScope scope) {
        String name = constructor.getName();
        List<Parameter> parameters = Arrays.stream(constructor.getParameters())
                .map(p -> getParameter(p, scope))
                .collect(toList());
        return new CallableSignature(constructor.getDeclaringClass().getCanonicalName(), name, parameters, UnitType.INSTANCE);
    }
}
