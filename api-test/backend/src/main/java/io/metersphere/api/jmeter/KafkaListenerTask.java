package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.ResultDTO;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.TestResultService;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.*;

@Data
public class KafkaListenerTask implements Runnable {
    private ConsumerRecord<?, String> record;
    private ApiExecutionQueueService apiExecutionQueueService;
    private TestResultService testResultService;

    private RedisTemplateService redisTemplateService;

    private static final Map<String, String> RUN_MODE_MAP = new HashMap<String, String>() {{
        this.put(ApiRunMode.SCHEDULE_API_PLAN.name(), "schedule-task");
        this.put(ApiRunMode.JENKINS_API_PLAN.name(), "schedule-task");
        this.put(ApiRunMode.MANUAL_PLAN.name(), "schedule-task");

        this.put(ApiRunMode.DEFINITION.name(), "api-test-case-task");
        this.put(ApiRunMode.JENKINS.name(), "api-test-case-task");
        this.put(ApiRunMode.API_PLAN.name(), "api-test-case-task");
        this.put(ApiRunMode.JENKINS_API_PLAN.name(), "api-test-case-task");
        this.put(ApiRunMode.MANUAL_PLAN.name(), "api-test-case-task");


        this.put(ApiRunMode.SCENARIO.name(), "api-scenario-task");
        this.put(ApiRunMode.SCENARIO_PLAN.name(), "api-scenario-task");
        this.put(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), "api-scenario-task");
        this.put(ApiRunMode.SCHEDULE_SCENARIO.name(), "api-scenario-task");
        this.put(ApiRunMode.JENKINS_SCENARIO_PLAN.name(), "api-scenario-task");

    }};

    public static final String ENV = "ENV";


    @Override
    public void run() {
        try {
            ResultDTO dto = this.formatResult();
            if (dto == null) {
                LoggerUtil.info("KAFKA监听未获取到执行结果", String.valueOf(record.key()));
                return;
            }
            LoggerUtil.info("KAFKA监听解任务开始处理结果：【"
                    + dto.getRequestResults().size() + "】", String.valueOf(record.key()));
            // 分三类存储
            Map<String, List<ResultDTO>> assortMap = new LinkedHashMap<>();
            // 携带结果
            if (CollectionUtils.isNotEmpty(dto.getRequestResults()) ||
                    (MapUtils.isNotEmpty(dto.getArbitraryData()) &&
                            dto.getArbitraryData().containsKey(ENV))) {
                String key = RUN_MODE_MAP.get(dto.getRunMode());
                assortMap.put(key, new LinkedList<>() {{
                    this.add(dto);
                }});
            }
            if (MapUtils.isNotEmpty(assortMap)) {
                testResultService.batchSaveResults(assortMap);
            }

            // 更新执行结果
            if (BooleanUtils.isTrue(dto.getHasEnded())) {
                redisTemplateService.delFilePath(dto.getReportId());
                LoggerUtil.info("KAFKA监听开始处理报告状态", dto.getReportId());
                testResultService.testEnded(dto);
                LoggerUtil.info("KAFKA监听开始处理执行队列", dto.getReportId());
                apiExecutionQueueService.queueNext(dto);
                // 更新测试计划报告
                LoggerUtil.info("Check Processing Test Plan report status：", dto.getReportId());
                apiExecutionQueueService.checkTestPlanCaseTestEnd(dto.getTestId(), dto.getRunMode(), dto.getTestPlanReportId());
                LoggerUtil.info("KAFKA监听整体资源处理结束", dto.getReportId());
            }
        } catch (Exception e) {
            LoggerUtil.error("KAFKA监听消费失败", String.valueOf(record.key()), e);
        }
    }

    private ResultDTO formatResult() {
        try {
            if (StringUtils.isNotEmpty(record.value())) {
                return JSON.parseObject(record.value(), new TypeReference<ResultDTO>() {
                });
            }
        } catch (Exception e) {
            LoggerUtil.error("结果数据格式化失败：", String.valueOf(record.key()), e);
        }
        return null;
    }
}
