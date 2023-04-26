package io.metersphere.api.jmeter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.metersphere.api.util.FixedCapacityUtils;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.StringUtils;

public class JMeterLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final static String THREAD_SPLIT = " ";

    @Override
    public void append(ILoggingEvent event) {
        try {
            if (!event.getLevel().levelStr.equals(LogUtils.DEBUG) && StringUtils.isNotEmpty(event.getThreadName())) {
                StringBuffer message = new StringBuffer();
                String threadName = StringUtils.substringBeforeLast(event.getThreadName(), THREAD_SPLIT);
                message.append(DateUtils.getTimeStr(event.getTimeStamp())).append(StringUtils.SPACE)
                        .append(event.getLevel()).append(StringUtils.SPACE)
                        .append(event.getThreadName()).append(StringUtils.SPACE)
                        .append(event.getFormattedMessage()).append(StringUtils.LF);

                if (event.getThrowableProxy() != null) {
                    message.append(event.getThrowableProxy().getMessage()).append(StringUtils.LF);
                    message.append(event.getThrowableProxy().getClassName()).append(StringUtils.LF);
                    if (event.getThrowableProxy().getStackTraceElementProxyArray() != null) {
                        for (StackTraceElementProxy stackTraceElementProxy : event.getThrowableProxy().getStackTraceElementProxyArray()) {
                            message.append("   ").append(stackTraceElementProxy.getSTEAsString()).append(StringUtils.LF);
                        }
                    }
                }
                if (message != null && !message.toString().contains("java.net.UnknownHostException")
                        && FixedCapacityUtils.containsKey(threadName)) {
                    FixedCapacityUtils.get(threadName).append(message);
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }
}