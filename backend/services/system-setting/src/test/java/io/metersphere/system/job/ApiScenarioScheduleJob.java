package io.metersphere.system.job;

import io.metersphere.system.schedule.BaseScheduleJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class ApiScenarioScheduleJob extends BaseScheduleJob {
    @Override
    protected void businessExecute(JobExecutionContext context) {
        /*
        TODO to Chen Jianxing:
            这里需要补充执行逻辑
            记得执行信息（环境、资源池、是否失败停止等配置）在jobDataMap里面
         */
    }

    public static JobKey getJobKey(String scenarioId) {
        return new JobKey(scenarioId, ApiScenarioScheduleJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String scenarioId) {
        return new TriggerKey(scenarioId, ApiScenarioScheduleJob.class.getName());
    }
}
