package jago.domain.scope;

import jago.domain.Parameter;
import jago.domain.VarargParameter;
import jago.domain.generic.GenericParameter;
import jago.domain.node.expression.call.Argument;
import jago.domain.type.*;
import jago.domain.type.generic.GenericParameterType;
import jago.domain.type.generic.GenericType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public class CallableSignature {
    private final Type owner;
    private final String name;
    private final List<Parameter> parameters;
    private Type returnType;

    public CallableSignature(Type owner, String name, List<Parameter> parameters, Type returnType) {
        this.owner = owner;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public static CallableSignature constructor(Type type, List<Parameter> parameters) {
        return new CallableSignature(type, type.getName(), parameters, UnitType.INSTANCE);
    }

    public boolean isVararg() {
        return parameters.get(parameters.size() - 1) instanceof VarargParameter;
    }


    public VarargParameter getVarargParameter() {
        return isVararg()
                ? (VarargParameter) parameters.get(parameters.size() - 1)
                : null;
    }


    public String getName() {
        return name;
    }


    public Type getOwner() {
        return owner;
    }


    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public boolean matchesExactly(String otherSignatureName, List<Argument> arguments) {
        if (!nameAndCountMatches(otherSignatureName, arguments)) return false;
        return argumentsAndParamsMatchedByIndex(arguments, Objects::equals);
    }

    public boolean matches(String otherSignatureName, List<Argument> arguments) {
        if (!nameAndCountMatches(otherSignatureName, arguments)) return false;
        return argumentsAndParamsMatchedByIndex(arguments, NullableType::isNullableOf);
    }


    private boolean nameAndCountMatches(String otherSignatureName, List<Argument> arguments) {
        boolean namesAreEqual = this.name.equals(otherSignatureName);
        if (!namesAreEqual) return false;
        long nonDefaultParametersCount = parameters.stream()
                .filter(p -> !p.hasDefaultValue())
                .count();
        // not sure if  >= @hunter04d
        return nonDefaultParametersCount >= arguments.size();
    }

    private boolean argumentsAndParamsMatchedByIndex(List<Argument> arguments, BiPredicate<Type, Type> typeComparator) {
        return IntStream.range(0, arguments.size())
                .allMatch(i -> {
                    Type parameterType = parameters.get(i).getType().erased();
                    Type argumentType = arguments.get(i).getType().erased();
                    return typeComparator.test(parameterType, argumentType);
                });
    }

    public Type getReturnType() {
        return returnType;
    }


    public boolean hasGenericParameters() {
        return parameters.stream().anyMatch(Parameter::isUnboundGeneric);
    }

    public boolean hasGenericReturnType() {
        return (returnType instanceof GenericType && ((GenericType) returnType).isUnbound())
                || returnType instanceof GenericParameterType;
    }

    public boolean isTypeResolved() {
        return returnType != null;
    }

    public boolean hasDefaulParameters() {
        return parameters.stream().anyMatch(Parameter::hasDefaultValue);
    }

    public void resolveReturnType(Type returnType) {
        if (this.returnType == null) {
            this.returnType = returnType;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallableSignature that = (CallableSignature) o;
        return Objects.equals(owner, that.owner)
                && Objects.equals(name, that.name)
                && CollectionUtils.isEqualCollection(parameters, that.parameters)
                && Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, parameters, returnType);
    }


    @Override
    public String toString() {
        return "Callable{" +
                owner + "." + name +
                " parameters=(" + parameters.stream().map(parameter -> parameter.getType().getName()).collect(Collectors.joining(", ")) +
                "): returnType=" + returnType +
                '}';
    }
}
