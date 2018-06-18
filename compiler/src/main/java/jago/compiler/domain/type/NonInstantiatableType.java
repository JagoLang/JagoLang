package jago.compiler.domain.type;

import java.util.Objects;

public class NonInstantiatableType implements Type {

    private final String name;

    public NonInstantiatableType(String name) {
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
        NonInstantiatableType that = (NonInstantiatableType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
