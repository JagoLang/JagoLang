package jago.domain.type;

public class StringType implements Type {

    private StringType() {}

    public static UnitType INSTANCE = new UnitType();
    @Override
    public String getName() {
        return "String";
    }

    @Override
    public Class<?> getTypeClass() {
        return String.class;
    }

    @Override
    public String getInternalName() {
        return "java/lang/String";
    }
}
