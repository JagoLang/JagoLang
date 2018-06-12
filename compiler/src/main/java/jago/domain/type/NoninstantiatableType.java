package jago.domain.type;

public class NoninstantiatableType implements Type {

    private final String name;

    public NoninstantiatableType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
