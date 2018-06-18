package jago.compiler.bytecodegeneration;

import jago.compiler.domain.Callable;
import jago.compiler.domain.Clazz;
import jago.compiler.domain.ClazzWrapper;
import jago.compiler.domain.CompilationUnit;

import java.util.List;

/**
 *
 */
public class BytecodeGenerator {
    public List<ClazzWrapper> generate(CompilationUnit compilationUnit) {
        List<Clazz> clazzes = compilationUnit.getClasses();
        List<Callable> callables = compilationUnit.getCallables();
        ClazzGenerator clazzGenerator = new ClazzGenerator(compilationUnit);
        return clazzGenerator.generate(clazzes, callables);
    }
}
