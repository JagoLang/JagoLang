package jago.exception;

public abstract class SemanticException extends RuntimeException {

    SemanticException(String s) {
        super(s);
    }
}
