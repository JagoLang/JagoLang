package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.call.*;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.GenericCallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.Type;
import jago.domain.type.generic.GenericType;
import jago.exception.IllegalReferenceException;
import jago.exception.SemanticException;
import jago.util.ParserUtils;
import jago.util.SignatureResolver;
import jago.util.TypeResolver;
import jago.util.constants.Messages;

import java.util.List;
import java.util.Optional;

import static jago.util.GenericsTypeChecker.*;
import static java.util.stream.Collectors.toList;


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

        List<Type> genericArguments = ctx.primaryGenericArgs != null
                ? ParserUtils.parseGenericArguments(ctx.primaryGenericArgs, scope)
                : null;
        List<Type> secondaryGenericArguments = ctx.secondaryGenericArgs != null
                ? ParserUtils.parseGenericArguments(ctx.secondaryGenericArgs, scope)
                : null;

        if (ctx.owner != null) {
            Expression owner = ctx.owner.accept(expressionVisitor).used();
            Type ownerType = owner.getType();
            CallableSignature signature = getMethodCallSignatureForInstanceCall(ownerType, methodName, arguments);
            if (!signature.isTypeResolved()) {
                awaitReturnTypeResolution(signature);
            }
            if (signature instanceof GenericCallableSignature) {
                signature = bindGenericSignature(((GenericCallableSignature) signature), genericArguments, arguments);
            }
            Type returnType = bindType(signature, ownerType, signature.getReturnType());

            return new InstanceCall(owner, signature, arguments, returnType);
        }

        if (ctx.qualifiedName() != null) {
            // Local variable or field if in class
            if (scope.isVariableDeclared(ctx.qualifiedName().getText())) {
                if (secondaryGenericArguments != null) {
                    throw new SemanticException("2 generic arg lists for non ctor call");
                }
                LocalVariable localVariable = scope.getLocalVariable(ctx.qualifiedName().getText());
                Type ownerType = localVariable.getType();
                CallableSignature signature = getMethodCallSignatureForInstanceCall(ownerType, methodName, arguments);
                if (!signature.isTypeResolved()) {
                    awaitReturnTypeResolution(signature);
                }
                if (signature instanceof GenericCallableSignature) {
                    signature = bindGenericSignature(((GenericCallableSignature) signature), genericArguments, arguments);
                }
                Type returnType = bindType(signature, ownerType, signature.getReturnType());
                return new InstanceCall(new VariableReference(localVariable).used(), signature, arguments, returnType);
            }
            // Call to something else
            String owner = ctx.qualifiedName().getText();
            Optional<Type> ownerTypeOptional = TypeResolver.getFromTypeName(owner, scope);
            if (ownerTypeOptional.isPresent()) {
                if (secondaryGenericArguments != null) {
                    throw new SemanticException("2 generic arg lists for non ctor call");
                }

                Type OwnerType = ownerTypeOptional.get();
                CallableSignature signature = getMethodCallSignatureForStaticCall(OwnerType, methodName, arguments);
                if (!signature.isTypeResolved()) {
                    awaitReturnTypeResolution(signature);
                }
                if (signature instanceof GenericCallableSignature) {
                    signature = bindGenericSignature(((GenericCallableSignature) signature), genericArguments, arguments);
                }
                Type returnType = bindType(signature, signature.getOwner(), signature.getReturnType());
                return new StaticCall(OwnerType, signature, arguments, returnType);
            }

            // signature not found, this is a fully qualified ctor call
            Type typeToCtor = TypeResolver.getFromTypeName(owner + "." + methodName, scope)
                    .orElseThrow(() -> getIllegalReferenceException(methodName, arguments));
            CallableSignature signature = getConstructorCallSignature(typeToCtor, arguments);

            // bind the type arguments to generic parameters
            if (signature.getOwner() instanceof GenericType) {
                if (genericArguments == null) {
                    throw new SemanticException("Generic ctor must always have at least diamond notation");
                }
                typeToCtor = bindSignatureOwner(signature, genericArguments, arguments);
            }
            if (signature instanceof GenericCallableSignature) {
                signature = bindGenericSignature(((GenericCallableSignature) signature), secondaryGenericArguments, arguments);
            }
            return new ConstructorCall(signature, typeToCtor, arguments);
        }

        // Local call
        CallableSignature signature = getMethodCallSignature(methodName, arguments);
        if (signature != null) {
            if (!signature.isTypeResolved()) {
                awaitReturnTypeResolution(signature);
            }
            Type ownerType = signature.getOwner();
            if (signature instanceof GenericCallableSignature) {
                signature = bindGenericSignature(((GenericCallableSignature) signature), genericArguments, arguments);
            }
            Type returnType = bindType(signature, ownerType, signature.getReturnType());
            return new StaticCall(ownerType, signature, arguments, returnType);
        }
        // a static import or a imported class ctor call
        //TODO see if a static import


        // ctor call
        Type typeToCtor = TypeResolver.getFromTypeNameOrThrow(methodName, scope);
        signature = getConstructorCallSignature(typeToCtor, arguments);

        // bind the type arguments to generic parameters
        if (signature.getOwner() instanceof GenericType) {
            if (genericArguments == null) {
                // TODO maybe instantiate an erased object???
                throw new SemanticException("Ctor must always have at least diamond notation");
            }
            typeToCtor = bindSignatureOwner(signature, genericArguments, arguments);
        }

        if (signature instanceof GenericCallableSignature) {
            signature = bindGenericSignature(((GenericCallableSignature) signature), secondaryGenericArguments, arguments);
        }
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
