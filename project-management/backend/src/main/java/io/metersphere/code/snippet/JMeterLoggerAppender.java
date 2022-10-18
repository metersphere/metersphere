package io.metersphere.code.snippet;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.code.snippet.util.FixedCapacityUtils;
import org.apache.commons.lang3.StringUtils;


public class JMeterLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    @Override
    public void append(ILoggingEvent event) {
        try {
            if (!event.getLevel().levelStr.equals(LogUtil.DEBUG)) {
                StringBuffer message = new StringBuffer();
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