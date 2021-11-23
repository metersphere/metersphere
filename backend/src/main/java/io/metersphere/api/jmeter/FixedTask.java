package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FixedTask {
    public static Map<String, Long> tasks = new HashMap<>();
    public static Map<String, Integer> guardTask = new HashMap<>();
    private ApiScenarioReportService scenarioReportService;

    @Scheduled(cron = "*/6 * * * * ?")
    public void execute() {
        if (MessageCache.caseExecResourceLock.size() > 10000) {
            MessageCache.caseExecResourceLock.clear();
        }
        if (MessageCache.scenarioExecResourceLock.size() > 5000) {
            MessageCache.scenarioExecResourceLock.clear();
        }
        if (scenarioReportService == null) {
            scenarioReportService = CommonBeanFactory.getBean(ApiScenarioReportService.class);
        }
        if (MessageCache.cache != null && MessageCache.cache.size() > 0) {
            for (String key : MessageCache.cache.keySet()) {
                ReportCounter counter = MessageCache.cache.get(key);
                LogUtil.info("集成报告：【" + key + "】总执行场景：【" + counter.getReportIds().size() + "】已经执行完成场景：【" + counter.getCompletedIds().size() + "】");
                List<String> filterList = counter.getReportIds().stream().filter(t -> !counter.getCompletedIds().contains(t)).collect(Collectors.toList());

                LogUtil.debug("剩余要执行的报告" + JSON.toJSONString(filterList));
                // 合并
                if (counter.getCompletedIds().size() >= counter.getReportIds().size()) {
                    scenarioReportService.margeReport(key, counter.getReportIds());
                    guardTask.remove(key);
                    MessageCache.cache.remove(key);
                } else {
                    try {
                        if (guardTask.containsKey(key)) {
                            int number = guardTask.get(key);
                            number += 1;
                            guardTask.put(key, number);
                        } else {
                            guardTask.put(key, 0);
                        }
                        if (CollectionUtils.isNotEmpty(counter.getPoolUrls()) && counter.getCompletedIds().size() > 0 && guardTask.get(key) > 200) {
                            // 资源池中已经没有执行的请求了
                            int runningCount = scenarioReportService.get(key, counter);
                            if (runningCount == 0) {
                                LogUtil.error("发生未知异常，进行资源合并，请检查资源池是否正常运行");
                                scenarioReportService.margeReport(key, counter.getReportIds());
                                guardTask.remove(key);
                                MessageCache.cache.remove(key);
                            }
                        }
                    } catch (Exception ex) {
                        LogUtil.error(ex.getMessage());
                    }
                }
            }
        }
    }
}
