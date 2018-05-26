package jago.bytecodegeneration;

import jago.domain.Clazz;
import jago.domain.ClazzWrapper;
import jago.domain.CompilationUnit;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.V1_8;

public class DataGenerator {

    private static int CLASS_VERSION = V1_8;
    private String parentName;
    private CompilationUnit compilationUnit;
    public DataGenerator(String parentName, CompilationUnit compilationUnit) {
        this.parentName = parentName;
        this.compilationUnit = compilationUnit;
    }

    public List<ClazzWrapper> generate(List<Clazz> clazzes) {
        return clazzes.stream().map(this::generateClazz).collect(Collectors.toList());
    }

    private ClazzWrapper generateClazz(Clazz clazz) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        String fullClazzName = String.format("%s$%s", compilationUnit.getCompilationUnitScope().getClassName(), clazz.getName());
        classWriter.visit(CLASS_VERSION, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, fullClazzName, null, "java/lang/Object", null);
        classWriter.visitEnd();
        return new ClazzWrapper(String.format("%s$%s", parentName, clazz.getName()), classWriter.toByteArray());
    }
}
