package jago.util;

import jago.bytecodegeneration.intristics.JVMNamingIntrinsics;
import jago.domain.Callable;
import jago.domain.node.expression.Parameter;
import jago.domain.scope.CallableSignature;
import jago.domain.type.Type;

import java.util.Collection;
import java.util.StringJoiner;


public final class DescriptorFactory {

    public static String getMethodDescriptor(Callable callable) {
        Collection<Parameter> parameters = callable.getParameters();
        Type returnType = callable.getReturnType();
        return getMethodDescriptor(parameters, returnType);
    }

    public static String getMethodDescriptor(CallableSignature signature) {
        Collection<Parameter> parameters = signature.getParameters();
        Type returnType = signature.getReturnType();
        return getMethodDescriptor(parameters, returnType);
    }

    private static String getMethodDescriptor(Collection<Parameter> parameters, Type returnType) {
        StringJoiner joiner = new StringJoiner("", "(", ")");
        for (Parameter parameter : parameters) {
            joiner.add(JVMNamingIntrinsics.getJVMDescriptor(parameter.getType()));
        }
        return joiner.toString() + JVMNamingIntrinsics.getJVMIReturnDescriptor(returnType);
    }
}
