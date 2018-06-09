package jago.bytecodegeneration.statement;

import jago.bytecodegeneration.expression.ExpressionGenerator;
import jago.bytecodegeneration.expression.MethodCallGenerator;
import jago.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.bytecodegeneration.intristics.TypeOpcodesIntrinsics;
import jago.domain.node.expression.EmptyExpression;
import jago.domain.node.expression.Expression;
import jago.domain.node.statement.*;
import jago.domain.scope.LocalScope;
import jago.domain.type.ClassType;
import jago.domain.type.Type;
import jago.domain.type.UnitType;
import jago.util.GeneratorResolver;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class StatementGenerator {
    private Logger logger = LogManager.getLogger(StatementGenerator.class);
    private final ExpressionGenerator expressionGenerator;
    private final MethodVisitor mv;
    private final LocalScope scope;

    private static final Map<Class<?>, Method> CLASS_METHOD_MAP = GeneratorResolver
            .getAllGenerationMethods(ExpressionGenerator.class, Statement.class);

    public StatementGenerator(MethodVisitor mv, LocalScope scope) {
        expressionGenerator = new ExpressionGenerator(mv, scope);
        this.mv = mv;
        this.scope = scope;
    }

    public void generate(Statement statement) {
        Method method = CLASS_METHOD_MAP.get(statement.getClass());
        if (method == null) {
            throw new NotImplementedException("generator for " + statement.getClass().getName() + " not present");
        }
        try {
            method.invoke(this, statement);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void generateReturn(ReturnStatement returnStatement) {
        Expression expression = returnStatement.getExpression();
        Type type = expression.getType();
        if (!(expression instanceof EmptyExpression))
            expressionGenerator.generate(expression);
        if (type == UnitType.INSTANCE)
            mv.visitInsn(Opcodes.RETURN);
        else
            mv.visitInsn(TypeOpcodesIntrinsics.getReturnOpcode(type));
    }


    public void generateCallableCall(CallableCallStatement callableCallStatement) {
        new MethodCallGenerator(mv, scope, expressionGenerator).generateMethodCall(callableCallStatement.getCallableCall());
        expressionGenerator.pop(callableCallStatement.getCallableCall());
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
        String descriptor = "(" + JvmNamingIntrinsics.getJVMDescriptor(type) + ")V";
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
