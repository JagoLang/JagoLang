package jago.exception.internal;

public class DoubleNullableException extends InternalException {

    public DoubleNullableException() {
        super("The type cannon't be nullified twice");
    }
}
