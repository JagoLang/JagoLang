package jago.compiler.domain.node.expression.operation;

import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.node.expression.call.Argument;
import jago.compiler.domain.node.expression.call.InstanceCall;
import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.type.Type;
import jago.compiler.exception.internal.InternalException;

import java.util.List;

public class ArithmeticOperationExpression extends InstanceCall {

    public ArithmeticOperationExpression(Expression ownerCalced,
                                         CallableSignature signature,
                                         List<Argument> arguments,
                                         Type returnType) {
        super(ownerCalced, signature, arguments, returnType);
        if (arguments.size() != 1) {
            throw new InternalException("somehow binary operation is not binary");
        }
    }

    public Expression leftExpression() {
        return getOwner();
    }

    public Expression rightExpression() {
        return getArguments().get(0).getExpression();
    }
}
