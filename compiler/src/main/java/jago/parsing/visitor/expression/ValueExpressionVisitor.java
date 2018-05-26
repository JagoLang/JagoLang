package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.ValueExpression;
import jago.domain.type.*;
import org.apache.commons.lang3.StringUtils;


public class ValueExpressionVisitor extends JagoBaseVisitor<ValueExpression> {

    @Override
    public ValueExpression visitValue(JagoParser.ValueContext ctx) {
        String value = ctx.getText();
        Type type;
        if (ctx.number() != null) {
            if (value.contains(".")) {
                if (value.endsWith("f") || value.endsWith("F")) {
                    type = NumericType.FLOAT;
                    value = StringUtils.stripEnd(value, "fF");
                } else {
                    type = NumericType.DOUBLE;
                }
            } else {
                if (value.endsWith("l") || value.endsWith("L")) {
                    type = NumericType.LONG;
                    value = StringUtils.stripEnd(value, "lL");
                } else {
                    type = NumericType.INT;
                }
            }
        } else if (ctx.BOOL() != null) {
            type = NumericType.BOOLEAN;
        } else if (ctx.CHAR() != null) {
            type = NumericType.CHAR;
        } else if(ctx.NULL() != null) {
            type = NullType.INSTANCE;
        }
        else {
            type = StringType.INSTANCE;
        }
        return new ValueExpression(value, type);
    }
}
