package jago.compiler.domain;

import jago.antlr.JagoParser;
import jago.compiler.domain.node.expression.Expression;
import jago.compiler.domain.type.Type;
import jago.compiler.parsing.visitor.expression.ExpressionVisitor;

import java.util.Objects;


//TODO: maybe make this class fully immutable
public class Parameter {
    private final String name;
    private Expression defaultValue;
    private final Type type;
    private final JagoParser.ExpressionContext defaultValueUnparsed;

    public Parameter(String name, Type type) {
        this(name, type, null);
    }


    public Parameter(String name, Type type, JagoParser.ExpressionContext defaultValue) {
        this.type = type;
        this.name = name;
        this.defaultValueUnparsed = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Expression getDefaultValue() {
        return defaultValue;
    }

    public Type getType() {
        return type;
    }

    public boolean isGeneric() {
        return type.isGeneric();
    }

    public boolean hasDefaultValue() {
        return defaultValueUnparsed != null;
    }

    public void setDefaultValue(ExpressionVisitor visitor) {
        this.defaultValue = defaultValueUnparsed.accept(visitor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return Objects.equals(name, parameter.name)
                && Objects.equals(defaultValue, parameter.defaultValue)
                && Objects.equals(type, parameter.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultValue, type);
    }

    @Override
    public String toString() {
        return "Parameter: " +
                name + ": " + type +
                "= " + defaultValue;
    }
}
