package jago.domain.node.expression;

import jago.domain.type.Type;
import lombok.ToString;

import java.util.Optional;

/**
 *
 */
@ToString
public class Parameter{
    private final String name;
    private final Optional<Expression> defaultValue;
    private final Type type;

    public Parameter(String name, Type type) {
        this(name, type, Optional.empty());
    }

    public Parameter(String name, Type type, Optional<Expression> defaultValue) {
        this.type = type;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Optional<Expression> getDefaultValue() {
        return defaultValue;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (defaultValue != null ? !defaultValue.equals(parameter.defaultValue) : parameter.defaultValue != null)
            return false;
        return !(type != null ? !type.equals(parameter.type) : parameter.type != null);

    }

    @Override
    public int hashCode() {
        int result = defaultValue != null ? defaultValue.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }


}
