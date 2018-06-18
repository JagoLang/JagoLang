package jago.compiler.bytecodegeneration;

import jago.compiler.domain.Callable;
import jago.compiler.domain.Clazz;
import jago.compiler.domain.ClazzWrapper;
import jago.compiler.domain.CompilationUnit;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ClazzGenerator {
    private static final int CLASS_VERSION = V1_8;
    private final ClassWriter classWriter;
    private CompilationUnit compilationUnit;

    public ClazzGenerator(CompilationUnit compilationUnit) {
        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        this.compilationUnit = compilationUnit;
    }

    public List<ClazzWrapper> generate(List<Clazz> clazzes, List<Callable> callables) {

        String fullClazzName = compilationUnit.getCompilationUnitScope().getInternalName();
        String className = compilationUnit.getCompilationUnitScope().getClassName();

        classWriter.visit(52, ACC_PUBLIC + Opcodes.ACC_SUPER, fullClazzName,
                null, "java/lang/Object", null);

        for (Clazz c : clazzes) {
            classWriter.visitInnerClass(fullClazzName + "$" + c.getName(), fullClazzName, c.getName(), ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
        }


        DataGenerator dataGenerator = new DataGenerator(className, compilationUnit);

        List<ClazzWrapper> wrappers = dataGenerator.generate(clazzes);


        CallableGenerator callableGenerator = new CallableGenerator(classWriter);

        callableGenerator.generate(callables);

        classWriter.visitEnd();
        wrappers.add(new ClazzWrapper(className, classWriter.toByteArray()));

        return wrappers;
    }
}