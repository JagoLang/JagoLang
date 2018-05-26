package jago.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class GeneratorResolver {


    public static <T, U> Optional<Method>  resolveGenerationMethod(T clazz, U generatingEntity) {
        return Arrays.stream(clazz.getClass().getMethods())
                .filter(m -> m.getParameterCount() == 1
                        && m.getParameterTypes()[0].equals(generatingEntity.getClass()))
                .findFirst();
    }
}
