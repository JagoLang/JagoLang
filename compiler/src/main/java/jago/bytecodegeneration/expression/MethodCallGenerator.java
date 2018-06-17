package jago.bytecodegeneration.expression;

import jago.bytecodegeneration.intristics.ArithmeticIntrinsics;
import jago.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.bytecodegeneration.intristics.NullableIntrinsics;
import jago.domain.Parameter;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.arthimetic.BinaryOperation;
import jago.domain.node.expression.call.*;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.NumericType;
import jago.domain.type.Type;
import jago.domain.type.generic.GenericParameterType;
import jago.exception.IllegalReferenceException;
import jago.util.ArgumentUtils;
import jago.util.DescriptorFactory;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

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

        Type ownerType = call.getOwnerType();
        String internalName = JvmNamingIntrinsics.getJVMInternalName(ownerType);

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

        // TODO: more concise decision making
        if (!(ownerType instanceof NumericType)) {
            if (call instanceof InstanceCall) {
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalName, methodName, methodDescriptor, false);
            } else if (call instanceof ConstructorCall) {
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, internalName, "<init>", methodDescriptor, false);
            } else {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, internalName, methodName, methodDescriptor, false);
            }
            if (call.getSignature().getReturnType() instanceof GenericParameterType) {
                Type callType = call.getType();
                mv.visitTypeInsn(Opcodes.CHECKCAST, JvmNamingIntrinsics.getJVMInternalName(callType));
                if (callType instanceof NumericType) {
                    NullableIntrinsics.generateNumericNullableBackwardsConversion(((NumericType) callType), mv);
                }
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

        if (arguments.isEmpty()) {
            return;
        }
        if (arguments.size() < parameters.size()) {
            //TODO: synthetic bridge delegation
        }
        if (arguments.size() == parameters.size()) {
            List<Argument> sortedArguments = ArgumentUtils.sortedArguments(arguments, parameters);
            for (int i = 0; i < sortedArguments.size(); i++) {
                Argument a = sortedArguments.get(i);
                expressionGenerator.generate(a.getExpression());
                if (a.getType() instanceof NumericType && !a.getType().equals(parameters.get(i).getType())) {
                    NullableIntrinsics.generateNumericNullableConversion(((NumericType) a.getType()), mv);
                }
            }
        }
        if (arguments.size() > parameters.size()) {
            // TODO: varargs
            throw new IllegalReferenceException(String.format(Messages.CALL_ARGUMENTS_MISMATCH, call));
        }

    }


}

