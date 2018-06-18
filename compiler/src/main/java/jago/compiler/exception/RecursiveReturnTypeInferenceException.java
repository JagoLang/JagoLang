package jago.compiler.exception;

import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.util.constants.Messages;

public class RecursiveReturnTypeInferenceException extends SemanticException {

    public RecursiveReturnTypeInferenceException(CallableSignature s) {
        super(String.format(Messages.RECURSIVE_RETURN_TYPE_INFERENCE, s));
    }
}
