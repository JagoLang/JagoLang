package jago.util;

import com.google.common.collect.Maps;
import jago.bytecodegeneration.expression.ExpressionGenerator;
import jago.domain.node.expression.Expression;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class GeneratorResolver {


    public static <T, U> Optional<Method>  resolveGenerationMethod(Class<T> clazz, U generatingEntity) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.getParameterCount() == 1
                        && m.getParameterTypes()[0] == generatingEntity.getClass())
                .findFirst();
    }

    public static <T, U> Map<Class<?>, Method> getAllGenerationMethods(Class<T> clazz, Class<U> generationBase) {
        Method[] methods = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> {
            if (m.getParameterCount() != 1) return false;
            Class<?> parameter = m.getParameterTypes()[0];
            return !Modifier.isAbstract(parameter.getModifiers())
                    && !parameter.isInterface()
                    && parameter.isAssignableFrom(generationBase);
        }).toArray(Method[]::new);
        return Maps.uniqueIndex(() -> Arrays.stream(methods).iterator(), m -> m.getParameterTypes()[0]);
    }
}
