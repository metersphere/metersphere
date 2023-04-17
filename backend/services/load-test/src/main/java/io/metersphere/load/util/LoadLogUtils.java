package io.metersphere.load.util;

import io.metersphere.sdk.util.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadLogUtils extends LogUtils {

    /**
     * 初始化日志
     *
     * @return
     */
    public static Logger getLogger() {
        return LoggerFactory.getLogger("load-test");
    }

    public static void writeLog(Object msg, String level) {
        writeLog(msg, level, getLogger());
    }

    public static void info(Object msg) {
        info(msg, getLogger());
    }

    public static void info(Object msg, Object o1) {
        info(msg, o1, getLogger().atError());
    }

    public static void info(Object msg, Object o1, Object o2) {
        info(msg, o1, o2, getLogger());
    }

    public static void info(Object msg, Object[] obj) {
        info(msg, obj, getLogger());
    }

    public static void debug(Object msg) {
        debug(msg, getLogger());
    }

    public static void debug(Object msg, Object o) {
        debug(msg, o, getLogger());
    }

    public static void debug(Object msg, Object o1, Object o2) {
        debug(msg, o1, o2, getLogger());
    }

    public static void debug(Object msg, Object[] obj) {
        debug(msg, obj, getLogger());
    }

    public static void warn(Object msg) {
        warn(msg, getLogger());
    }

    public static void warn(Object msg, Object o) {
        warn(msg, o, getLogger());
    }

    public static void warn(Object msg, Object o1, Object o2) {
        warn(msg, o1, o2, getLogger());
    }

    public static void warn(Object msg, Object[] obj) {
        warn(msg, obj, getLogger());
    }

    public static void error(Object msg) {
        error(msg, getLogger());
    }

    public static void error(Throwable e) {
        error(e, getLogger());
    }

    public static void error(Object msg, Object o) {
        error(msg, o, getLogger());
    }

    public static void error(Object msg, Object o1, Object o2) {
        error(msg, o1, o2, getLogger());
    }

    public static void error(Object msg, Object[] obj) {
        error(msg, obj, getLogger());
    }

    public static void error(Object msg, Throwable ex) {
        error(msg, ex, getLogger());
    }
}
