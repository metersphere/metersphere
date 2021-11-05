package io.metersphere.api.jmeter;

import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

public class JmeterThreadController {
    private final static String THREAD_SPLIT = " ";

    public static String stop(String name) {

        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();

        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        StringBuilder threadNames = new StringBuilder();
        for (int i = 0; i < noThreads; i++) {
            if (StringUtils.isNotEmpty(lstThreads[i].getName()) && lstThreads[i].getName().startsWith(name)) {
                String threadName = StringUtils.substringBeforeLast(lstThreads[i].getName(), THREAD_SPLIT);
                if (StringUtils.isNotEmpty(threadName)) {
                    MessageCache.executionQueue.remove(threadName);
                }
                System.out.println("异常强制处理线程编号：" + i + " = " + lstThreads[i].getName());
                LogUtil.error("异常强制处理线程编号：" + i + " = " + lstThreads[i].getName());
                threadNames.append(lstThreads[i].getName()).append("；");
                lstThreads[i].interrupt();
            }
        }
        return threadNames.toString();
    }
}
