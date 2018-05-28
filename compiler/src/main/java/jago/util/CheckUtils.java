package jago.util;

public final class CheckUtils {
    public static void notNull(Object checked, Runnable runnable) {
        if (checked == null) return;
        runnable.run();
    }
}
