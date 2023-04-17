package io.metersphere.sdk.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class LogUtils {
    //日志工具类
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";

    public static void writeLog(Object msg, String level, Logger logger) {
        if (logger != null && logger.isInfoEnabled()) {
            switch (level) {
                case DEBUG -> logger.debug(LogUtils.getMsg(msg));
                case INFO -> logger.info(LogUtils.getMsg(msg));
                case WARN -> logger.warn(LogUtils.getMsg(msg));
                case ERROR -> logger.error(LogUtils.getMsg(msg));
                default -> logger.error(StringUtils.EMPTY);
            }
        }
    }

    public static void info(Object msg, Logger logger) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg));
        }
    }

    public static void info(Object msg, Object o1, Logger logger) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg), o1);
        }
    }

    public static void info(Object msg, Object o1, Object o2, Logger logger) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void info(Object msg, Object[] obj, Logger logger) {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg), obj);
        }
    }

    public static void debug(Object msg, Logger logger) {
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg));
        }
    }

    public static void debug(Object msg, Object o, Logger logger) {
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg), o);
        }
    }

    public static void debug(Object msg, Object o1, Object o2, Logger logger) {
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void debug(Object msg, Object[] obj, Logger logger) {
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg), obj);
        }
    }

    public static void warn(Object msg, Logger logger) {
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg));
        }
    }

    public static void warn(Object msg, Object o, Logger logger) {
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg), o);
        }
    }

    public static void warn(Object msg, Object o1, Object o2, Logger logger) {
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void warn(Object msg, Object[] obj, Logger logger) {
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg), obj);
        }
    }

    public static void error(Object msg, Logger logger) {
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg));// 并追加方法名称
        }
    }

    public static void error(Throwable e, Logger logger) {
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(e), e);// 同时打印错误堆栈信息
        }
    }

    public static void error(Object msg, Object o, Logger logger) {
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), o);
        }
    }

    public static void error(Object msg, Object o1, Object o2, Logger logger) {
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void error(Object msg, Object[] obj, Logger logger) {
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), obj);
        }
    }

    public static void error(Object msg, Throwable ex, Logger logger) {
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), ex);
        }
    }

    public static String getMsg(Object msg, Throwable ex) {
        String str = "";

        if (msg != null) {
            str = LogUtils.getLogMethod() + "[" + msg + "]";
        } else {
            str = LogUtils.getLogMethod() + "[null]";
        }
        if (ex != null) {
            str += "[" + ex.getMessage() + "]";
        }

        return str;
    }

    public static String getMsg(Object msg) {
        return LogUtils.getMsg(msg, null);
    }


    /**
     * 得到调用方法名称
     *
     * @return
     */
    private static String getLogMethod() {
        String str = "";
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        if (stack.length > 4) {
            StackTraceElement ste = stack[4];
            str = "Method[" + ste.getMethodName() + "]";// 方法名称
        }
        return str;
    }
}
