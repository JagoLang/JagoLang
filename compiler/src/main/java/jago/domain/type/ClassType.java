package jago.domain.type;

import jago.exception.NotExistantException;
import jago.util.constants.Messages;
import lombok.ToString;

@ToString
public class ClassType implements Type {
    private final String name;

    public ClassType(String name) {
        this.name = name;
    }

    public static final Type STRING = new ClassType("java.lang.String");


    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getTypeClass() {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new NotExistantException(String.format(Messages.CLASS_DONT_EXIST, name));
        }
    }


    @Override
    public String getInternalName() {
        return name.replace('.', '/');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassType classType = (ClassType) o;

        return !(name != null ? !name.equals(classType.name) : classType.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
