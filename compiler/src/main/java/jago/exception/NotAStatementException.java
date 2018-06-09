package jago.exception;

import java.util.List;
import java.util.stream.Collectors;

public class NotAStatementException extends SemanticException {

    public NotAStatementException(String s) {
        super(s);
    }
}
