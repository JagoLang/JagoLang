package jago.compiler.bytecodegeneration.intristics;

import jago.compiler.bytecodegeneration.expression.ExpressionGenerator;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.operation.ArithmeticOperation;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.MethodVisitor;

public class ArithmeticIntrinsics {


    private final MethodVisitor mv;
    private final ExpressionGenerator expressionGenerator;

    public ArithmeticIntrinsics(MethodVisitor mv, ExpressionGenerator expressionGenerator) {
        this.mv = mv;
        this.expressionGenerator = expressionGenerator;
    }

    public void generate(Expression left, Expression right, String methodName) {
        expressionGenerator.generate(left);
        expressionGenerator.generate(right);

        JvmTypeSpecificInformation jvmTypeSpecificInformation = JvmTypeSpecificInformation.of(left.getType());
        ArithmeticOperation operation = ArithmeticOperation.getOperationFromMethodName(methodName);
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
                mv.visitInsn(jvmTypeSpecificInformation.getRem());
                break;
        }
    }
}
