package jago.util;

public class CheckUtils {
    public static void notNull(Object checked, Runnable runnable) {
        if (checked == null) return;
        runnable.run();
    }
}
