package jago.bytecodegeneration.expression;

import jago.bytecodegeneration.intristics.ArithmeticIntrinsics;
import jago.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.domain.node.expression.Expression;
import jago.domain.Parameter;
import jago.domain.node.expression.arthimetic.BinaryOperation;
import jago.domain.node.expression.call.*;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.ClassType;
import jago.domain.type.NumericType;
import jago.domain.type.StringType;
import jago.exception.IllegalReferenceException;
import jago.util.DescriptorFactory;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.stream.Collectors;

public class MethodCallGenerator {
    private final MethodVisitor mv;
    private final LocalScope scope;
    private final ExpressionGenerator expressionGenerator;


    public MethodCallGenerator(MethodVisitor methodVisitor, LocalScope scope, ExpressionGenerator expressionGenerator) {
        this.scope = scope;
        this.mv = methodVisitor;
        this.expressionGenerator = expressionGenerator;
    }

    public void generateMethodCall(CallableCall call) {

        Expression owner = call.getOwner();

        String internalName = JvmNamingIntrinsics.getJVMInternalName(call.getOwnerType());

        String methodName = call.getIdentifier();

        String methodDescriptor = DescriptorFactory.getMethodDescriptor(call.getSignature());

        if (owner != null) {
            if (owner.getType() instanceof NumericType
                    && NumericType.isOperationDefinedForNonBoolean(BinaryOperation.getOperationFromMethodName(methodName))
                    && call.getArguments().size() == 1
                    && call.getArguments().get(0).getType().equals(owner.getType())) {
                Expression right = call.getArguments().get(0).getExpression();
                new ArithmeticIntrinsics(mv, expressionGenerator, scope).generate(owner, right, methodName);
                return;
            }
            expressionGenerator.generate(owner);
        }

        if (call instanceof ConstructorCall) {
            mv.visitTypeInsn(Opcodes.NEW, internalName);
            mv.visitInsn(Opcodes.DUP);
        }


        generateArguments(call);

        if (call.getOwnerType() instanceof ClassType || call.getOwnerType().equals(StringType.INSTANCE)) {
            if (call instanceof InstanceCall) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalName, methodName, methodDescriptor, false);
            } else if (call instanceof ConstructorCall) {
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", methodDescriptor, false);
            } else {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, internalName, methodName, methodDescriptor, false);
            }
        } else {
            throw new NotImplementedException("Methods for primitives are on todo list");
        }
    }

    private void generateArguments(CallableCall call) {
        generateArguments(call, call.getSignature());
    }

    private void generateArguments(Call call, CallableSignature signature) {
        List<Parameter> parameters = signature.getParameters();
        List<Argument> arguments = call.getArguments();
        // TODO: varargs
        if (arguments.isEmpty()) {
            return;
        }
        if (arguments.size() == parameters.size()) {
            Argument[] sortedArguments = sortedArguments(arguments, parameters);
            for (Argument a : sortedArguments) {
                expressionGenerator.generate(a.getExpression());
            }
        }
        if (arguments.size() > parameters.size()) {
            throw new IllegalReferenceException(String.format(Messages.CALL_ARGUMENTS_MISMATCH, call));
        }

    }

    private Argument[] sortedArguments(List<Argument> arguments, List<Parameter> parameters) {
        Argument[] sortedList = new Argument[arguments.size()];
        int i = 0;
        while (!(i >= arguments.size() || arguments.get(i) instanceof NamedArgument)) {
            sortedList[i] = arguments.get(i);
            i++;
        }
        List<String> paramNames = parameters.stream().skip(i).map(Parameter::getName).collect(Collectors.toList());
        for (int j = i; j < arguments.size(); j++) {
            sortedList[paramNames.indexOf(((NamedArgument) arguments.get(j)).getName()) + i] = arguments.get(j);
        }
        return sortedList;

    }

}

