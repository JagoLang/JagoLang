package jago.domain.type;

/**
 * Special null Type with absence of an anything meaningful
 */
public class NullType implements Type {

    private NullType() {}

    public static NullType INSTANCE = new NullType();
    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public String getInternalName() {
        return null;
    }
}
