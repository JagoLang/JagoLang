package jago.domain;

import jago.JagoParser;
import jago.domain.type.CompositeType;
import jago.domain.type.DecoratorType;
import jago.domain.type.Type;
import jago.exception.TypeMismatchException;

public class VarargParameter extends Parameter {

    public VarargParameter(String name, Type type) {
        super(name, type);
    }

    public VarargParameter(String name, Type type, JagoParser.ExpressionContext defaultValue) {
        super(name, type, defaultValue);
    }

    public Type getComponentType() {
        Type type = getType();
        if (type instanceof CompositeType) {
            return ((CompositeType) type).getComponentType();
        }
        if (type instanceof DecoratorType) {
            Type innerType = ((DecoratorType) type).getInnerType();
            if (innerType instanceof CompositeType) {
                return ((CompositeType) innerType).getComponentType();
            }
        }
        throw new TypeMismatchException();
    }

}
