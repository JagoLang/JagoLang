package jago.compiler.domain.node.expression.initializer;

import jago.compiler.domain.node.expression.AbstractExpression;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.type.*;
import jago.compiler.exception.TypeMismatchException;

import java.util.List;

public class ArrayInitializer extends AbstractExpression {

    private final List<Expression> expressionList;
    private final CompositeType type;

    public ArrayInitializer(List<Expression> expressionList) {
        //TODO permit nullable coalescing or figure out the base type for all of expressions
        if (expressionList.stream().map(Expression::getType).distinct().limit(2).count() > 1) {
            throw new TypeMismatchException();
        }
        this.expressionList = expressionList;
        Type type = expressionList.get(0).getType();
        this.type = type instanceof NumericType
                ? new PrimitiveArrayType(((NumericType) type))
                : new ArrayType(type);
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    @Override
    public CompositeType getType() {
        return type;
    }
}
