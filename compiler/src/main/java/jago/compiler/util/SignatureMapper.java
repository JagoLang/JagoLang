package jago.compiler.util;

import jago.compiler.domain.Parameter;
import jago.compiler.domain.VarargParameter;
import jago.compiler.domain.generic.GenericParameter;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.GenericCallableSignature;
import jago.compiler.domain.type.*;
import jago.compiler.domain.type.Type;
import jago.compiler.domain.type.generic.GenericParameterType;
import jago.compiler.domain.type.generic.GenericType;
import jago.compiler.exception.internal.InternalException;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jago.compiler.util.TypeResolver.nullify;
import static java.util.stream.Collectors.toList;

//Todo:UPD:nice shit mate=D
public final class SignatureMapper {

    private SignatureMapper() {
    }

    public static CallableSignature fromMethod(Method method) {

        String name = method.getName();
        List<Parameter> parameters = Arrays.stream(method.getParameters())
                .map(SignatureMapper::getParameter)
                .collect(toList());
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.equals(Object.class)
                && !Arrays.asList("equals", "hashCode", "toString").contains(method.getName())) {
            throw new NotImplementedException("Those method you cannot call yet");
        }

        Type returnType = nullify(fromJavaType(method.getGenericReturnType()));
        Type ownerType = fromJavaType(declaringClass, declaringClass.getTypeParameters());

        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        if (typeParameters.length == 0) {
            return new CallableSignature(ownerType,
                    name,
                    parameters,
                    returnType);
        }
        List<GenericParameter> genericParameters = Arrays.stream(typeParameters)
                .map(tp -> new GenericParameter(tp.getName()))
                .collect(toList());
        List<Type> genericParameterTypes = genericParameters.stream().map(GenericParameterType::new).collect(toList());
        GenericCallableSignature genericCallableSignature = new GenericCallableSignature(ownerType, name, parameters, returnType, genericParameterTypes, genericParameters);
        genericParameters.forEach(gp -> gp.setOwner(genericCallableSignature));
        return genericCallableSignature;
    }

    private static Parameter getParameter(java.lang.reflect.Parameter p) {
        Type parameterType = nullify(fromJavaType(p.getParameterizedType()));
        return p.isVarArgs()
                ? new VarargParameter(p.getName(), parameterType)
                : new Parameter(p.getName(), parameterType);
    }


    private static Type fromJavaType(java.lang.reflect.Type javaType) {
        if (javaType instanceof Class) {
            return TypeResolver.getFromClass(((Class) javaType));
        }
        if (javaType instanceof TypeVariable) {
            TypeVariable type = (TypeVariable) javaType;
            // TODO compute bounds
            return new GenericParameterType(new GenericParameter(type.getName(), 0, NullableType.of(AnyType.INSTANCE)));
        }
        if (javaType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) javaType;
            Class<?> rawTypeClass = (Class<?>) type.getRawType();
            Type rawType = TypeResolver.getFromClass(rawTypeClass);
            List<Type> args = new ArrayList<>();
            for (java.lang.reflect.Type type1 : type.getActualTypeArguments()) {
                args.add(fromJavaType(type1));
            }
            List<GenericParameter> genericParameters = Arrays.stream(rawTypeClass.getTypeParameters())
                    .map(tp -> new GenericParameter(tp.getName(), 0, NullableType.of(AnyType.INSTANCE)))
                    .collect(toList());
            GenericType genericType = new GenericType(rawType, args, genericParameters);
            genericParameters.forEach(gp -> gp.setOwner(genericType));
            for (int i = 0; i < args.size(); i++) {
                Type arg = args.get(i);
                if (arg instanceof GenericParameterType) {
                    args.set(i, new GenericParameterType(genericParameters.get(i)));
                }
            }
            return genericType;
        }
        if (javaType instanceof GenericArrayType) {
            GenericArrayType type = (GenericArrayType) javaType;
            return new ArrayType(fromJavaType(type.getGenericComponentType()));
        }
        throw new InternalException("Type does not exist");
    }

    private static Type fromJavaType(Class<?> javaType, TypeVariable<?>[] typeVariables) {
        Type type = TypeResolver.getFromClass(javaType);
        if (typeVariables.length == 0) {
            return type;
        }
        List<Type> collect = new ArrayList<>();
        List<GenericParameter> genericParameters = new ArrayList<>();
        for (TypeVariable<?> tv : typeVariables) {
            GenericParameter genericParameter = new GenericParameter(tv.getName(), 0, AnyType.INSTANCE);
            genericParameters.add(genericParameter);
            GenericParameterType genericParameterType = new GenericParameterType(genericParameter);
            collect.add(genericParameterType);
        }
        GenericType genericType = new GenericType(type, collect, genericParameters);
        genericParameters.forEach(gp -> gp.setOwner(genericType));
        return genericType;
    }

    public static CallableSignature fromConstructor(Constructor constructor) {
        String name = constructor.getName();
        List<Parameter> parameters = Arrays.stream(constructor.getParameters())
                .map(SignatureMapper::getParameter)
                .collect(toList());
        Class declaringClass = constructor.getDeclaringClass();
        return new CallableSignature(fromJavaType(declaringClass, declaringClass.getTypeParameters()), name, parameters, UnitType.INSTANCE);
    }
}
