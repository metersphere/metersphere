package io.metersphere.api.utils;

import io.metersphere.sdk.util.LogUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskRunnerUtils {

    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 10;
    // 线程池维护线程所允许的空闲时间
    private final static int KEEP_ALIVE_TIME = 1;
    // 线程池所使用的缓冲队列大小
    private final static int WORK_QUEUE_SIZE = 50000;

    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            MAX_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(WORK_QUEUE_SIZE));

    public static void executeThreadPool(ExecTask task) {
        try {
            // 开始执行任务
            threadPool.execute(task);

            LogUtils.info("当前线程池活跃线程数量：{}，当前线程池线程数量：{}，当前线程池队列数量：{}",
                    threadPool.getActiveCount(),
                    threadPool.getPoolSize(),
                    threadPool.getQueue().size());
        } catch (Exception e) {
            LogUtils.error("KAFKA消费失败：", e);
        }
    }

    public static void setThreadPoolSize(int poolSize) {
        try {
            if (poolSize > 10 && poolSize < 500 && poolSize != threadPool.getMaximumPoolSize()) {
                threadPool.setMaximumPoolSize(poolSize);
                threadPool.setCorePoolSize(poolSize);
                threadPool.allowCoreThreadTimeOut(true);
                LogUtils.info("Set successfully: " + threadPool.prestartAllCoreThreads());
            }
            LogUtils.info("Invalid thread pool size: " + poolSize);
        } catch (Exception e) {
            LogUtils.error("设置线程参数异常", e);
        }
    }
}
