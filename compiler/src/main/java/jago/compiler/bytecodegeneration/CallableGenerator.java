package jago.compiler.bytecodegeneration;

import jago.compiler.bytecodegeneration.statement.StatementGenerator;
import jago.compiler.domain.Callable;
import jago.compiler.domain.node.expression.EmptyExpression;
import jago.compiler.domain.node.statement.BlockStatement;
import jago.compiler.domain.node.statement.ReturnStatement;
import jago.compiler.domain.node.statement.Statement;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.LocalScope;
import jago.compiler.domain.type.UnitType;
import jago.compiler.util.DescriptorFactory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class CallableGenerator {
    private final ClassWriter classWriter;

    public CallableGenerator(ClassWriter classWriter) {
        this.classWriter = classWriter;
    }

    private void generateOne(Callable callable) {
        String name = callable.getName();
        CallableSignature callableSignature = callable.getCallableSignature();
        String descriptor = DescriptorFactory.getMethodDescriptor(callableSignature);
        String methodSignature = DescriptorFactory.getMethodSignature(callableSignature);
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                name,
                descriptor,
                methodSignature,
                null);
        mv.visitCode();

        LocalScope scope = ((BlockStatement) callable.getStatement()).getLocalScope();
        StatementGenerator statementGenerator = new StatementGenerator(mv, scope);
        statementGenerator.generate(callable.getStatement());
        appendReturnIfNotExists(callable, statementGenerator);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void appendReturnIfNotExists(Callable callable, StatementGenerator statementScopeGenrator) {
        boolean isLastStatementReturn = false;
        BlockStatement block = (BlockStatement) callable.getStatement();
        if (!block.getStatements().isEmpty()) {
            Statement lastStatement = block.getStatements().get(block.getStatements().size() - 1);
            isLastStatementReturn = lastStatement instanceof ReturnStatement;
        }
        if (!isLastStatementReturn && callable.getReturnType().equals(UnitType.INSTANCE)) {
            ReturnStatement returnStatement = new ReturnStatement(new EmptyExpression(callable.getReturnType()));
            statementScopeGenrator.generateReturn(returnStatement);
        }
    }

    public void generate(List<Callable> callables) {
        callables.forEach(this::generateOne);
    }
}
