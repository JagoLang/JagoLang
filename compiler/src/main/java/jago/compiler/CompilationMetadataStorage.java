package jago.compiler;

import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import jago.exception.RecursiveReturnTypeInferenceException;
import jago.util.GraphUtil;
import jago.util.graphs.ConcurrentDirectionalGraph;
import jago.util.graphs.DirectionalGraph;
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
