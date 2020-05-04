package utils;

/**
 * @author ashan on 2020-05-01
 */
public class ExceptionHandler {
    public interface UnhandledFunction<T> {
        T apply() throws Throwable;
    }

    public interface UnhandledRunnable {
        void run() throws Throwable;
    }

    public static <T> T unhandled(UnhandledFunction<T> function) {
        try {
            return function.apply();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static void unhandled(UnhandledRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static void ignore(UnhandledRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ignore) {

        }
    }

    public static <T> T ignoreWithDefault(UnhandledFunction<T> function, T defaultValue) {
        try {
            return function.apply();
        } catch (Throwable ignore) {
            return defaultValue;
        }
    }
}
