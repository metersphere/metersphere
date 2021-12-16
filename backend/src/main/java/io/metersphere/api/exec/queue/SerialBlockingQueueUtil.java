package io.metersphere.api.exec.queue;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.ResultDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SerialBlockingQueueUtil {
    // 只作用与串行任务
    public static Map<String, BlockingQueue<Object>> queue = new ConcurrentHashMap<>();
    public static final String END_SIGN = "RUN-END";
    private static final int QUEUE_SIZE = 1;


    public static void offer(ResultDTO dto, Object report) {
        String key = dto != null && StringUtils.equals(dto.getReportType(), RunModeConstants.SET_REPORT.toString()) ? dto.getReportId() + "_" + dto.getTestId() : dto.getReportId();
        if (StringUtils.isNotEmpty(key) && queue.containsKey(key)) {
            try {
                queue.get(key).offer(report);
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                queue.remove(key);
            }
        }
    }

    public static Object take(String key) {
        try {
            if (StringUtils.isNotEmpty(key) && !queue.containsKey(key)) {
                BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
                queue.put(key, blockingQueue);
                return blockingQueue.poll(3, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            LogUtil.error("获取队列失败：" + e.getMessage());
        }
        return null;
    }

    public static void remove(String key) {
        if (StringUtils.isNotEmpty(key) && queue.containsKey(key)) {
            queue.get(key).offer(END_SIGN);
            queue.remove(key);
        }
    }
}
