package jago.domain.type;

import jago.domain.node.expression.Parameter;
import jago.domain.scope.CallableSignature;

import java.util.Collections;


public class AnyType implements Type {


    public static AnyType INSTANCE = new AnyType();

    @Override
    public String getName() {
        return "Any";
    }

    @Override
    public Class<?> getTypeClass() {
        return Object.class;
    }


    @Override
    public CallableSignature getEqualsOperation() {
        return new CallableSignature(this.getName(),
                "equals",
                Collections.singletonList(new Parameter("other", this)),
                BuiltInType.BOOLEAN);

    }


    @Override
    public String getDescriptor() {
        return "Ljava/lang/Object;";
    }

    @Override
    public String getInternalName() {
        return "java/lang/Object";
    }



}
