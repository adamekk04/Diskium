package org.diskium;

public final class MultiplatformLogger {

    private static Logger logger;

    public static void setLogger(Logger logger) {
        MultiplatformLogger.logger = logger;
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void error(String msg, Throwable throwable) {
        logger.error(msg, throwable);
    }

    public interface Logger {
        void info(String message);
        void warn(String message);
        void error(String message);
        void error(String message, Throwable throwable);
    }
}
