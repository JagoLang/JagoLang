package jago.domain.scope;

import jago.JagoParser;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.Callable;
import jago.domain.Clazz;
import jago.domain.imports.Import;
import jago.domain.node.statement.Statement;
import jago.parsing.visitor.CallableBodyVisitor;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CompilationUnitScope {
    // TODO: add file scope of data and file callableSignatures

    private String packageName;
    private List<Import> imports;
    private List<CallableSignature> callableSignatures;
    private ImplicitReturnTypeResolver implicitReturnTypeResolver;
    private List<Clazz> dataClasses;
    private String internalName;
    private String className;

    public CompilationUnitScope(String packageName, List<Import> imports, String fileName) {
        this.packageName = packageName;
        this.imports = imports;
        className = fileName;
        if (packageName == null) {
            internalName = fileName;
        } else {
            internalName = String.format("%s/%s", packageName.replaceAll("\\.", "/"), fileName);
        }
    }

    public void setImplicitReturnTypeResolver(List<Pair<CallableSignature, JagoParser.CallableBodyContext>> sc) {
        this.implicitReturnTypeResolver = new ImplicitReturnTypeResolver(sc);
    }

    public void setCallableSignatures(List<Pair<CallableSignature, JagoParser.CallableBodyContext>> sc) {
        this.callableSignatures = sc.stream().map(Pair::getKey).collect(Collectors.toList());
    }

    public void setDataClasses(List<Clazz> dataClasses) {
        this.dataClasses = dataClasses;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<Import> getImports() {
        return imports;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getFullyQualifiedName() {
        return packageName == null ? className : packageName + "." + className;
    }

    public List<CallableSignature> getCallableSignatures() {
        return callableSignatures;
    }

    public List<Clazz> getDataClasses() {
        return dataClasses;
    }

    public List<Callable> resolveCallables() {
        implicitReturnTypeResolver.resolveCallables();
        return Arrays.asList(implicitReturnTypeResolver.callables);
    }


    private class ImplicitReturnTypeResolver {

        private final LinkedMap<CallableSignature, JagoParser.CallableBodyContext> materials;
        private final Callable[] callables;

        ImplicitReturnTypeResolver(List<Pair<CallableSignature, JagoParser.CallableBodyContext>> sc) {
            materials = new LinkedMap<>();
            sc.forEach(p -> materials.put(p.getKey(), p.getValue()));
            callables = new Callable[sc.size()];
        }

        void resolveCallables() {
            ExecutorService executor = Executors.newFixedThreadPool(callables.length);
            List<Future<Callable>> units = new ArrayList<>(callables.length);
            for (int i = 0; i < callables.length; ++i) {
                units.add(null);
                if (callables[i] == null) {
                    CallableSignature callableSignature = materials.get(i);
                    JagoParser.CallableBodyContext bodyContext = materials.getValue(i);
                    CompilationMetadataStorage.implicitResolutionGraph.addNode(callableSignature);
                    Future<Callable> callableFuture = executor.submit(() -> {
                        CallableBodyVisitor bodyVisitor = new CallableBodyVisitor(CompilationUnitScope.this, callableSignature);
                        Statement block = bodyVisitor.visitCallableBody(bodyContext);
                        return new Callable(callableSignature, block);
                    });
                    units.set(i, callableFuture);
                }
            }

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < callables.length; ++i) {
                if (callables[i] == null) {
                    try {
                        callables[i] = units.get(i).get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.getCause().printStackTrace();
                    }
                }
            }
        }
    }


}
