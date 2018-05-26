package jago.bytecodegeneration.expression;

import jago.bytecodegeneration.intristics.ArithmeticIntrinsics;
import jago.bytecodegeneration.intristics.JVMNamingIntrinsics;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.Parameter;
import jago.domain.node.expression.arthimetic.BinaryOperation;
import jago.domain.node.expression.calls.Call;
import jago.domain.node.expression.calls.CallableCall;
import jago.domain.node.expression.calls.ConstructorCall;
import jago.domain.node.expression.calls.InstanceCall;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.exception.IllegalReferenceException;
import jago.util.DescriptorFactory;
import jago.util.SignatureResolver;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

        String internalName = JVMNamingIntrinsics.getJVMInternalName(call.getOwnerType());

        String methodName = call.getIdentifier();

        String methodDescriptor = DescriptorFactory.getMethodDescriptor(call.getSignature());

        if (owner != null) {
            if (owner.getType() instanceof NumericType
                    && NumericType.isOperationDefinedForNonBoolean(BinaryOperation.getOperationFromMethodName(methodName))
                    && call.getArguments().size() == 1
                    && call.getArguments().get(0).getType().equals(owner.getType()))  {
                Expression right = call.getArguments().get(0);
                new ArithmeticIntrinsics(mv, expressionGenerator, scope).generateInartistic(owner, right, methodName);
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


    public CallableSignature getConstructorCallSignature(String className, List<Expression> arguments) {
        boolean isDifferentThanCurrentClass = !className.equals(scope.getClassName());
        if (isDifferentThanCurrentClass) {
            List<Type> argumentsTypes = arguments.stream().map(Expression::getType).collect(toList());
            return SignatureResolver.getConstructorSignature(className, argumentsTypes, scope)
                    .orElseThrow(() -> new IllegalReferenceException(
                            String.format(Messages.METHOD_DONT_EXIST, className, arguments)
                    ));
        }
        return getMethodCallSignature(null, scope.getClassName(), arguments);
    }


    private void generateArguments(CallableCall call) {
        CallableSignature signature;
        if (call instanceof ConstructorCall) {
            signature = getConstructorCallSignature(call.getIdentifier(), call.getArguments());
        } else {
            signature = getMethodCallSignature(call.getOwnerType(), call.getIdentifier(), call.getArguments());
        }
        generateArguments(call, signature);
    }

    // why is this here?
    public CallableSignature getMethodCallSignature(Type owner, String methodName, List<Expression> arguments) {

        boolean isDifferentThanCurrentClass = owner != null && !owner.getName().equals(scope.getCompilationUnitScope().getClassName());

        if (isDifferentThanCurrentClass) {
            List<Type> argumentsTypes = arguments.stream().map(Expression::getType).collect(toList());
            return SignatureResolver.getMethodSignatureForInstanceCall(owner, methodName, argumentsTypes, scope)
                    .orElseThrow(() -> new IllegalReferenceException(
                            String.format(Messages.METHOD_DONT_EXIST, methodName, arguments)
                    ));
        }
        return getMethodCallSignature(methodName, arguments);
    }

    private CallableSignature getMethodCallSignature(String identifier, List<Expression> arguments) {
        if (identifier.equals("super")) {
            // call to super, this is safe to ignore
            return new CallableSignature("super", "super", Collections.emptyList(), BuiltInType.VOID);
        }
        return scope.getCompilationUnitScope()
                .getCallableSignatures()
                .stream()
                .filter(signature -> signature.matches(identifier, arguments))
                .findFirst()
                .orElseThrow(() -> new IllegalReferenceException(identifier + arguments));
    }

    private void generateArguments(Call call, CallableSignature signature) {
        List<Parameter> parameters = signature.getParameters();
        List<Expression> arguments = call.getArguments();
        if (arguments.size() > parameters.size()) {
            throw new IllegalReferenceException(String.format(Messages.CALL_ARGUMENTS_MISMATCH, call));
        }

        arguments.forEach(expressionGenerator::generate);

    }

       /* public void generate(ConstructorCall constructorCall) {
        FunctionSignature signature = scope.getConstructorCallSignature(constructorCall.getIdentifier(), constructorCall.getArguments());
        String ownerDescriptor = new ClassType(signature.getName()).getDescriptor();
        methodVisitor.visitTypeInsn(Opcodes.NEW, ownerDescriptor);
        methodVisitor.visitInsn(Opcodes.DUP);
        String methodDescriptor = DescriptorFactory.getMethodDescriptor(signature);
        generateArguments(constructorCall, signature);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, ownerDescriptor, "<init>", methodDescriptor, false);
    }*/

    /*public void generate(SuperCall superCall) {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        generateArguments(superCall);
        String ownerDescriptor = scope.getSuperClassInternalName();
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, ownerDescriptor, "<init>", "()V" *//*TODO Handle super calls with arguments*//*, false);
    }*/
}

