package io.metersphere.api.exec.queue;

import io.metersphere.api.exec.utils.NamedThreadFactory;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.*;

@Service
public class ExecThreadPoolExecutor {
    // 线程池维护线程的最少数量
    private final static int CORE_POOL_SIZE = 10;
    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 10;
    // 线程池维护线程所允许的空闲时间
    private final static int KEEP_ALIVE_TIME = 1;
    // 线程池所使用的缓冲队列大小
    private final static int WORK_QUEUE_SIZE = 10000;

    private MsRejectedExecutionHandler msRejectedExecutionHandler = new MsRejectedExecutionHandler();
    /**
     * 创建线程池
     */
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(WORK_QUEUE_SIZE),
            new NamedThreadFactory("MS-JMETER-RUN-TASK"),
            msRejectedExecutionHandler);
    /**
     * 缓冲区调度线程池
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("MS-BUFFER-SCHEDULED"));

    public void addTask(JmeterRunRequestDTO requestDTO) {
        outApiThreadPoolExecutorLogger();
        ExecTask task = new ExecTask(requestDTO);
        threadPool.execute(task);
    }

    /**
     * 调度线程池，检查缓冲区
     */
    final ScheduledFuture scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
            //判断缓冲队列是否存在记录
            if (CollectionUtils.isNotEmpty(msRejectedExecutionHandler.getBufferQueue())) {
                //当线程池的队列容量少于WORK_QUEUE_SIZE，则开始把缓冲队列的任务 加入到 线程池
                if (threadPool.getQueue().size() < WORK_QUEUE_SIZE) {
                    JmeterRunRequestDTO requestDTO = msRejectedExecutionHandler.getBufferQueue().poll();
                    ExecTask task = new ExecTask(requestDTO);
                    threadPool.submit(task);
                    LoggerUtil.info("把缓冲区任务重新添加到线程池，报告ID：" + requestDTO.getReportId());
                }
            }
        }
    }, 0, 2, TimeUnit.SECONDS);


    /**
     * 终止线程池和调度线程池
     */
    public void shutdown() {
        //true表示如果定时任务在执行，立即中止，false则等待任务结束后再停止
        LoggerUtil.info("终止执行线程池和调度线程池：" + scheduledFuture.cancel(true));
        scheduler.shutdown();
        threadPool.shutdown();
    }

    /**
     * 保留两位小数
     */
    private String divide(int num1, int num2) {
        return String.format("%1.2f%%", Double.parseDouble(num1 + "") / Double.parseDouble(num2 + "") * 100);
    }

    public void outApiThreadPoolExecutorLogger() {
        ArrayBlockingQueue queue = (ArrayBlockingQueue) threadPool.getQueue();
        StringBuffer buffer = new StringBuffer("API 并发队列详情：\n");
        buffer.append(" 核心线程数：" + threadPool.getCorePoolSize()).append("\n");
        buffer.append(" 活动线程数：" + threadPool.getActiveCount()).append("\n");
        buffer.append(" 最大线程数：" + threadPool.getMaximumPoolSize()).append("\n");
        buffer.append(" 线程池活跃度：" + divide(threadPool.getActiveCount(), threadPool.getMaximumPoolSize())).append("\n");
        buffer.append(" 任务完成数：" + threadPool.getCompletedTaskCount()).append("\n");
        buffer.append(" 队列大小：" + (queue.size() + queue.remainingCapacity())).append("\n");
        buffer.append(" 当前排队线程数：" + (msRejectedExecutionHandler.getBufferQueue().size() + queue.size())).append("\n");
        buffer.append(" 队列剩余大小：" + queue.remainingCapacity()).append("\n");
        buffer.append(" 阻塞队列大小：" + PoolExecBlockingQueueUtil.queue.size()).append("\n");
        buffer.append(" 队列使用度：" + divide(queue.size(), queue.size() + queue.remainingCapacity()));

        LoggerUtil.info(buffer.toString());

        if (queue.size() > 0 && LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug(this.getWorkerQueue());
        }
    }

    public void setCorePoolSize(int maximumPoolSize) {
        try {
            int corePoolSize = maximumPoolSize > 500 ? 500 : maximumPoolSize;
            if (corePoolSize > CORE_POOL_SIZE) {
                threadPool.setCorePoolSize(corePoolSize);
            }
            threadPool.setMaximumPoolSize(maximumPoolSize);
            threadPool.allowCoreThreadTimeOut(true);
            LoggerUtil.info("AllCoreThreads: " + threadPool.prestartAllCoreThreads());
        } catch (Exception e) {
            LoggerUtil.error("设置线程参数异常：", e);
        }
    }

    public void removeQueue(String reportId) {
        // 检查缓冲区
        Queue<JmeterRunRequestDTO> bufferQueue = msRejectedExecutionHandler.getBufferQueue();
        if (CollectionUtils.isNotEmpty(bufferQueue)) {
            bufferQueue.forEach(item -> {
                if (item != null && StringUtils.equals(item.getReportId(), reportId)) {
                    bufferQueue.remove(item);
                }
            });
        }
        // 检查等待队列
        BlockingQueue workerQueue = threadPool.getQueue();
        workerQueue.forEach(item -> {
            ExecTask task = (ExecTask) item;
            if (task.getRequest() != null && StringUtils.equals(task.getRequest().getReportId(), reportId)) {
                workerQueue.remove(item);
            }
        });
    }

    public void removeAllQueue() {
        // 检查缓冲区
        msRejectedExecutionHandler.getBufferQueue().clear();
        // 检查等待队列
        threadPool.getQueue().clear();
    }

    public boolean check(String reportId) {
        // 检查缓冲区
        Queue<JmeterRunRequestDTO> bufferQueue = msRejectedExecutionHandler.getBufferQueue();
        if (CollectionUtils.isNotEmpty(bufferQueue)) {
            return bufferQueue.stream().filter(task -> StringUtils.equals(task.getReportId(), reportId)).count() > 0;
        }
        // 检查等待队列
        BlockingQueue workerQueue = threadPool.getQueue();
        return workerQueue.stream().filter(task -> StringUtils.equals(((ExecTask) task).getRequest().getReportId(), reportId)).count() > 0;
    }

    public boolean checkPlanReport(String planReportId) {
        // 检查缓冲区
        Queue<JmeterRunRequestDTO> bufferQueue = msRejectedExecutionHandler.getBufferQueue();
        if (CollectionUtils.isNotEmpty(bufferQueue)) {
            return bufferQueue.stream().filter(task -> StringUtils.equals(task.getTestPlanReportId(), planReportId)).count() > 0;
        }
        // 检查等待队列
        BlockingQueue workerQueue = threadPool.getQueue();
        return workerQueue.stream().filter(task -> StringUtils.equals(((ExecTask) task).getRequest().getTestPlanReportId(), planReportId)).count() > 0;
    }

    public String getWorkerQueue() {
        StringBuffer buffer = new StringBuffer();
        BlockingQueue workerQueue = threadPool.getQueue();
        workerQueue.forEach(item -> {
            ExecTask task = (ExecTask) item;
            if (task.getRequest() != null) {
                buffer.append("等待队列报告：【 " + task.getRequest().getReportId() + "】资源：【 " + task.getRequest().getTestId() + "】").append("\n");
            }
        });

        return buffer.toString();
    }
}
