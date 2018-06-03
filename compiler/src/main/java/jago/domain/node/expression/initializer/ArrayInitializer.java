package jago.domain.node.expression.initializer;

import jago.domain.node.expression.AbstractExpression;
import jago.domain.node.expression.Expression;
import jago.domain.type.ArrayType;
import jago.domain.type.Type;
import jago.exception.TypeMismatchException;

import java.util.List;

public class ArrayInitializer extends AbstractExpression {

    private final List<Expression> expressionList;

    private final ArrayType type;
    public ArrayInitializer(List<Expression> expressionList) {
        //TODO permit nullable coalescing
        if (expressionList.stream().map(Expression::getType).distinct().limit(2).count() > 1) {
            throw new TypeMismatchException();
        }
        this.expressionList = expressionList;
        type = new ArrayType(expressionList.get(0).getType());
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    @Override
    public Type getType() {
        return type;
    }
}
