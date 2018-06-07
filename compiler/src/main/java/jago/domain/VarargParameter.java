package jago.domain;

import jago.JagoParser;
import jago.domain.type.CompositeType;
import jago.domain.type.Type;

public class VarargParameter extends Parameter {

    public VarargParameter(String name, CompositeType type) {
        super(name, type);
    }

    public VarargParameter(String name, CompositeType type, JagoParser.ExpressionContext defaultValue) {
        super(name, type, defaultValue);
    }

    public Type getComponentType() {
        return ((CompositeType) getType()).getComponentType();
    }

}
