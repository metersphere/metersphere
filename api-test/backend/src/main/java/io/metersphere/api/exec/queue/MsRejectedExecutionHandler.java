package io.metersphere.api.exec.queue;

import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class MsRejectedExecutionHandler implements RejectedExecutionHandler {
    /**
     * 执行任务缓冲队列,当线程池满了，则将任务存入到此缓冲队列
     * 这里是否搞个redis/写到磁盘？
     */
    private Queue<JmeterRunRequestDTO> bufferQueue = new LinkedBlockingQueue<>();

    public Queue<JmeterRunRequestDTO> getBufferQueue() {
        return bufferQueue;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        //任务加入到缓冲队列
        bufferQueue.offer(((ExecTask) r).getRequest());
        LoggerUtil.info("执行任务过多，任务加入缓冲区：" + ((ExecTask) r).getRequest().getReportId());
    }
}
