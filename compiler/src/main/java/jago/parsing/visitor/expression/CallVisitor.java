package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.call.*;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.NumericType;
import jago.domain.type.Type;
import jago.domain.type.UnitType;
import jago.exception.IllegalReferenceException;
import jago.util.ParserUtils;
import jago.util.SignatureResolver;
import jago.util.TypeResolver;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


//Todo: refactor whole class
public class CallVisitor extends JagoBaseVisitor<CallableCall> {
    private final LocalScope scope;
    private final ExpressionVisitor expressionVisitor;

    public CallVisitor(LocalScope scope, ExpressionVisitor expressionVisitor) {
        this.scope = scope;
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public CallableCall visitMethodCall(JagoParser.MethodCallContext ctx) {
        String methodName = ctx.callableName().getText();
        List<Argument> arguments = getArgumentsForCall(ctx.argumentList());
        if (ctx.genericArguments() != null) {
            List<Type> genericArguments = ParserUtils.parseGenericArguments(ctx.genericArguments(), scope.getImports());
        }
        if (ctx.owner != null) {
            Expression owner = ctx.owner.accept(expressionVisitor).used();
            Type ownerType = owner.getType();
            CallableSignature signature = getMethodCallSignatureForInstanceCall(ownerType, methodName, arguments);
            return new InstanceCall(owner, signature, arguments);
        }

        if (ctx.qualifiedName() != null) {
            // Local variable
            if (scope.isVariableDeclared(ctx.qualifiedName().getText())) {
                LocalVariable localVariable = scope.getLocalVariable(ctx.qualifiedName().getText());
                Type ownerType = localVariable.getType();
                CallableSignature signature = getMethodCallSignatureForInstanceCall(ownerType, methodName, arguments);

                return new InstanceCall(new VariableReference(localVariable).used(), signature, arguments);
            }
            // Call to something else
            String owner = ctx.qualifiedName().getText();
            Optional<Type> ownerType = TypeResolver.getFromTypeName(owner, scope);
            if (ownerType.isPresent()) {
                CallableSignature signature = getMethodCallSignatureForStaticCall(ownerType.get(), methodName, arguments);
                if (!signature.isTypeResolved()) {
                    awaitReturnTypeResolution(signature);
                }
                return new StaticCall(ownerType.get(), signature, arguments);
            }

            // signature not found, this is a fully qualified ctor call
            Optional<Type> typeToCtor = TypeResolver.getFromTypeName(owner + "." + methodName, scope);
            if (!typeToCtor.isPresent()) {
                throw getIllegalReferenceException(methodName, arguments);
            }
            CallableSignature signature = getConstructorCallSignature(typeToCtor.get(), arguments);
            return new ConstructorCall(signature, typeToCtor.get(), arguments);

        }
        // Local call
        CallableSignature signature = getMethodCallSignature(methodName, arguments);

        if (signature != null) {
            if (!signature.isTypeResolved()) {
                awaitReturnTypeResolution(signature);
            }
            return new StaticCall(TypeResolver.getFromTypeNameOrThrow(scope.getClassName(), scope), signature, arguments);
        }
        // a static import or a imported class ctor call
        //TODO see if a static import

        // ctor call
        Type typeToCtor = TypeResolver.getFromTypeNameOrThrow(methodName, scope);
        // TODO provide ctors for build in types or make a division between builtin reference types, arrays and primitives
        if (typeToCtor instanceof NumericType) {
            throw new NotImplementedException("We need to do something about the built in type");
        }
        signature = getConstructorCallSignature(typeToCtor, arguments);
        return new ConstructorCall(signature, typeToCtor, arguments);
    }

    private void awaitReturnTypeResolution(CallableSignature signature) {
        CompilationMetadataStorage.implicitResolutionGraph.addEdge(scope.getCallable(), signature);
        // spin waiting, this might be bad, but in practice this has to spin for a short period of time
        while (!signature.isTypeResolved()) {
            CompilationMetadataStorage.findCyclicDependencies(signature);
        }
    }

    private CallableSignature getMethodCallSignatureForInstanceCall(Type owner,
                                                                    String methodName,
                                                                    List<Argument> arguments) {
        return SignatureResolver.getMethodSignatureForInstanceCall(owner, methodName, arguments, scope)
                .orElseThrow(() -> getIllegalReferenceException(methodName, arguments));
    }

    private CallableSignature getConstructorCallSignature(Type owner,
                                                          List<Argument> arguments) {
        return SignatureResolver.getConstructorSignature(owner.getName(), arguments, scope)
                .orElseThrow(() -> getIllegalReferenceException(owner.getName(), arguments));
    }

    private CallableSignature getMethodCallSignatureForStaticCall(Type owner,
                                                                  String methodName,
                                                                  List<Argument> arguments) {
        return SignatureResolver.getMethodSignatureForStaticCall(owner, methodName, arguments, scope)
                .orElseThrow(() -> getIllegalReferenceException(methodName, arguments));
    }

    private IllegalReferenceException getIllegalReferenceException(String methodName, List<?> arguments) {
        return new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments));
    }


    private CallableSignature getMethodCallSignature(String identifier, List<Argument> arguments) {
        if (identifier.equals("super")) {
            //call to super's ctor, this should not be here
            return new CallableSignature("super", "super", Collections.emptyList(), UnitType.INSTANCE);
        }
        // TODO in class context see if this is a instance call or a static call
        List<CallableSignature> callableSignaturesWithMatchingName = scope
                .getCompilationUnitScope()
                .getCallableSignatures()
                .stream()
                .filter(callableSignature -> callableSignature.getName().equals(identifier)).collect(toList());
        return SignatureResolver.getMatchingLocalFunction(identifier, callableSignaturesWithMatchingName, arguments)
                .orElse(null);
    }

    private List<Argument> getArgumentsForCall(JagoParser.ArgumentListContext argumentsListCtx) {
       return new ArgumentVisitor(expressionVisitor).visitArgumentList(argumentsListCtx);
    }
}
