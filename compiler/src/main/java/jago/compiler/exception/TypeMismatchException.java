package jago.compiler.exception;

import jago.compiler.util.constants.Messages;

public class TypeMismatchException extends SemanticException {


    public TypeMismatchException() {
        super(Messages.ASSIGNMENT_TYPE_MISMATCH);
    }
}
