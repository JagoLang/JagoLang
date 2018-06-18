package jago.compiler.domain.scope;


import jago.compiler.domain.Parameter;
import jago.compiler.domain.node.expression.LocalVariable;
import jago.compiler.domain.node.statement.ReturnStatement;
import jago.compiler.domain.type.Type;
import jago.compiler.domain.type.UnitType;
import jago.compiler.exception.ReturnTypeInferenceFailException;
import jago.compiler.exception.VariableRedeclarationException;

import java.util.ArrayList;
import java.util.List;

public class CallableScope extends LocalScope {



    private final List<ReturnStatement> returnStatements;
    public CallableScope(CompilationUnitScope compilationUnitScope, LocalScope parent, CallableSignature signature) {
        super(compilationUnitScope, parent, signature);
        returnStatements = new ArrayList<>();
    }


    public void addParameters(List<Parameter> parameters) {
        parameters.forEach(p -> {
                    if (!addLocalVariable(new LocalVariable(p.getName(), p.getType())))
                        throw new VariableRedeclarationException(p.getName());
                }
        );
    }

    public void addReturnStatement(ReturnStatement rs) {
        returnStatements.add(rs);
    }

    public Type resolveReturnType() {
        if (returnStatements.isEmpty()) {
            return UnitType.INSTANCE;
        }
        boolean typesAreAllTheSame = returnStatements.stream().map(ReturnStatement::getType).distinct().count() == 1;
        if (typesAreAllTheSame) {
            return returnStatements.get(0).getType();
        }
        throw new ReturnTypeInferenceFailException(getCallable().toString());
    }

}
