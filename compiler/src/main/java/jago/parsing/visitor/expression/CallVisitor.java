package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.compiler.CompilationMetadataStorage;
import jago.domain.Parameter;
import jago.domain.generic.GenericParameter;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.call.*;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import jago.exception.IllegalReferenceException;
import jago.exception.TypeMismatchException;
import jago.util.ParserUtils;
import jago.util.SignatureResolver;
import jago.util.TypeResolver;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;

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
        List<Type> genericArguments = ctx.genericArguments() != null
                ? ParserUtils.parseGenericArguments(ctx.genericArguments(), scope.getImports())
                : null;
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
            Type typeToCtor = TypeResolver.getFromTypeName(owner + "." + methodName, scope)
                    .orElseThrow(() -> getIllegalReferenceException(methodName, arguments));
            CallableSignature signature = getConstructorCallSignature(typeToCtor, arguments);
            // bind the type arguments to generic parameters
            if (signature.isGeneric()) {
                if (signature.getOwner() instanceof GenericType && genericArguments != null) {
                    GenericType signatureOwnerGeneric = (GenericType) signature.getOwner();
                    GenericType boundType = signatureOwnerGeneric.bind(genericArguments);
                    //TODO type check does not yet account for named and/or default arguments
                    if (!genericBindsMatchArguments(boundType, signature, arguments)) {
                        throw new TypeMismatchException();
                    }
                    typeToCtor = boundType;
                } else
                    //TODO deduce generic binds from arguments if possible
                    throw new TypeMismatchException();
            }
            return new ConstructorCall(signature, typeToCtor, arguments);
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

    private static boolean genericBindsMatchArguments(GenericType boundType,
                                                      CallableSignature signature,
                                                      List<Argument> arguments) {
        List<Type> parameterTypes = signature.getParameters().stream().map(Parameter::getType).collect(toList());
        List<Type> argumentTypes = arguments.stream().map(Argument::getType).collect(toList());
        for (int i = 0; i < parameterTypes.size(); i++) {
            Type parameterType = parameterTypes.get(i);
            Type argumentType = argumentTypes.get(i);
            if (parameterType instanceof GenericParameterType) {
                if (!matchGenericParameterToArgument(boundType, parameterType.getGenericParameter(), argumentType)) {
                    return false;
                }
            } else if (parameterType instanceof GenericType
                    && argumentType instanceof GenericType
                    && !matchGenericTypeToArgument(boundType, (GenericType) parameterType, (GenericType) argumentType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchGenericTypeToArgument(GenericType boundType,
                                                      GenericType parameterType,
                                                      GenericType argumentType) {
        List<Type> genericArgumentsFromParameter = parameterType.getGenericArguments();
        List<Type> genericArgumentsFromArgument = argumentType.getGenericArguments();
        if (genericArgumentsFromArgument.size() != genericArgumentsFromParameter.size()) {
            return false;
        }
        for (int i = 0; i < genericArgumentsFromParameter.size(); i++) {
            Type typeParam = genericArgumentsFromParameter.get(i);
            Type typeArg = genericArgumentsFromArgument.get(i);
            if (typeParam instanceof GenericParameterType) {
                if (!matchGenericParameterToArgument(boundType, parameterType.getGenericParameter(), argumentType)) {
                    return false;
                }

            } else if (typeParam instanceof GenericType && typeArg instanceof GenericType) {
                if (!matchGenericTypeToArgument(boundType, ((GenericType) typeParam), ((GenericType) typeArg))) {
                    return false;
                }
            }
        }
        return true;

    }


    private static boolean matchGenericParameterToArgument(GenericType boundType,
                                                           GenericParameter parameter,
                                                           Type argumentType) {
        Type type = boundType.getGenericArguments().get(boundType.getBounds().indexOf(parameter));
        return NullableType.isNullableOf(type, argumentType);

    }
}
