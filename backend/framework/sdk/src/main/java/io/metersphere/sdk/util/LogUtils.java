package io.metersphere.sdk.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";

    /**
     * 初始化日志
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(LogUtils.getLogClass());
    }

    public static void writeLog(Object msg, String level) {
        Logger logger = LogUtils.getLogger();

        if (DEBUG.equals(level)) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(LogUtils.getMsg(msg));
            }
        } else if (INFO.equals(level)) {
            if (logger != null && logger.isInfoEnabled()) {
                logger.info(LogUtils.getMsg(msg));
            }
        } else if (WARN.equals(level)) {
            if (logger != null && logger.isWarnEnabled()) {
                logger.warn(LogUtils.getMsg(msg));
            }
        } else if (ERROR.equals(level)) {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error(LogUtils.getMsg(msg));
            }
        } else {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error(StringUtils.EMPTY);
            }
        }
    }

    public static void info(Object msg) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg));
        }
    }

    public static void info(String var1, Object... var2) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(var1, var2);
        }
    }


    public static void info(Object msg, Object o1) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg), o1);
        }
    }

    public static void info(Object msg, Object o1, Object o2) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void info(Object msg, Object[] obj) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtils.getMsg(msg), obj);
        }
    }

    public static void debug(Object msg) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg));
        }
    }

    public static void debug(Object msg, Object o) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg), o);
        }
    }

    public static void debug(Object msg, Object o1, Object o2) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void debug(Object msg, Object[] obj) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtils.getMsg(msg), obj);
        }
    }

    public static void warn(Object msg) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg));
        }
    }

    public static void warn(Object msg, Object o) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg), o);
        }
    }

    public static void warn(Object msg, Object o1, Object o2) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void warn(Object msg, Object[] obj) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtils.getMsg(msg), obj);
        }
    }

    public static void error(Object msg) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg));// 并追加方法名称
        }
    }

    public static void error(Throwable e) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(e), e);// 同时打印错误堆栈信息
        }
    }

    public static void error(Object msg, Object o) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), o);
        }
    }

    public static void error(Object msg, Object o1, Object o2) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), o1, o2);
        }
    }

    public static void error(Object msg, Object[] obj) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), obj);
        }
    }

    public static void error(Object msg, Throwable ex) {
        Logger logger = LogUtils.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtils.getMsg(msg), ex);
        }
    }

    public static String getMsg(Object msg, Throwable ex) {
        String str = "";

        if (msg != null) {
            str = LogUtils.getLogMethod() + "[" + msg.toString() + "]";
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
     * 得到调用类名称
     */
    private static String getLogClass() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        if (stack.length > 3) {
            StackTraceElement ste = stack[3];
            return ste.getClassName();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 得到调用方法名称
     */
    private static String getLogMethod() {
        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        if (ArrayUtils.isNotEmpty(stack) && stack.length > 4) {
            StackTraceElement ste = stack[4];
            return "Method[" + ste.getMethodName() + "]";
        }
        return StringUtils.EMPTY;
    }

    public static String toString(Throwable e) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);) {
            //将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            return sw.toString();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
}
