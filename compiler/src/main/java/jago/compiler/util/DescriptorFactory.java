package jago.compiler.util;

import jago.compiler.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.compiler.domain.Parameter;
import jago.compiler.domain.generic.GenericParameter;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.GenericCallableSignature;
import jago.compiler.domain.type.ArrayType;
import jago.compiler.domain.type.DecoratorType;
import jago.compiler.domain.type.Type;
import jago.compiler.domain.type.UnitType;
import jago.compiler.domain.type.generic.GenericParameterType;
import jago.compiler.domain.type.generic.GenericType;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


public final class DescriptorFactory {

    public static String getMethodDescriptor(CallableSignature signature) {
        List<Parameter> parameters = signature.getParameters();
        Type returnType = signature.getReturnType();
        return getMethodDescriptor(parameters, returnType);
    }

    private static String getMethodDescriptor(List<Parameter> parameters, Type returnType) {
        StringJoiner joiner = new StringJoiner("", "(", ")");
        for (Parameter parameter : parameters) {
            joiner.add(JvmNamingIntrinsics.getJVMDescriptor(parameter.getType()));
        }
        return joiner.toString() + JvmNamingIntrinsics.getJVMIReturnDescriptor(returnType);
    }

    public static String getMethodSignature(CallableSignature signature) {
        if (!signature.hasGenericSignature()) return null;
        StringBuilder stringBuilder = new StringBuilder();
        if (signature instanceof GenericCallableSignature) {
            List<GenericParameter> bounds = ((GenericCallableSignature) signature).getBounds();
            StringJoiner joiner = new StringJoiner("", "<", ">");
            for (GenericParameter b : bounds) {
                joiner.add("T:").add(JvmNamingIntrinsics.getJVMDescriptor(b.getConstraint()));
            }
            stringBuilder.append(joiner.toString());
        }
        StringJoiner joiner = new StringJoiner("", "(", ")");
        for (Parameter p : signature.getParameters()) {
            joiner.add(getTypeSignature(p.getType()));
        }
        stringBuilder.append(joiner.toString()).append(getReturnTypeSignature(signature.getReturnType()));
        return stringBuilder.toString();
    }

    private static String getTypeSignature(Type type) {
        if (type instanceof GenericParameterType) {
            return 'T' + type.getName() + ';';
        }
        if (type instanceof GenericType) {
            String erasedDescriptor = JvmNamingIntrinsics.getJVMDescriptor(type);
            if (erasedDescriptor.endsWith(";"))
                erasedDescriptor = erasedDescriptor.substring(0, erasedDescriptor.length() - 1);
            List<Type> genericArguments = ((GenericType) type).getGenericArguments();
            String collect = genericArguments.stream()
                    .map(DescriptorFactory::getTypeSignature)
                    .collect(Collectors.joining("", "<", ">"));
            return erasedDescriptor + collect + ';';
        }
        if (type instanceof ArrayType) {
            return '[' + getTypeSignature(((ArrayType) type).getComponentType());
        }
        if (type instanceof DecoratorType) {
            return getTypeSignature(((DecoratorType) type).getInnerType());
        }
        return JvmNamingIntrinsics.getJVMDescriptor(type);
    }

    private static String getReturnTypeSignature(Type type) {
        return type == UnitType.INSTANCE ? "V" : getTypeSignature(type);
    }
}
