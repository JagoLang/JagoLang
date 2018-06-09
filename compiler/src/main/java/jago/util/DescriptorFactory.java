package jago.util;

import jago.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.domain.Callable;
import jago.domain.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.List;
import java.util.StringJoiner;


public final class DescriptorFactory {

    public static String getMethodDescriptor(Callable callable) {
        List<Parameter> parameters = callable.getParameters();
        Type returnType = callable.getReturnType();
        return getMethodDescriptor(parameters, returnType);
    }

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
}
