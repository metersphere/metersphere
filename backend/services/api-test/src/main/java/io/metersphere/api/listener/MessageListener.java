package io.metersphere.api.listener;

import io.metersphere.api.event.ApiEventSource;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.service.ApiReportSendNoticeService;
import io.metersphere.api.service.definition.ApiTestCaseBatchRunService;
import io.metersphere.api.service.queue.ApiExecutionQueueService;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.dto.queue.ExecutionQueue;
import io.metersphere.sdk.dto.queue.ExecutionQueueDetail;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {
    public static final String MESSAGE_CONSUME_ID = "MS-API-MESSAGE-CONSUME";
    @Resource
    private ApiReportSendNoticeService apiReportSendNoticeService;

    @Resource
    private ApiEventSource apiEventSource;

    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private ApiTestCaseBatchRunService apiTestCaseBatchRunService;
    @Resource
    private ApiScenarioBatchRunService apiScenarioBatchRunService;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;

    @KafkaListener(id = MESSAGE_CONSUME_ID, topics = KafkaTopicConstants.API_REPORT_TASK_TOPIC, groupId = MESSAGE_CONSUME_ID)
    public void messageConsume(ConsumerRecord<?, String> record) {
        try {
            LogUtils.info("接收到发送通知信息：{}", record.key());
            if (ObjectUtils.isNotEmpty(record.value())) {
                ApiNoticeDTO dto = JSON.parseObject(record.value(), ApiNoticeDTO.class);

                // 集合报告不发送通知
                if (!BooleanUtils.isTrue(dto.getIntegratedReport())) {
                    apiReportSendNoticeService.sendNotice(dto);
                    // TODO 通知测试计划处理后续
                    LogUtils.info("发送通知给测试计划：{}", record.key());
                    apiEventSource.fireEvent(ApplicationScope.API_TEST, record.value());
                } else {
                    ApiExecuteResourceType resourceType = EnumValidator.validateEnum(ApiExecuteResourceType.class, dto.getResourceType());
                    boolean isStop = switch (resourceType) {
                        case API_CASE ->
                                StringUtils.equals(apiReportMapper.selectByPrimaryKey(dto.getReportId()).getExecStatus(), ExecStatus.STOPPED.name())
                                        && deleteQueue(dto.getQueueId());
                        case API_SCENARIO ->
                                StringUtils.equals(apiScenarioReportMapper.selectByPrimaryKey(dto.getReportId()).getExecStatus(), ExecStatus.STOPPED.name())
                                        && deleteQueue(dto.getQueueId());
                        default -> false;
                    };

                    if (isStop) {
                        return;
                    }
                }

                executeNextTask(dto);
            }
        } catch (Exception e) {
            LogUtils.error("接收到发送通知信息：{}", e);
        }
    }

    private boolean deleteQueue(String queueId) {
        apiExecutionQueueService.deleteQueue(queueId);
        return true;
    }

    /**
     * 执行批量的下一个任务
     *
     * @param dto
     */
    private void executeNextTask(ApiNoticeDTO dto) {
        if (StringUtils.isBlank(dto.getQueueId())) {
            return;
        }
        try {
            ExecutionQueue queue = apiExecutionQueueService.getQueue(dto.getQueueId());
            // 串行才执行下个任务
            if (queue == null || BooleanUtils.isTrue(queue.getRunModeConfig().isParallel())) {
                return;
            }
            ApiExecuteResourceType resourceType = EnumValidator.validateEnum(ApiExecuteResourceType.class, queue.getResourceType());

            if (isStopOnFailure(dto, queue, resourceType)) {
                // 失败停止，不执行后续任务
                return;
            }

            ExecutionQueueDetail nextDetail = apiExecutionQueueService.getNextDetail(dto.getQueueId());
            switch (resourceType) {
                case API_CASE -> apiTestCaseBatchRunService.executeNextTask(queue, nextDetail);
                case API_SCENARIO -> apiScenarioBatchRunService.executeNextTask(queue, nextDetail);
                default -> {
                }
            }
        } catch (Exception e) {
            LogUtils.error("执行任务失败：", e);
        }
    }

    /**
     * 处理失败停止后的报告处理
     *
     * @param dto
     * @param queue
     * @param resourceType
     * @return
     */
    private boolean isStopOnFailure(ApiNoticeDTO dto, ExecutionQueue queue, ApiExecuteResourceType resourceType) {
        if (BooleanUtils.isTrue(queue.getRunModeConfig().getStopOnFailure()) && StringUtils.equals(dto.getReportStatus(), ReportStatus.ERROR.name())) {
            switch (resourceType) {
                case API_CASE -> apiTestCaseBatchRunService.updateStopOnFailureApiReport(queue);
                case API_SCENARIO -> apiScenarioBatchRunService.updateStopOnFailureReport(queue);
                default -> {
                }
            }
            // 如果是失败停止，清空队列，不继续执行
            apiExecutionQueueService.deleteQueue(queue.getQueueId());
            return true;
        }
        return false;
    }
}
