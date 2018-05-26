package jago.compiler;

import jago.domain.scope.CompilationUnitScope;
import org.apache.commons.collections4.map.LinkedMap;


public class CompilationMetadataStorage {
    public static final LinkedMap<String, CompilationUnitScope> compilationUnitScopes = new LinkedMap<>();

    public static CompilationUnitScope getCompUnitScope(String key){
        return compilationUnitScopes.get(key);
    }
}
