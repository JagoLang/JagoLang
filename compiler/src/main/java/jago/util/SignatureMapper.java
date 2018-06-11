package jago.util;

import jago.domain.Parameter;
import jago.domain.VarargParameter;
import jago.domain.generic.GenericParameter;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.domain.type.Type;
import jago.exception.TypeMismatchException;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

//Todo:UPD:nice shit mate=D
public final class SignatureMapper {


    public static CallableSignature fromMethod(Method method, LocalScope scope) {

        String name = method.getName();
        List<Parameter> parameters = Arrays.stream(method.getParameters())
                .map(p -> getParameter(p, scope))
                .collect(toList());
        if (method.getDeclaringClass().equals(Object.class)
                && !Arrays.asList("equals", "hashCode", "toString").contains(method.getName())) {
            throw new NotImplementedException("Those method you cannot call yet");
        }
        return new CallableSignature(method.getDeclaringClass().getCanonicalName(),
                name,
                parameters,
                fromJavaType(method.getGenericReturnType()));
    }

    private static Parameter getParameter(java.lang.reflect.Parameter p, LocalScope scope) {
        if (p.getType().isPrimitive()) {
            return new Parameter(p.getName(), NumericType.valueOf(p.getType().getName().toUpperCase()));
        }
        //TODO: it might not be null tolerable
        Type parameterType = NullTolerableType.of(fromJavaType(p.getParameterizedType()));
        if (p.isVarArgs())
            return new VarargParameter(p.getName(), parameterType);

        else return new Parameter(p.getName(), parameterType);
    }

    private static Type fromJavaType(java.lang.reflect.Type javaType) {
        if (javaType instanceof Class) {
            return TypeResolver.getFromClass(((Class) javaType));
        }
        if (javaType instanceof TypeVariable) {
            TypeVariable type = (TypeVariable) javaType;
            // TODO compute bounds
            return new GenericParameterType(new GenericParameter(type.getName(), 0, AnyType.INSTANCE));
        }
        if (javaType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) javaType;
            Type rawType = fromJavaType(type.getRawType());
            List<Type> args = new ArrayList<>();
            for (java.lang.reflect.Type type1 : type.getActualTypeArguments()) {
                args.add(fromJavaType(type1));
            }
            return new GenericType(rawType, args);
        }
        if (javaType instanceof GenericArrayType) {
            GenericArrayType type = (GenericArrayType) javaType;
            return new ArrayType(fromJavaType(type.getGenericComponentType()));
        }
        throw new TypeMismatchException();

    }

    public static CallableSignature fromConstructor(Constructor constructor, LocalScope scope) {
        String name = constructor.getName();
        List<Parameter> parameters = Arrays.stream(constructor.getParameters())
                .map(p -> getParameter(p, scope))
                .collect(toList());
        return new CallableSignature(constructor.getDeclaringClass().getCanonicalName(), name, parameters, UnitType.INSTANCE);
    }
}
