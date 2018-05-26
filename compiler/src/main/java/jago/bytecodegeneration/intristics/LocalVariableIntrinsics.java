package jago.bytecodegeneration.intristics;

import jago.domain.node.expression.LocalVariable;
import jago.domain.scope.LocalScope;
import org.apache.commons.collections4.map.LinkedMap;

public class LocalVariableIntrinsics {


    public static int getVariableIndex(String name, LocalScope localScope) {
        int sum = 0;
        LinkedMap<String, LocalVariable> declaredVariables = localScope.getDeclaredVariables();
        for (int i = 0; i < declaredVariables.indexOf(name); i++) {
            sum += TypeOpcodesIntrinsics.getTypeStackSize(declaredVariables.getValue(i).getType());
        }
        return sum + TypeOpcodesIntrinsics.getTypeStackSize(declaredVariables.get(name).getType()) - 1;
    }
}
