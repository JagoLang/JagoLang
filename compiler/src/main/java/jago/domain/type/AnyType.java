package jago.domain.type;

public class AnyType implements Type {


    public static AnyType INSTANCE = new AnyType();

    @Override
    public String getName() {
        return "Any";
    }

}
