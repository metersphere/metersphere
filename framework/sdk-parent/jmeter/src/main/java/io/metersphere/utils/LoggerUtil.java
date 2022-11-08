package io.metersphere.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggerUtil {
    //日志工具类
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
        return LoggerFactory.getLogger("ms-jmeter-run-log");
    }

    public static void writeLog(Object msg, String level) {
        Logger logger = LoggerUtil.getLogger();

        if (DEBUG.equals(level)) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(LoggerUtil.getMsg(msg));
            }
        } else if (INFO.equals(level)) {
            if (logger != null && logger.isInfoEnabled()) {
                logger.info(LoggerUtil.getMsg(msg));
            }
        } else if (WARN.equals(level)) {
            if (logger != null && logger.isWarnEnabled()) {
                logger.warn(LoggerUtil.getMsg(msg));
            }
        } else if (ERROR.equals(level)) {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error(LoggerUtil.getMsg(msg));
            }
        } else {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error("");
            }
        }
    }

    public static void info(Object msg) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LoggerUtil.getMsg(msg));
        }
    }

    public static void info(Object msg, Object o1) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LoggerUtil.getMsg(msg), o1);
        }
    }

    public static void info(String msg, String reportId) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LoggerUtil.getLogMethod() + "[REPORT-ID: " + reportId + "] " + msg);
        }
    }

    public static void info(String msg, String reportId, Object o1) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LoggerUtil.getLogMethod() + "[REPORT-ID: " + reportId + "] " + msg, o1);
        }
    }

    public static void info(Object msg, Object o1, Object o2) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LoggerUtil.getMsg(msg), o1, o2);
        }
    }

    public static void info(Object msg, Object[] obj) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LoggerUtil.getMsg(msg), obj);
        }
    }

    public static void debug(Object msg) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LoggerUtil.getMsg(msg));
        }
    }

    public static void debug(Object msg, Object o) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LoggerUtil.getMsg(msg), o);
        }
    }

    public static void debug(Object msg, Object o1, Object o2) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LoggerUtil.getMsg(msg), o1, o2);
        }
    }

    public static void debug(Object msg, Object[] obj) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LoggerUtil.getMsg(msg), obj);
        }
    }

    public static void warn(Object msg) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LoggerUtil.getMsg(msg));
        }
    }

    public static void warn(Object msg, Object o) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LoggerUtil.getMsg(msg), o);
        }
    }

    public static void warn(Object msg, Object o1, Object o2) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LoggerUtil.getMsg(msg), o1, o2);
        }
    }

    public static void warn(Object msg, Object[] obj) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LoggerUtil.getMsg(msg), obj);
        }
    }

    public static void error(Object msg) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LoggerUtil.getMsg(msg));// 并追加方法名称
        }
    }

    public static void error(Throwable e) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LoggerUtil.getMsg(e), e);// 同时打印错误堆栈信息
        }
    }

    public static void error(Object msg, Object o) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LoggerUtil.getMsg(msg), o);
        }
    }

    public static void error(String msg, String reportId, Object o1) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.error(LoggerUtil.getLogMethod() + "[REPORT-ID: " + reportId + "] " + msg, o1);
        }
    }

    public static void error(Object msg, Object o1, Object o2) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LoggerUtil.getMsg(msg), o1, o2);
        }
    }

    public static void error(Object msg, Object[] obj) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LoggerUtil.getMsg(msg), obj);
        }
    }

    public static void error(Object msg, Throwable ex) {
        Logger logger = LoggerUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LoggerUtil.getMsg(msg), ex);
        }
    }

    public static String getMsg(Object msg, Throwable ex) {
        String str = "";

        if (msg != null) {
            str = LoggerUtil.getLogMethod() + "[" + msg.toString() + "]";
        } else {
            str = LoggerUtil.getLogMethod() + "[null]";
        }
        if (ex != null) {
            str += "[" + ex.getMessage() + "]";
        }

        return str;
    }

    public static String getMsg(Object msg) {
        return LoggerUtil.getMsg(msg, null);
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

    public static String toString(Throwable e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            //将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }

    public static String getExceptionDetailsToStr(Exception e) {
        StringBuilder sb = new StringBuilder(e.toString());
        StackTraceElement[] stackElements = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackElements) {
            sb.append(stackTraceElement.toString());
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
