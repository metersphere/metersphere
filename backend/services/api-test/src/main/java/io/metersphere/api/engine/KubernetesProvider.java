package io.metersphere.api.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.constants.MsgType;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.dto.SocketMsgDTO;
import io.metersphere.sdk.dto.api.result.ProcessResultDTO;
import io.metersphere.sdk.dto.api.result.TaskResultDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.WebSocketUtils;
import io.metersphere.system.dto.pool.TestResourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KubernetesProvider {

    private static final String RUNNING_PHASE = "Running";
    private static final String SHELL_COMMAND = "sh";

    public static KubernetesClient getKubernetesClient(TestResourceDTO credential) {
        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(credential.getIp())
                .withOauthToken(credential.getToken())
                .withTrustCerts(true)
                .withNamespace(credential.getNamespace());

        return new KubernetesClientBuilder()
                .withConfig(configBuilder.build())
                .build();
    }

    public static Pod getExecPod(KubernetesClient client, TestResourceDTO credential) {
        List<Pod> nodePods = client.pods()
                .inNamespace(credential.getNamespace())
                .list().getItems()
                .stream()
                .filter(s -> RUNNING_PHASE.equals(s.getStatus().getPhase()) && StringUtils.startsWith(s.getMetadata().getGenerateName(), "task-runner"))
                .toList();

        if (CollectionUtils.isEmpty(nodePods)) {
            throw new MSException("Execution node not found");
        }
        return nodePods.get(ThreadLocalRandom.current().nextInt(nodePods.size()));
    }

    /**
     * 执行命令
     *
     * @param resource
     * @param command
     */
    protected static void exec(TestResourceDTO resource, Object runRequest, String command) {
        try (KubernetesClient client = getKubernetesClient(resource)) {
            Pod pod = getExecPod(client, resource);
            LogUtils.info("当前执行 Pod：【 " + pod.getMetadata().getName() + " 】");
            LogUtils.info("执行命令：【 " + command + " 】");
            // 同步执行命令
            client.pods().inNamespace(client.getNamespace())
                    .withName(pod.getMetadata().getName())
                    .redirectingInput()
                    .writingOutput(System.out)
                    .writingError(System.err)
                    .withTTY()
                    .usingListener(new SimpleListener(runRequest))
                    .exec(SHELL_COMMAND, "-c", command + StringUtils.LF);
        }
    }

    private record SimpleListener(Object runRequest) implements ExecListener {
        @Override
        public void onOpen() {
            LogUtils.info("K8s 开启监听");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            LogUtils.error("K8s 监听失败", t);
            if (runRequest != null) {
                LogUtils.info("请求参数：{}", JSON.toJSONString(runRequest));
                handleGeneralError(runRequest, t);
                return;
            }
            throw new MSException("K8S 节点执行错误：" + t.getMessage(), t);
        }

        @Override
        public void onClose(int code, String reason) {
            LogUtils.info("K8s 监听关闭：code=" + code + ", reason=" + reason);
            // No additional actions needed for now
        }
    }

    private static void handleGeneralError(Object requestObj, Throwable e) {
        // 检查请求对象是否为 TaskRequestDTO 类型
        if (requestObj instanceof TaskRequestDTO request) {
            // 发送结果到 WebSocket，如果报告 ID 存在
            String reportId = request.getTaskItem().getReportId();
            if (WebSocketUtils.has(reportId)) {
                SocketMsgDTO socketMsgDTO = new SocketMsgDTO(
                        reportId,
                        request.getTaskInfo().getRunMode(),
                        MsgType.EXEC_END.name(),
                        e.getMessage()
                );
                WebSocketUtils.sendMessageSingle(socketMsgDTO);
            }

            // 尝试获取 KafkaTemplate 并发送任务结果
            KafkaTemplate<String, String> kafkaTemplate = CommonBeanFactory.getBean(KafkaTemplate.class);
            if (kafkaTemplate != null) {
                TaskResultDTO result = buildTaskResult(request, e);
                kafkaTemplate.send(KafkaTopicConstants.API_REPORT_TOPIC, JSON.toJSONString(result));
            }
        }
    }

    private static TaskResultDTO buildTaskResult(TaskRequestDTO request, Throwable e) {
        // 创建并配置 TaskResultDTO
        TaskResultDTO result = new TaskResultDTO();
        result.setRequest(request);
        result.setRequestResults(List.of()); // 空的请求结果列表
        result.setHasEnded(true); // 标记任务已结束

        // 创建并配置 ProcessResultDTO
        ProcessResultDTO processResultDTO = new ProcessResultDTO();
        processResultDTO.setStatus(ResultStatus.ERROR.name());

        result.setProcessResultDTO(processResultDTO);
        result.setConsole(e.getMessage()); // 将异常信息记录到控制台日志

        return result;
    }
}
