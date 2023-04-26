package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
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

    @Override
    public void run() {
        try {
            // 分三类存储
            Map<String, List<ResultDTO>> assortMap = new LinkedHashMap<>();
            List<ResultDTO> resultDTOS = new LinkedList<>();
            LoggerUtil.info("KAFKA解析结果任务开始解析结果", String.valueOf(record.key()));
            ResultDTO dto = this.formatResult();
            if (dto == null) {
                LoggerUtil.info("未获取到执行结果", String.valueOf(record.key()));
                return;
            }

            if (BooleanUtils.isTrue(dto.getHasEnded())) {
                redisTemplateService.delFilePath(dto.getReportId());
                resultDTOS.add(dto);
                // 全局并发队列
                PoolExecBlockingQueueUtil.offer(dto.getReportId());
                LoggerUtil.info("KAFKA消费结束：", record.key());
            }
            // 携带结果
            if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                String key = RUN_MODE_MAP.get(dto.getRunMode());
                if (assortMap.containsKey(key)) {
                    assortMap.get(key).add(dto);
                } else {
                    assortMap.put(key, new LinkedList<>() {{
                        this.add(dto);
                    }});
                }
            }
            if (MapUtils.isNotEmpty(assortMap)) {
                LoggerUtil.info("KAFKA消费执行内容存储开始", String.valueOf(record.key()));
                testResultService.batchSaveResults(assortMap);
                LoggerUtil.info("KAFKA消费执行内容存储结束", String.valueOf(record.key()));
            }
            // 更新执行结果
            if (CollectionUtils.isNotEmpty(resultDTOS)) {
                resultDTOS.forEach(testResult -> {
                    LoggerUtil.info("资源 " + testResult.getTestId() + " 整体执行完成", testResult.getReportId());
                    testResultService.testEnded(testResult);
                    LoggerUtil.info("执行队列处理：" + testResult.getQueueId(), testResult.getReportId());
                    apiExecutionQueueService.queueNext(testResult);
                    // 更新测试计划报告
                    LoggerUtil.info("Check Processing Test Plan report status：" + testResult.getQueueId() + "，" + testResult.getTestId(), testResult.getReportId());
                    apiExecutionQueueService.checkTestPlanCaseTestEnd(testResult.getTestId(), testResult.getRunMode(), testResult.getTestPlanReportId());
                });
            }
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", String.valueOf(record.key()), e);
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
