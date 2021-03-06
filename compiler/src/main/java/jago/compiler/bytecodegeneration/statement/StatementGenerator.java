package jago.compiler.bytecodegeneration.statement;

import jago.compiler.bytecodegeneration.expression.ExpressionGenerator;
import jago.compiler.bytecodegeneration.expression.MethodCallGenerator;
import jago.compiler.bytecodegeneration.intristics.ArrayIntrinsics;
import jago.compiler.bytecodegeneration.intristics.JvmNamingIntrinsics;
import jago.compiler.bytecodegeneration.intristics.TypeOpcodesIntrinsics;
import jago.compiler.domain.node.expression.EmptyExpression;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.call.Argument;
import jago.compiler.domain.node.statement.*;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.domain.type.*;
import jago.compiler.util.GeneratorResolver;
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
            .getAllGenerationMethods(StatementGenerator.class, Statement.class);

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

    public void generateIndexerAssignment(IndexerAssignmentStatement indexerAssignment) {
        Expression owner = indexerAssignment.getOwner();
        List<Argument> arguments = indexerAssignment.getArguments();
        if (owner instanceof CompositeType) {
            CompositeType arrayType = (CompositeType) owner;
            if (arguments.size() == 2
                    && arguments.get(0).getType().equals(NumericType.INT)
                    && arguments.get(1).getType().erased().equals(arrayType.getComponentType())) {
                expressionGenerator.generate(owner);
                for (Argument a : arguments) {
                    expressionGenerator.generate(a.getExpression());
                }
                mv.visitInsn(ArrayIntrinsics.getAStoreTypeCode(arrayType));
            }
        } else {
            new MethodCallGenerator(mv, scope, expressionGenerator).generateMethodCall(indexerAssignment.getCallableCall());
        }

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
        String fieldDescriptor = JvmNamingIntrinsics.getJVMInternalName(owner);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, fieldDescriptor, "println", descriptor, false);
    }

    public void generateBlock(BlockStatement blockStatement) {
        LocalScope localScope = blockStatement.getLocalScope();
        List<Statement> statements = blockStatement.getStatements();
        statements.forEach(statement -> new StatementGenerator(mv, localScope).generate(statement));
    }

}
