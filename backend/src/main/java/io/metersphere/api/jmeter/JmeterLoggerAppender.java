package io.metersphere.api.jmeter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;

public class JmeterLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    @Override
    public void append(ILoggingEvent event) {
        try {
            if (!event.getLevel().levelStr.equals(LogUtil.DEBUG)) {
                StringBuffer message = new StringBuffer();
                message.append(DateUtils.getTimeStr(event.getTimeStamp())).append(" ")
                        .append(event.getLevel()).append(" ")
                        .append(event.getThreadName()).append(" ")
                        .append(event.getFormattedMessage()).append("\n");

                if (event.getThrowableProxy() != null) {
                    message.append(event.getThrowableProxy().getMessage()).append("\n");
                    message.append(event.getThrowableProxy().getClassName()).append("\n");
                    if (event.getThrowableProxy().getStackTraceElementProxyArray() != null) {
                        for (StackTraceElementProxy stackTraceElementProxy : event.getThrowableProxy().getStackTraceElementProxyArray()) {
                            message.append("   ").append(stackTraceElementProxy.getSTEAsString()).append("\n");
                        }
                    }
                }
                if (message != null && !message.toString().contains("java.net.UnknownHostException")) {
                    if (FixedCapacityUtils.fixedCapacityCache.containsKey(event.getTimeStamp())) {
                        FixedCapacityUtils.fixedCapacityCache.get(event.getTimeStamp()).append(message);
                    } else {
                        FixedCapacityUtils.fixedCapacityCache.put(event.getTimeStamp(), message);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}