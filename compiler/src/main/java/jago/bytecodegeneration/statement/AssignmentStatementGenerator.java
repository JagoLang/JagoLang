package jago.bytecodegeneration.statement;

import jago.bytecodegeneration.expression.ExpressionGenerator;
import jago.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.bytecodegeneration.intristics.LocalVariableIntrinsics;
import jago.bytecodegeneration.intristics.NullableIntrinsics;
import jago.bytecodegeneration.intristics.TypeOpcodesIntrinsics;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.statement.Assignment;
import jago.domain.scope.LocalScope;
import jago.domain.type.NullType;
import jago.domain.type.NullableType;
import jago.domain.type.NumericType;
import jago.domain.type.Type;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class AssignmentStatementGenerator {

    private final MethodVisitor methodVisitor;
    private final ExpressionGenerator expressionGenerator;
    private final LocalScope scope;

    public AssignmentStatementGenerator(MethodVisitor methodVisitor,
                                        ExpressionGenerator expressionGenerator,
                                        LocalScope scope) {
        this.methodVisitor = methodVisitor;
        this.expressionGenerator = expressionGenerator;
        this.scope = scope;
    }

    public void generate(Assignment assignment) {
        String varName = assignment.getIdentifier();

        Expression expression = assignment.getExpression();

        expressionGenerator.generate(expression);

        Type type = expression.getType();

        if (scope.isVariableDeclared(varName)) {
            LocalVariable localVariable = scope.getLocalVariables().get(varName);
            if (localVariable.isNullable()
                    && !expression.getType().equals(NullType.INSTANCE)
                    && expression.getType() instanceof NumericType) {
                NullableIntrinsics.generateNumericNullableConversion((NumericType) expression.getType(), methodVisitor);
            }
            // bypass the cast if we are using intrinsics
            else {
                castIfNecessary(type, localVariable.getType());
            }
            methodVisitor.visitVarInsn(TypeOpcodesIntrinsics.getStoreOpcode(localVariable.getType()), LocalVariableIntrinsics.getVariableIndex(varName, scope));
            // fail fast
            // TODO: add a proper fail fast resolving
            if (!localVariable.isNullable() && !(localVariable.getType() instanceof NumericType)) {

                Label l = new Label();
                methodVisitor.visitVarInsn(ALOAD, LocalVariableIntrinsics.getVariableIndex(varName, scope));
                methodVisitor.visitJumpInsn(IFNONNULL, l);
                methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException");
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "()V", false);
                methodVisitor.visitInsn(ATHROW);
                methodVisitor.visitLabel(l);
            }
        }
        /*Field field = scope.getField(varName);
        String descriptor = field.getType().getDescriptor();
        methodVisitor.visitVarInsn(Opcodes.ALOAD,0);
        expression.accept(expressionGenerator);
        castIfNecessary(type, field.getType());
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD,field.getOwnerInternalName(),field.getName(),descriptor);*/
    }

    private void castIfNecessary(Type expressionType, Type variableType) {
        String internalName = JvmNamingIntrinsics.getJVMInternalName(variableType);
        if (!expressionType.equals(variableType) && !NullableType.isNullableOf(variableType, expressionType)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, internalName);
        } else if (variableType instanceof NullableType && expressionType.equals(NullType.INSTANCE)) {
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, internalName);
        }
    }
}
