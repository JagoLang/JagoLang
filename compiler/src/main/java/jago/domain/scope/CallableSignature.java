package jago.domain.scope;

import jago.domain.node.expression.Expression;
import jago.domain.node.expression.Parameter;
import jago.domain.type.Type;
import jago.domain.type.UnitType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    public static CallableSignature  constructor(String type, List<Parameter> parameters) {
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



    public boolean matches(String otherSignatureName, List<Expression> arguments) {
        boolean namesAreEqual = this.name.equals(otherSignatureName);
        if (!namesAreEqual) return false;
        long nonDefaultParametersCount = parameters.stream()
                .filter(p -> !p.getDefaultValue().isPresent())
                .count();
        if (nonDefaultParametersCount > arguments.size()) return false;

        return areArgumentsAndParamsMatchedByIndex(arguments);
    }

    private boolean areArgumentsAndParamsMatchedByIndex(List<Expression> arguments) {
        return IntStream.range(0, arguments.size())
                .allMatch(i -> {
                    Type argumentType = arguments.get(i).getType();
                    Type parameterType = parameters.get(i).getType();
                    return argumentType.equals(parameterType);
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
        return Objects.equals(owner, that.owner) &&
                Objects.equals(name, that.name) &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(returnType, that.returnType);
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
