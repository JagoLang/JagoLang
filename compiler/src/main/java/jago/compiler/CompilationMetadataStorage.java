package jago.compiler;

import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
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
        GraphUtil.findCycle(implicitResolutionGraph, from);
    }
}
