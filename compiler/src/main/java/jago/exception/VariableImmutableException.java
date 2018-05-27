package jago.exception;

import jago.domain.node.expression.LocalVariable;
import jago.util.constants.Messages;

public class VariableImmutableException extends SemanticException {


    public VariableImmutableException( LocalVariable localVariable) {
        super(String.format(Messages.VARIABLE_IS_NOT_MUTABLE, localVariable.getName()));
    }
}
