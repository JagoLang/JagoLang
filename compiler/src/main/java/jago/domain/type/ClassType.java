package jago.domain.type;

import jago.exception.NotExistantException;
import jago.util.constants.Messages;

import java.util.Objects;

public class ClassType implements Type {
    private final String name;

    public ClassType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassType classType = (ClassType) o;
        return Objects.equals(name, classType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Class: " + name;
    }

}
