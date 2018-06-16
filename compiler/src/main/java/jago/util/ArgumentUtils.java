package jago.util;

import jago.domain.Parameter;
import jago.domain.node.expression.call.Argument;
import jago.domain.node.expression.call.NamedArgument;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArgumentUtils {

    public static List<Argument> sortedArguments(List<Argument> arguments, List<Parameter> parameters) {
        Argument[] sortedList = new Argument[arguments.size()];
        int i = 0;
        while (!(i >= arguments.size() || arguments.get(i) instanceof NamedArgument)) {
            sortedList[i] = arguments.get(i);
            i++;
        }
        List<String> paramNames = parameters.stream().skip(i).map(Parameter::getName).collect(Collectors.toList());
        for (int j = i; j < arguments.size(); j++) {
            sortedList[paramNames.indexOf(((NamedArgument) arguments.get(j)).getName()) + i] = arguments.get(j);
        }
        return Arrays.asList(sortedList);

    }
}
