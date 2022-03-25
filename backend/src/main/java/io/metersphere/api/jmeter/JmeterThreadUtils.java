package io.metersphere.api.jmeter;

import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

public class JmeterThreadUtils {

    public static String stop(String name) {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        StringBuilder threadNames = new StringBuilder();
        for (int i = 0; i < noThreads; i++) {
            if (StringUtils.isNotEmpty(lstThreads[i].getName()) && lstThreads[i].getName().startsWith(name)) {
                System.out.println("异常强制处理线程编号：" + i + " = " + lstThreads[i].getName());
                LogUtil.error("异常强制处理线程编号：" + i + " = " + lstThreads[i].getName());
                threadNames.append(lstThreads[i].getName()).append("；");
                lstThreads[i].interrupt();
            }
        }
        return threadNames.toString();
    }

    public static boolean isRunning(String reportId, String testId) {
        if (StringUtils.isEmpty(reportId)) {
            return false;
        }
        if (PoolExecBlockingQueueUtil.queue.containsKey(reportId)) {
            return true;
        }
        if (CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).check(reportId)) {
            return true;
        }
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int noThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[noThreads];
        currentGroup.enumerate(lstThreads);
        for (int i = 0; i < noThreads; i++) {
            if (lstThreads[i] != null) {
                if (StringUtils.isNotEmpty(reportId) && StringUtils.isNotEmpty(lstThreads[i].getName()) && lstThreads[i].getName().startsWith(reportId)) {
                    return true;
                } else if (StringUtils.isNotEmpty(testId) && StringUtils.isNotEmpty(lstThreads[i].getName()) && lstThreads[i].getName().startsWith(testId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
