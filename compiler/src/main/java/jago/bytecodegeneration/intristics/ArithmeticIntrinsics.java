package jago.bytecodegeneration.intristics;

import jago.bytecodegeneration.expression.ExpressionGenerator;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.arthimetic.BinaryOperation;
import jago.domain.scope.LocalScope;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.MethodVisitor;

public class ArithmeticIntrinsics {


    private final MethodVisitor mv;
    private final ExpressionGenerator expressionGenerator;
    private final LocalScope scope;

    public ArithmeticIntrinsics(MethodVisitor mv, ExpressionGenerator expressionGenerator, LocalScope scope) {
        this.mv = mv;
        this.expressionGenerator = expressionGenerator;
        this.scope = scope;
    }

    public void generate(Expression left, Expression right, String methodName) {
        expressionGenerator.generate(left);
        expressionGenerator.generate(right);

        JVMTypeSpecificInformation jvmTypeSpecificInformation = JVMTypeSpecificInformation.of(left.getType());
        BinaryOperation operation = BinaryOperation.getOperationFromMethodName(methodName);
        switch (operation) {
            case SUB:
                mv.visitInsn(jvmTypeSpecificInformation.getSubstract());
                break;
            case ADD:
                mv.visitInsn(jvmTypeSpecificInformation.getAdd());
                break;
            case MUL:
                mv.visitInsn(jvmTypeSpecificInformation.getMultiply());
                break;
            case DIV:
                mv.visitInsn(jvmTypeSpecificInformation.getDivide());
                break;
            case POW:
                throw new NotImplementedException("powerOf is not implemented");
            case REM:
                mv.visitInsn(jvmTypeSpecificInformation.getMultiply());
                break;
        }
    }
}
