package jago.compiler.domain.type;

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

}
