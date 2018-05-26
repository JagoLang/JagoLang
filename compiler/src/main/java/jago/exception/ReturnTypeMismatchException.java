package jago.exception;

import jago.domain.node.expression.Expression;
import jago.domain.scope.LocalScope;

public class ReturnTypeMismatchException extends SemanticException {

    public ReturnTypeMismatchException(String s, LocalScope scope, Expression expression) {
        super(String.format(s, scope.getCallable().getReturnType().getName(), expression.getType().getName()));
    }
}
