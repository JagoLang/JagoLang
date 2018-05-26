package jago.parsing.visitor.expression;

import jago.JagoBaseVisitor;
import jago.JagoParser;
import jago.domain.node.expression.Expression;
import jago.domain.node.expression.LocalVariable;
import jago.domain.node.expression.VariableReference;
import jago.domain.node.expression.calls.Call;
import jago.domain.node.expression.calls.ConstructorCall;
import jago.domain.node.expression.calls.InstanceCall;
import jago.domain.node.expression.calls.StaticCall;
import jago.domain.scope.CallableSignature;
import jago.domain.scope.LocalScope;
import jago.domain.type.BuiltInType;
import jago.domain.type.ClassType;
import jago.domain.type.Type;
import jago.exception.IllegalReferenceException;
import jago.util.SignatureResolver;
import jago.util.TypeResolver;
import jago.util.constants.Messages;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


//Todo: refactor whole class
public class CallVisitor extends JagoBaseVisitor<Call> {
    private final LocalScope scope;
    private final ExpressionVisitor expressionVisitor;

    public CallVisitor(LocalScope scope, ExpressionVisitor expressionVisitor) {
        this.scope = scope;
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public Call visitMethodCall(JagoParser.MethodCallContext ctx) {
        String methodName = ctx.callableName().getText();


        List<Expression> arguments = getArgumentsForCall((JagoParser.UnnamedArgumentsListContext) ctx.argumentList());


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
                    scope.getCompilationUnitScope().resolveCallable(scope.getCallable(), signature);
                }
                return new StaticCall(ownerType.get(), signature, arguments);
            }

            // signature not found, this is a fully qualified ctor call
            Optional<Type> typeToCtor = TypeResolver.getFromTypeNameBypassImportCheck(owner + "." + methodName, scope);
            if (!typeToCtor.isPresent()) {
                throw new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments));
            }
            List<Type> argumentTypes = arguments.stream().map(Expression::getType).collect(toList());
            if (!SignatureResolver.doesConstructorExist(typeToCtor.get().getName(), argumentTypes, scope)) {
                throw new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments));
            }
            CallableSignature signature = getConstructorCallSignature(typeToCtor.get(), methodName, arguments);
            return new ConstructorCall(signature, (ClassType) typeToCtor.get(), arguments);

        }
        // Local call
        CallableSignature signature = getMethodCallSignature(methodName, arguments);

        if (signature != null) {
            if (!signature.isTypeResolved()) {
                scope.getCompilationUnitScope().resolveCallable(scope.getCallable(), signature);
            }
            return new StaticCall(TypeResolver.getFromTypeNameOrThrow(scope.getClassName(), scope), signature, arguments);
        }
        // a static import or a imported class ctor call
        //TODO see if a static import

        Type typeToCtor = TypeResolver.getFromTypeNameOrThrow(methodName, scope);
        // TODO provide ctors for build in types or make a division between builtin reference types, arrays and primitives
        if (typeToCtor instanceof BuiltInType) {
            throw new NotImplementedException("We need to do something about the built in type");
        }
        List<Type> argumentTypes = arguments.stream().map(Expression::getType).collect(toList());
        if (!SignatureResolver.doesConstructorExist(typeToCtor.getName(), argumentTypes, scope)) {
            throw new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments));
        }
        signature = getConstructorCallSignature(typeToCtor, methodName, arguments);

        return new ConstructorCall(signature, (ClassType) typeToCtor, arguments);

    }


    private CallableSignature getMethodCallSignatureForInstanceCall(Type owner,
                                                                    String methodName,
                                                                    List<Expression> arguments) {
        if (owner.getName().equals(scope.getClassName())) {
            return getMethodCallSignature(methodName, arguments);
        }
        List<Type> argumentsTypes = arguments.stream().map(Expression::getType).collect(toList());
        return SignatureResolver.getMethodSignatureForInstanceCall(owner, methodName, argumentsTypes, scope)
                .orElseThrow(() -> new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments)));
    }

    private CallableSignature getConstructorCallSignature(Type owner,
                                                                    String methodName,
                                                                    List<Expression> arguments) {

        if (owner.getName().equals(scope.getClassName())) {
            //ToDo: Handle comiplation Classes
            return getMethodCallSignature(methodName, arguments);
        }
        List<Type> argumentsTypes = arguments.stream().map(Expression::getType).collect(toList());
        return SignatureResolver.getConstructorSignature(owner.getName(), argumentsTypes, scope)
                .orElseThrow(() -> new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments)));
    }


    private CallableSignature getMethodCallSignatureForStaticCall(Type owner,
                                                                  String methodName,
                                                                  List<Expression> arguments) {

        if (owner.getName().equals(scope.getClassName())) {
            return getMethodCallSignature(methodName, arguments);
        }
        List<Type> argumentsTypes = arguments.stream().map(Expression::getType).collect(toList());
        return SignatureResolver.getMethodSignatureForStaticCall(owner, methodName, argumentsTypes, scope)
                .orElseThrow(() -> new IllegalReferenceException(String.format(Messages.METHOD_DONT_EXIST, methodName, arguments)));
    }

    private CallableSignature getMethodCallSignature(String identifier, List<Expression> arguments) {
        if (identifier.equals("super")) {
            //call to super's ctor, this should not be here
            return new CallableSignature("super", "super", Collections.emptyList(), BuiltInType.VOID);
        }
        return scope.getCompilationUnitScope()
                .getCallableSignatures()
                .stream()
                .filter(signature -> signature.matches(identifier, arguments))
                .findFirst()
                .orElse(null);
    }

    private List<Expression> getArgumentsForCall(JagoParser.UnnamedArgumentsListContext argumentsListCtx) {
        if (argumentsListCtx != null) {
            return argumentsListCtx.argument()
                    .stream()
                    .map(argCon -> argCon.accept(expressionVisitor).used())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
