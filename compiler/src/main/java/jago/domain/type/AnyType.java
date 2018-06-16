package jago.domain.type;

import jago.domain.Parameter;
import jago.domain.scope.CallableSignature;

import java.util.Collections;


public class AnyType implements Type {


    public static AnyType INSTANCE = new AnyType();

    @Override
    public String getName() {
        return "Any";
    }

}
