package jago.compiler.exception;

import jago.compiler.util.constants.Messages;

public class VariableRedeclarationException extends SemanticException {
    public VariableRedeclarationException(String varName) {
        super(String.format(Messages.VARIABLE_REDECLARATION, varName));
    }
}
