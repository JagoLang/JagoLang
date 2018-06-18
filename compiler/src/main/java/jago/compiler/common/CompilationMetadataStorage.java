package jago.compiler.common;

import jago.compiler.domain.scope.CallableSignature;
import jago.compiler.domain.scope.CompilationUnitScope;
import jago.compiler.exception.RecursiveReturnTypeInferenceException;
import jago.compiler.util.GraphUtil;
import jago.compiler.util.graphs.ConcurrentDirectionalGraph;
import jago.compiler.util.graphs.DirectionalGraph;
import org.apache.commons.collections4.map.LinkedMap;


public class CompilationMetadataStorage {
    public static final LinkedMap<String, CompilationUnitScope> compilationUnitScopes = new LinkedMap<>();

    public static CompilationUnitScope getCompUnitScope(String key){
        return compilationUnitScopes.get(key);
    }


    public static final DirectionalGraph<CallableSignature> implicitResolutionGraph = new ConcurrentDirectionalGraph<>();

    public static void findCyclicDependencies(CallableSignature from) {
        try {
            GraphUtil.findCycle(implicitResolutionGraph, from);
        }
        catch (IllegalArgumentException e) {
            throw new RecursiveReturnTypeInferenceException(from);
        }

    }
}
