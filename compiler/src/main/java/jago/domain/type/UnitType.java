package jago.domain.type;

public class UnitType implements Type {


    public static UnitType INSTANCE = new UnitType();

    @Override
    public String getName() {
        return "Unit";
    }

    @Override
    public Class<?> getTypeClass() {
        return void.class;
    }


    @Override
    public String getDescriptor() {
        //TODO only when it is a return type, otherwise reference Unit from stdlib
        return "V";
    }

    @Override
    public String getInternalName() {
        return "V";
    }
}
