package jago.bytecodegeneration;

import jago.bytecodegeneration.statement.StatementGenerator;
import jago.domain.Callable;
import jago.domain.node.expression.EmptyExpression;
import jago.domain.node.statement.BlockStatement;
import jago.domain.node.statement.ReturnStatement;
import jago.domain.node.statement.Statement;
import jago.domain.scope.LocalScope;
import jago.domain.type.UnitType;
import jago.util.DescriptorFactory;
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
        String descriptor = DescriptorFactory.getMethodDescriptor(callable);
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                name, descriptor, null, null);
        mv.visitCode();

        LocalScope scope = ((BlockStatement) callable.getStatement()).getLocalScope();
        StatementGenerator statementGenerator = new StatementGenerator(mv, scope);
        statementGenerator.generate(callable.getStatement());
        appendReturnIfNotExists(callable,statementGenerator);
        // I think we don't need this -@hunter04d
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
