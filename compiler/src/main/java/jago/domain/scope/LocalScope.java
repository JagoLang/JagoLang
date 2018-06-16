package jago.domain.scope;

import jago.domain.generic.GenericParameter;
import jago.domain.imports.Import;
import jago.domain.node.expression.LocalVariable;
import org.apache.commons.collections4.map.LinkedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalScope {
    private CompilationUnitScope compilationUnitScope;
    private final LocalScope parent;
    private final LinkedMap<String, LocalVariable> localVariables;
    private final CallableSignature callableSignature;

    public LocalScope(CompilationUnitScope compilationUnitScope,
                      LocalScope parent) {
        this.compilationUnitScope = compilationUnitScope;
        this.parent = parent;
        if (parent == null) {
            localVariables = new LinkedMap<>();
            // if this happens something went really wrong
            callableSignature = null;
        } else {
            localVariables = new LinkedMap<>(parent.localVariables);
            callableSignature = parent.callableSignature;
        }
    }

    public static LocalScope fromParent(LocalScope parent) {
        return new LocalScope(parent.compilationUnitScope, parent);
    }

    protected LocalScope(CompilationUnitScope compilationUnitScope,
                         LocalScope parent, CallableSignature signature) {
        this.compilationUnitScope = compilationUnitScope;
        this.parent = parent;
        localVariables = parent == null ? new LinkedMap<>() : new LinkedMap<>(parent.localVariables);
        callableSignature = signature;
    }


    public CallableSignature getCallable() {
        return callableSignature;
    }

    public List<GenericParameter> getAllGenericParameters() {
        LocalScope currentScope = this;
        List<GenericParameter> genericParameters = new ArrayList<>();
        while (currentScope != null) {
            if (currentScope instanceof CallableScope && currentScope.getCallable() instanceof GenericCallableSignature) {
                genericParameters.addAll(((GenericCallableSignature) currentScope.getCallable()).getBounds());
            }
            currentScope = currentScope.parent;
        }
        return genericParameters;
    }

    public LinkedMap<String, LocalVariable> getDeclaredVariables() {
        return localVariables;
    }

    public LocalVariable getLocalVariable(String name) {
        return localVariables.getOrDefault(name, null);
    }

    public boolean addLocalVariable(LocalVariable lv) {
        if (isVariableDeclared(lv.getName())) {
            return false;
        }
        localVariables.put(lv.getName(), lv);
        return true;
    }

    public boolean isVariableDeclared(String variable) {
        return localVariables.containsKey(variable);
    }

    public CallableScope getCallableScope() {
        LocalScope currentScope = this;
        while (currentScope != null) {
            if (currentScope instanceof CallableScope) {
                return (CallableScope) currentScope;
            }
            currentScope = currentScope.parent;
        }
        return null;
    }

    public String getClassName() {
        return compilationUnitScope.getClassName();
    }

    public List<Import> getImports() {
        return compilationUnitScope.getImports();
    }

    public CompilationUnitScope getCompilationUnitScope() {
        return compilationUnitScope;
    }

    public LocalScope getParent() {
        return parent;
    }


    public Map<String, LocalVariable> getLocalVariables() {
        return localVariables;
    }
}
