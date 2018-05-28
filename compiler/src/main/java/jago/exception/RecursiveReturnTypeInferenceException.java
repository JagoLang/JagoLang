package jago.exception;

import jago.domain.scope.CallableSignature;
import jago.util.constants.Messages;

public class RecursiveReturnTypeInferenceException extends SemanticException {

    public RecursiveReturnTypeInferenceException(CallableSignature s) {
        super(String.format(Messages.RECURSIVE_RETURN_TYPE_INFERENCE, s));
    }
}
