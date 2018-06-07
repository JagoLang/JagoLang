package jago.bytecodegeneration.statement;

import jago.bytecodegeneration.expression.ExpressionGenerator;
import jago.bytecodegeneration.intristics.JVMNamingIntrinsics;
import jago.bytecodegeneration.intristics.TypeOpcodesIntrinsics;
import jago.domain.node.expression.EmptyExpression;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.call.Call;
import jago.domain.node.expression.call.ConstructorCall;
import jago.domain.node.expression.call.InstanceCall;
import jago.domain.node.expression.call.StaticCall;
import jago.domain.node.statement.*;
import jago.domain.scope.LocalScope;
import jago.domain.type.ClassType;
import jago.domain.type.Type;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static jago.util.GeneratorResolver.resolveGenerationMethod;

@SuppressWarnings("unused")
public class StatementGenerator {
    private Logger logger = LogManager.getLogger(StatementGenerator.class);
    private final ExpressionGenerator expressionGenerator;
    private final MethodVisitor mv;
    private final LocalScope scope;

    public StatementGenerator(MethodVisitor mv, LocalScope scope) {
        expressionGenerator = new ExpressionGenerator(mv, scope);
        this.mv = mv;
        this.scope = scope;
    }

    public void generate(Statement statement) {
        Optional<Method> method = resolveGenerationMethod(this, statement);
        try {
            method.orElseThrow(() -> new NotImplementedException("generator for "+ statement.getClass().getName() +" not present")).invoke(this, statement);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void generateReturn(ReturnStatement returnStatement) {
        Expression expression = returnStatement.getExpression();
        Type type = expression.getType();
        if (!(expression instanceof EmptyExpression)) {
            expressionGenerator.generate(expression);
            mv.visitInsn(TypeOpcodesIntrinsics.getReturnOpcode(type));
            return;
        }
        mv.visitInsn(Opcodes.RETURN);
    }


    public void generateInstanceCall(InstanceCall call) {
        expressionGenerator.generateInstanceCall(call);
    }

    public void generateStaticCall(StaticCall call) {
        expressionGenerator.generateStaticCall(call);
    }

    public void generateConstructorCall(ConstructorCall call) {
        expressionGenerator.generateConstructorCall(call);
    }

    public void generateMethodCall(Call call) {
        expressionGenerator.generate(call);
    }

    public void generateVariableDeclaration(VariableDeclarationStatement variableDeclarationStatement) {
        Expression expression = variableDeclarationStatement.getExpression();
        Assignment assignment = new Assignment(variableDeclarationStatement);
        generateAssignment(assignment);
    }

    public void generateAssignment(Assignment assignment) {
        new AssignmentStatementGenerator(mv, expressionGenerator, scope).generate(assignment);
    }

    public void generateLogger(LogStatement logStatement) {
        Expression expression = logStatement.getExpression();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        expressionGenerator.generate(expression);
        Type type = expression.getType();
        String descriptor = "(" + JVMNamingIntrinsics.getJVMDescriptor(type) + ")V";
        ClassType owner = new ClassType("java.io.PrintStream");
        String fieldDescriptor = owner.getInternalName();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, fieldDescriptor, "println", descriptor, false);

    }

    public void generateBlock(BlockStatement blockStatement) {
        LocalScope localScope = blockStatement.getLocalScope();
        List<Statement> statements = blockStatement.getStatements();
        statements.forEach(statement -> new StatementGenerator(mv, localScope).generate(statement));
    }


}
