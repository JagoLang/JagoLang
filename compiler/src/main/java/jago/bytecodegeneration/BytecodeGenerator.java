package jago.bytecodegeneration;

import jago.domain.Callable;
import jago.domain.Clazz;
import jago.domain.ClazzWrapper;
import jago.domain.CompilationUnit;

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
