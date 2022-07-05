package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.*;

@Data
public class KafkaListenerTask implements Runnable {
    private ConsumerRecord<?, String> record;
    private ApiExecutionQueueService apiExecutionQueueService;
    private TestResultService testResultService;
    private ObjectMapper mapper;

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
            LoggerUtil.info("报告【" + record.key() + "】开始解析结果");
            ResultDTO dto = this.formatResult();
            if (dto == null) {
                return;
            }
            if (dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("TEST_END")
                    && (Boolean) dto.getArbitraryData().get("TEST_END")) {
                resultDTOS.add(dto);
                LoggerUtil.info("KAFKA消费结果处理：【" + record.key() + "】结果状态：" + dto.getArbitraryData().get("TEST_END"));
            }
            // 携带结果
            if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                String key = RUN_MODE_MAP.get(dto.getRunMode());
                if (assortMap.containsKey(key)) {
                    assortMap.get(key).add(dto);
                } else {
                    assortMap.put(key, new LinkedList<ResultDTO>() {{
                        this.add(dto);
                    }});
                }
            }
            if (MapUtils.isNotEmpty(assortMap)) {
                LoggerUtil.info("KAFKA消费执行内容存储开始");
                testResultService.batchSaveResults(assortMap);
                LoggerUtil.info("KAFKA消费执行内容存储结束");
            }
            // 更新执行结果
            if (CollectionUtils.isNotEmpty(resultDTOS)) {
                resultDTOS.forEach(testResult -> {
                    LoggerUtil.info("报告 【 " + testResult.getReportId() + " 】资源 " + testResult.getTestId() + " 整体执行完成");
                    testResultService.testEnded(testResult);
                    LoggerUtil.info("执行队列处理：" + testResult.getQueueId());
                    apiExecutionQueueService.queueNext(testResult);
                    // 全局并发队列
                    PoolExecBlockingQueueUtil.offer(testResult.getReportId());
                    // 更新测试计划报告
                    if (StringUtils.isNotEmpty(testResult.getTestPlanReportId())) {
                        LoggerUtil.info("Check Processing Test Plan report status：" + testResult.getQueueId() + "，" + testResult.getTestId());
                        apiExecutionQueueService.testPlanReportTestEnded(testResult.getTestPlanReportId());
                    }
                });
            }
        } catch (Exception e) {
            LoggerUtil.error("报告【" + record.key() + "】KAFKA消费失败：", e);
        }
    }

    private ResultDTO formatResult() {
        try {
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (StringUtils.isNotEmpty(record.value())) {
                return mapper.readValue(record.value(), new TypeReference<ResultDTO>() {
                });
            }
        } catch (Exception e) {
            LoggerUtil.error("报告【" + record.key() + "】格式化数据失败：", e);
        }
        return null;
    }
}
