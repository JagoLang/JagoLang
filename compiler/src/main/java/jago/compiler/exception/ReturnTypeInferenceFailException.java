package jago.compiler.exception;

import jago.compiler.util.constants.Messages;

public class ReturnTypeInferenceFailException extends SemanticException {
    public ReturnTypeInferenceFailException(String s) {
        super(String.format(Messages.RETURN_TYPE_INFERENCE_FAILED, s));
    }
}
