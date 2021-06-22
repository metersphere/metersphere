package io.metersphere.api.jmeter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class JmeterLoggerAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    public static Map<Long, StringBuffer> logger;

    @Override
    public void append(ILoggingEvent event) {
        try {
            if (logger == null) {
                logger = new LinkedHashMap<>();
            }
            if (event.getLevel().levelStr.equals(LogUtil.INFO)) {
                StringBuffer message = new StringBuffer();
                message.append(DateUtils.getTimeStr(event.getTimeStamp())).append(" ")
                        .append(event.getLevel()).append(" ")
                        .append(event.getThreadName()).append(" ")
                        .append(event.getFormattedMessage()).append("\n");
                if (logger.containsKey(event.getTimeStamp())) {
                    logger.get(event.getTimeStamp()).append(message);
                } else {
                    logger.put(event.getTimeStamp(), message);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}