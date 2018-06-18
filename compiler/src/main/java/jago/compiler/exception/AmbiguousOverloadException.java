package jago.compiler.exception;

public class AmbiguousOverloadException extends SemanticException {
    public AmbiguousOverloadException(String s) {
        super(s);
    }
}
