package jago.compiler.domain;

import java.util.List;

public class Clazz {
    private final String name;
    private final List<Field> fields;

    public Clazz(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }
}
