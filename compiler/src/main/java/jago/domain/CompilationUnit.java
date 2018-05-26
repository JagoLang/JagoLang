package jago.domain;

import jago.domain.scope.CompilationUnitScope;

import java.util.List;

public class CompilationUnit {

    private CompilationUnitScope compilationUnitScope;
    private final List<Clazz> classes;
    private final List<Callable> callables;

    public CompilationUnit(List<Clazz> classes, List<Callable> callables, CompilationUnitScope compilationUnitScope) {
        this.classes = classes;
        this.callables = callables;
        this.setCompilationUnitScope(compilationUnitScope);
    }


    public List<Clazz> getClasses() {
        return classes;
    }

    public List<Callable> getCallables() {
        return callables;
    }

    public CompilationUnitScope getCompilationUnitScope() {
        return compilationUnitScope;
    }

    public void setCompilationUnitScope(CompilationUnitScope compilationUnitScope) {
        this.compilationUnitScope = compilationUnitScope;
    }
}
