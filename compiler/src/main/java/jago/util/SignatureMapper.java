package jago.util;

import jago.domain.Parameter;
import jago.domain.VarargParameter;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
        return new CallableSignature(method.getDeclaringClass().getCanonicalName(), name, parameters, TypeResolver.getFromClass(returnType));
    }

    private static Parameter getParameter(java.lang.reflect.Parameter p, LocalScope scope) {
        Type parameterType;
        if (p.getType().isPrimitive())
            parameterType = NumericType.valueOf(p.getType().getName().toUpperCase());
        else
            //TODO: it might not be null tolerable
            parameterType = NullTolerableType.of(TypeResolver.getFromClass(p.getType()));
        if (p.isVarArgs())

            return new VarargParameter(p.getName(), ((CompositeType) parameterType));

        else return new Parameter(p.getName(), parameterType);
    }

    public static CallableSignature fromConstructor(Constructor constructor, LocalScope scope) {
        String name = constructor.getName();
        List<Parameter> parameters = Arrays.stream(constructor.getParameters())
                .map(p -> getParameter(p, scope))
                .collect(toList());
        return new CallableSignature(constructor.getDeclaringClass().getCanonicalName(), name, parameters, UnitType.INSTANCE);
    }
}
