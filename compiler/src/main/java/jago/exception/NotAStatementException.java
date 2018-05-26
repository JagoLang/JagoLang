package jago.exception;

import java.util.List;
import java.util.stream.Collectors;

public class NotAStatementException extends SemanticException {

    public NotAStatementException(List<Integer> lines) {
        super(lines.stream().map(Object::toString)
                .collect(Collectors.joining(", ", "lines: ", " are not a valid statement")));

    }
}
