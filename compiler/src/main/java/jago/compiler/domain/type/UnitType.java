package jago.compiler.domain.type;

public class UnitType implements Type {

    private UnitType() {}

    public static UnitType INSTANCE = new UnitType();

    @Override
    public String getName() {
        return "Unit";
    }

}
