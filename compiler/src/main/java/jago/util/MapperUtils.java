package jago.util;

import jago.domain.node.expression.Expression;
import jago.domain.type.Type;

import java.util.ArrayList;
import java.util.List;

public final class MapperUtils {

    public static List<Type> expressionsToTypes(List<Expression> expressions) {
        List<Type> list = new ArrayList<>();
        for (Expression e : expressions) {
            list.add(e.getType());
        }
        return list;
    }
}
