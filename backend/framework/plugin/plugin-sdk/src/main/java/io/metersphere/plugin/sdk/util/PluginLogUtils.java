package io.metersphere.plugin.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginLogUtils {
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";

    /**
     * 初始化日志
     *
     * @return
     */
    public static Logger getLogger() {
        return LoggerFactory.getLogger(PluginLogUtils.getLogClass());
    }

    public static void writeLog(Object msg, String level) {
        Logger logger = PluginLogUtils.getLogger();

        if (DEBUG.equals(level)) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(PluginLogUtils.getMsg(msg));
            }
        } else if (INFO.equals(level)) {
            if (logger != null && logger.isInfoEnabled()) {
                logger.info(PluginLogUtils.getMsg(msg));
            }
        } else if (WARN.equals(level)) {
            if (logger != null && logger.isWarnEnabled()) {
                logger.warn(PluginLogUtils.getMsg(msg));
            }
        } else if (ERROR.equals(level)) {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error(PluginLogUtils.getMsg(msg));
            }
        } else {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error("");
            }
        }
    }

    public static void info(Object msg) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(PluginLogUtils.getMsg(msg));
            System.out.println(msg);
        }
    }

    public static void info(Object msg, Object o1) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(PluginLogUtils.getMsg(msg), o1);
        }
    }

    public static void info(Object msg, Object o1, Object o2) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(PluginLogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void info(Object msg, Object[] obj) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(PluginLogUtils.getMsg(msg), obj);
        }
    }

    public static void debug(Object msg) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(PluginLogUtils.getMsg(msg));
        }
    }

    public static void debug(Object msg, Object o) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(PluginLogUtils.getMsg(msg), o);
        }
    }

    public static void debug(Object msg, Object o1, Object o2) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(PluginLogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void debug(Object msg, Object[] obj) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(PluginLogUtils.getMsg(msg), obj);
        }
    }

    public static void warn(Object msg) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(PluginLogUtils.getMsg(msg));
        }
    }

    public static void warn(Object msg, Object o) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(PluginLogUtils.getMsg(msg), o);
        }
    }

    public static void warn(Object msg, Object o1, Object o2) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(PluginLogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void warn(Object msg, Object[] obj) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(PluginLogUtils.getMsg(msg), obj);
        }
    }

    public static void error(Object msg) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(PluginLogUtils.getMsg(msg));// 并追加方法名称
        }
    }

    public static void error(Object msg, Object o) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(PluginLogUtils.getMsg(msg), o);
        }
    }

    public static void error(Object msg, Object o1, Object o2) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(PluginLogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void error(Object msg, Object[] obj) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(PluginLogUtils.getMsg(msg), obj);
        }
    }

    public static void error(Object msg, Throwable ex) {
        Logger logger = PluginLogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(PluginLogUtils.getMsg(msg), ex);
        }
    }

    public static String getMsg(Object msg, Throwable ex) {
        String str = "";

        if (msg != null) {
            str = PluginLogUtils.getLogMethod() + "[" + msg.toString() + "]";
        } else {
            str = PluginLogUtils.getLogMethod() + "[null]";
        }
        if (ex != null) {
            str += "[" + ex.getMessage() + "]";
        }

        return str;
    }

    public static String getMsg(Object msg) {
        return PluginLogUtils.getMsg(msg, null);
    }

    /**
     * 得到调用类名称
     *
     * @return
     */
    private static String getLogClass() {
        String str = "";

        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        if (stack.length > 3) {
            StackTraceElement ste = stack[3];
            str = ste.getClassName();// 类名称
        }

        return str;
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