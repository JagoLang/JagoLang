package jago.compiler.exception;

import jago.compiler.domain.node.expression.LocalVariable;
import jago.compiler.util.constants.Messages;

public class VariableImmutableException extends SemanticException {


    public VariableImmutableException( LocalVariable localVariable) {
        super(String.format(Messages.VARIABLE_IS_NOT_MUTABLE, localVariable.getName()));
    }
}
