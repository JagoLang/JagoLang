package jago.compiler.exception;

import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.scope.LocalScope;

public class ReturnTypeMismatchException extends SemanticException {

    public ReturnTypeMismatchException(String s, LocalScope scope, Expression expression) {
        super(String.format(s, scope.getCallable().getReturnType().getName(), expression.getType().getName()));
    }
}
