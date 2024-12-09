package io.metersphere.api.jmeter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.FixedCapacityUtil;
import org.apache.commons.lang3.StringUtils;

public class JMeterLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private static final String THREAD_SPLIT = " ";
    private static final String SPACE = " ";
    private static final String LINE_FEED = "\n";
    private static final String UNKNOWN_HOST_EXCEPTION = "java.net.UnknownHostException";

    @Override
    public void append(ILoggingEvent event) {
        try {
            // Ensure the event level is not DEBUG and thread name is not empty
            if (!event.getLevel().levelStr.equals(LogUtil.DEBUG) && StringUtils.isNotEmpty(event.getThreadName())) {
                String threadName = StringUtils.substringBeforeLast(event.getThreadName(), THREAD_SPLIT);
                StringBuilder message = new StringBuilder();

                // Build the log message
                message.append(DateUtils.getTimeStr(event.getTimeStamp()))
                        .append(SPACE)
                        .append(event.getLevel())
                        .append(SPACE)
                        .append(event.getThreadName())
                        .append(SPACE)
                        .append(event.getFormattedMessage())
                        .append(LINE_FEED);

                // If there's a throwable associated with the event, append its details
                if (event.getThrowableProxy() != null) {
                    message.append(event.getThrowableProxy().getMessage())
                            .append(LINE_FEED)
                            .append(event.getThrowableProxy().getClassName())
                            .append(LINE_FEED);

                    // Append stack trace if present
                    StackTraceElementProxy[] stackTraceElements = event.getThrowableProxy().getStackTraceElementProxyArray();
                    if (stackTraceElements != null) {
                        for (StackTraceElementProxy stackTraceElementProxy : stackTraceElements) {
                            message.append("   ").append(stackTraceElementProxy.getSTEAsString()).append(LINE_FEED);
                        }
                    }
                }

                // Check for UnknownHostException and ensure thread is in FixedCapacityUtil before appending
                if (!message.toString().contains(UNKNOWN_HOST_EXCEPTION) && FixedCapacityUtil.containsKey(threadName)) {
                    FixedCapacityUtil.get(threadName).append(message);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
