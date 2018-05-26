package jago.parsing.visitor;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.JagoParser.ClassDeclarationContext;
import jago.JagoParser.CompilationUnitContext;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.Callable;
import jago.domain.Clazz;
import jago.domain.CompilationUnit;
import jago.domain.imports.Import;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.CompilationUnitScope;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 *
 */
public class CompilationUnitVisitor extends JagoBaseVisitor<CompilationUnit> {

    private String fileName;
    private CountDownLatch latch;

    public CompilationUnitVisitor(String fileName, CountDownLatch latch) {
        this.fileName = fileName;
        this.latch = latch;
    }

    @Override
    public CompilationUnit visitCompilationUnit(@NotNull CompilationUnitContext ctx) {
        ClassVisitor classVisitor = new ClassVisitor();
        List<ClassDeclarationContext> classDeclarationContext = ctx.classDeclaration();
        String packageName = null;

        if (ctx.packageName() != null) packageName = ctx.packageName().getText();

        String owner =  (packageName == null ? "" : packageName + "." ) + fileName;

        ImportsVisitor importsVisitor = new ImportsVisitor();

        List<Import> imports = importsVisitor.visitImports(ctx.imports());

        CallableVisitor callableVisitor = new CallableVisitor(owner, imports);

        CompilationUnitScope compilationUnitScope = new CompilationUnitScope(packageName, imports, fileName);

        synchronized (CompilationMetadataStorage.compilationUnitScopes) {
            CompilationMetadataStorage.compilationUnitScopes.put(compilationUnitScope.getInternalName(), compilationUnitScope);
        }

        List<Clazz> classDeclarations = classDeclarationContext.stream()
                .map(c -> c.accept(classVisitor))
                .collect(Collectors.toList());

        compilationUnitScope.setDataClasses(classDeclarations);

        List<Pair<CallableSignature, JagoParser.CallableBodyContext>> signaturesWithBlocks = ctx.callable().stream()
                .map(f -> f.accept(callableVisitor))
                .collect(Collectors.toList());

        compilationUnitScope.setCallableSignatures(signaturesWithBlocks);

        compilationUnitScope.setImplicitReturnTypeResolver(signaturesWithBlocks);

        latch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Callable> callables = compilationUnitScope.resolveCallables();

        return new CompilationUnit(classDeclarations, callables, compilationUnitScope);
    }


}
