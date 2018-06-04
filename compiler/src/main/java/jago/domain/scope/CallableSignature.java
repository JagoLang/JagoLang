package jago.domain.scope;

import jago.domain.node.expression.Expression;
import jago.domain.node.expression.Parameter;
import jago.domain.type.NullableType;
import jago.domain.type.Type;
import jago.domain.type.UnitType;
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
    private final String owner;
    private final String name;
    private final List<Parameter> parameters;
    private Type returnType;

    public CallableSignature(String owner, String name, List<Parameter> parameters, Type returnType) {
        this.owner = owner;
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public static CallableSignature constructor(String type, List<Parameter> parameters) {
        return new CallableSignature(type, type, parameters, UnitType.INSTANCE);
    }

    public CallableSignature(String owner, String name, List<Parameter> parameters) {
        this.owner = owner;
        this.name = name;
        this.parameters = parameters;
        this.returnType = null;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getInternalName() {
        return owner.replace('.', '/');
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public boolean matchesExactly(String otherSignatureName, List<Type> arguments) {
        if (!nameAndCountMatches(otherSignatureName, arguments)) return false;
        return argumentsAndParamsMatchedByIndex(arguments, Objects::equals);
    }

    public boolean matches(String otherSignatureName, List<Type> arguments) {
        if (!nameAndCountMatches(otherSignatureName, arguments)) return false;
        return argumentsAndParamsMatchedByIndex(arguments, NullableType::isNullableOf);
    }

    private boolean nameAndCountMatches(String otherSignatureName, List<Type> arguments) {
        boolean namesAreEqual = this.name.equals(otherSignatureName);
        if (!namesAreEqual) return false;
        long nonDefaultParametersCount = parameters.stream()
                .filter(p -> !p.getDefaultValue().isPresent())
                .count();
        // not sure if  >= @hunter04d
        return nonDefaultParametersCount >= arguments.size();
    }

    private boolean argumentsAndParamsMatchedByIndex(List<Type> arguments, BiPredicate<Type, Type> typeComparator) {
        return IntStream.range(0, arguments.size())
                .allMatch(i -> {
                    Type parameterType = parameters.get(i).getType();
                    Type argumentType = arguments.get(i);
                    return typeComparator.test(parameterType, argumentType);
                });
    }


    public Type getReturnType() {
        return returnType;
    }


    public boolean isTypeResolved() {
        return returnType != null;
    }

    public void resolveReturnType(Type returnType) {
        this.returnType = returnType;
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
