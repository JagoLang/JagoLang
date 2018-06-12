package jago.domain.type;

public class StringType implements Type {

    private StringType() {}

    public static StringType INSTANCE = new StringType();
    @Override
    public String getName() {
        return "String";
    }

}
