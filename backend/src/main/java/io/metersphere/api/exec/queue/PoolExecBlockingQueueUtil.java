package io.metersphere.api.exec.queue;

import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PoolExecBlockingQueueUtil {
    // 系统级队列控制整体并发数量
    public static Map<String, BlockingQueue<Object>> queue = new ConcurrentHashMap<>();

    public static void init(String key, int queueSize) {
        BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(queueSize);
        queue.put(key, blockingQueue);
    }

    public static void offer(String key) {
        if (StringUtils.isNotEmpty(key) && queue.containsKey(key)) {
            try {
                if (!queue.get(key).offer("end")) {
                    queue.get(key).add("end");
                }
            } catch (Exception e) {
                LogUtil.error(e);
            } finally {
                queue.get(key).offer("end");
                queue.remove(key);
            }
        }
    }

    public static Object take(String key) {
        try {
            if (StringUtils.isNotEmpty(key) && queue.containsKey(key)) {
                return queue.get(key).poll(5, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            LogUtil.error("获取队列失败：" + e.getMessage());
            return null;
        } finally {
            if (StringUtils.isNotEmpty(key)) {
                queue.remove(key);
            }
        }
        return null;
    }

    public static void remove(String key) {
        if (StringUtils.isNotEmpty(key) && queue.containsKey(key)) {
            queue.get(key).add("end");
            queue.remove(key);
        }
    }
}
