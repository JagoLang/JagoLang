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
import jago.domain.scope.GenericCallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.*;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import jago.exception.IllegalReferenceException;
import jago.exception.SemanticException;
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

        List<Type> genericArguments = ctx.primaryGenericArgs != null
                ? ParserUtils.parseGenericArguments(ctx.primaryGenericArgs, scope.getImports())
                : null;
        List<Type> secondarygenericArguments = ctx.secondaryGenericArgs!= null
                ? ParserUtils.parseGenericArguments(ctx.secondaryGenericArgs, scope.getImports())
                : null;

        if (ctx.owner != null) {
            Expression owner = ctx.owner.accept(expressionVisitor).used();
            Type ownerType = owner.getType();
            CallableSignature signature = getMethodCallSignatureForInstanceCall(ownerType, methodName, arguments);
            // TODO generics check
            tryCompleteGenericBinding(ownerType, signature, arguments, genericArguments);
            Type returnType = bindType(signature, ownerType, signature.getReturnType());
            return new InstanceCall(owner, signature, arguments, returnType);
        }

        if (ctx.qualifiedName() != null) {
            // Local variable or field if in class
            if (scope.isVariableDeclared(ctx.qualifiedName().getText())) {
                if (secondarygenericArguments != null) {
                    throw new SemanticException("2 generic arg lists for non ctor call");
                }

                LocalVariable localVariable = scope.getLocalVariable(ctx.qualifiedName().getText());
                Type ownerType = localVariable.getType();
                //TODO generics check
                CallableSignature signature = getMethodCallSignatureForInstanceCall(ownerType, methodName, arguments);
                if (signature.getOwner() instanceof GenericType && ownerType instanceof GenericType) {
                    checkGenericBindsForOwner(signature, (GenericType) ownerType, arguments);
                }
                return new InstanceCall(new VariableReference(localVariable).used(), signature, arguments, signature.getReturnType());
            }
            // Call to something else
            String owner = ctx.qualifiedName().getText();
            Optional<Type> ownerType = TypeResolver.getFromTypeName(owner, scope);
            if (ownerType.isPresent()) {
                if (secondarygenericArguments != null) {
                    throw new SemanticException("2 generic arg lists for non ctor call");
                }

                CallableSignature signature = getMethodCallSignatureForStaticCall(ownerType.get(), methodName, arguments);
                if (!signature.isTypeResolved()) {
                    awaitReturnTypeResolution(signature);
                }
                //TODO generics check
                return new StaticCall(ownerType.get(), signature, arguments);
            }

            // signature not found, this is a fully qualified ctor call
            Type typeToCtor = TypeResolver.getFromTypeName(owner + "." + methodName, scope)
                    .orElseThrow(() -> getIllegalReferenceException(methodName, arguments));
            CallableSignature signature = getConstructorCallSignature(typeToCtor, arguments);

            // bind the type arguments to generic parameters

            if (signature.getOwner() instanceof GenericType) {
                if (genericArguments == null) {
                    // TODO maybe instantiate an erased object???
                    throw new SemanticException("Ctor must always have at least diamond notation");
                }
                typeToCtor = bindSignatureOwner(signature, genericArguments, arguments);
            }
            // TODO the constructor itself might have generic parameters
            //TODO ctor generic binds
            if (signature instanceof GenericCallableSignature) {
                checkGenericBindsForSignature(((GenericCallableSignature) signature), typeToCtor, arguments, secondarygenericArguments);
            }
            return new ConstructorCall(signature, typeToCtor, arguments);
        }
        // Local call
        CallableSignature signature = getMethodCallSignature(methodName, arguments);
        if (signature != null) {
            if (!signature.isTypeResolved()) {
                awaitReturnTypeResolution(signature);
            }
            //TODO generics check
            return new StaticCall(TypeResolver.getFromTypeNameOrThrow(scope.getClassName(), scope), signature, arguments);
        }
        // a static import or a imported class ctor call
        //TODO see if a static import


        // ctor call
        Type typeToCtor = TypeResolver.getFromTypeNameOrThrow(methodName, scope);
        signature = getConstructorCallSignature(typeToCtor, arguments);
        //TODO generics check
        // bind the type arguments to generic parameters

        if (signature.getOwner() instanceof GenericType) {
            if (genericArguments == null) {
                // TODO maybe instantiate an erased object???
                throw new SemanticException("Ctor must always have at least diamond notation");
            }
            typeToCtor = bindSignatureOwner(signature, genericArguments, arguments);
        }
        // TODO the constructor itself might have generic parameters
        //TODO ctor generic binds
        if (signature instanceof GenericCallableSignature) {
            checkGenericBindsForSignature(((GenericCallableSignature) signature), typeToCtor, arguments, secondarygenericArguments);
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

    /*TODO ======================Generic method resolution type checker, extract into a separate class =============================== TODO*/
    private static boolean genericBindsMatchArguments(GenericType boundType,
                                                      CallableSignature signature,
                                                      List<Argument> arguments) {
        List<Type> parameterTypes = signature.getParameters().stream().map(Parameter::getType).collect(toList());
        //TODO position arguments properly
        List<Type> argumentTypes = arguments.stream().map(Argument::getType).collect(toList());
        for (int i = 0; i < argumentTypes.size(); i++) {
            Type parameterType = parameterTypes.get(i);
            Type argumentType = argumentTypes.get(i);
            if (parameterType instanceof GenericParameterType) {
                if (!matchGenericParameterToArgument(boundType, parameterType.getGenericParameter(), argumentType)) {
                    return false;
                }
                continue;
            }
            if (parameterType instanceof GenericType
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

    private static Type bindSignatureOwner(CallableSignature signature,
                                           List<Type> genericArguments,
                                           List<Argument> arguments) {
        if (genericArguments == null) {
            throw new TypeMismatchException();
        }

        GenericType signatureOwnerGeneric = (GenericType) signature.getOwner();
        if (genericArguments.isEmpty()) {
            return signatureOwnerGeneric;
        }
        GenericType boundType = signatureOwnerGeneric.bind(genericArguments);
        //TODO type check does not yet account for named and/or default arguments
        if (genericBindsMatchArguments(boundType, signature, arguments)) {
            return boundType;
        }
        // TODO deduce generic binds from arguments if possible or return type if in can be matched to anything
        throw new TypeMismatchException();

    }

    private static boolean matchGenericParameterToArgument(GenericType boundType,
                                                           GenericParameter parameter,
                                                           Type argumentType) {
        int i = boundType.getBounds().indexOf(parameter);
        if (i == -1) {
            // it doesn't actually match however it might match to a signature generic,
            // so just bypass it
            return true;
        }
        Type type = boundType.getGenericArguments().get(i);
        return NullableType.isNullableOf(type, argumentType);

    }

    private static void tryCompleteGenericBinding(Type ownerType,
                                                  CallableSignature signature,
                                                  List<Argument> arguments,
                                                  List<Type> genericArguments) {
        if (ownerType instanceof GenericType) {
            checkGenericBindsForOwner(signature,  (GenericType) ownerType, arguments);
        }
        tryCallableGenericBinding(ownerType, signature, arguments, genericArguments);
    }

    private static Type bindType(CallableSignature signature, Type ownerTypeBound, Type typeToBind) {
        if (typeToBind instanceof GenericParameterType) {
            if (ownerTypeBound instanceof GenericType) {
                GenericType genericType = (GenericType) ownerTypeBound;
                int i = genericType.getBounds().indexOf(typeToBind.getGenericParameter());
                if (i != -1) {
                    return genericType.getGenericArguments().get(i);
                }
            }
            if (signature instanceof GenericCallableSignature) {
                GenericCallableSignature genericCallableSignature = (GenericCallableSignature) signature;
                int i = genericCallableSignature.getBounds().indexOf(typeToBind.getGenericParameter());
                if (i != -1) {
                    return genericCallableSignature.getGenericArguments().get(i);
                }
            }
        } else if (typeToBind instanceof GenericType && ((GenericType) typeToBind).isUnbound()) {
            GenericType genericReturnType = (GenericType) typeToBind;
            List<Type> boundArgs = genericReturnType.getGenericArguments().stream().map(ga -> bindType(signature, ownerTypeBound, ga)).collect(toList());
            return genericReturnType.bind(boundArgs);
        }
        return typeToBind;
    }


    private static void tryCallableGenericBinding(Type ownerType,
                                                  CallableSignature signature,
                                                  List<Argument> arguments, List<Type> genericArguments) {
        if (signature instanceof GenericCallableSignature) {
            checkGenericBindsForSignature((GenericCallableSignature) signature, ownerType, arguments, genericArguments);
        }
    }

    private static void checkGenericBindsForOwner(CallableSignature signature,
                                                  GenericType ownerType,
                                                  List<Argument> arguments) {
        if (!genericBindsMatchArguments(ownerType, signature, arguments)) {
            throw new TypeMismatchException();
        }
    }

    private static void checkGenericBindsForSignature(GenericCallableSignature genericSignature,
                                                      Type ownerType,
                                                      List<Argument> arguments,
                                                      List<Type> genericArguments) {
        // TODO: signature itself is generic try to match to it
        // TODO: deduce generic binds from arguments if possible
        // TODO: we are not done yet, is return type generic? we might be able to do a type match later, consider this to be a match, type check will be done later
    }
}
