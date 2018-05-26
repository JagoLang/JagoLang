package jago.exception;

import jago.util.constants.Messages;

public class VariableRedeclarationException extends RuntimeException {
    public VariableRedeclarationException(String varName) {
        super(String.format(Messages.VARIABLE_REDECLARATION, varName));
    }
}
