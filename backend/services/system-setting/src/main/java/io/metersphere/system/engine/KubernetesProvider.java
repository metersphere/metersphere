package io.metersphere.system.engine;

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
import io.metersphere.sdk.dto.api.task.TaskBatchRequestDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.dto.pool.TestResourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.CollectionUtils;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KubernetesProvider {

    private static final String RUNNING_PHASE = "Running";
    private static final String SHELL_COMMAND = "sh";
    private static final int TIMEOUT = 120000;

    public static KubernetesClient getKubernetesClient(TestResourceDTO credential) {
        ConfigBuilder configBuilder = new ConfigBuilder()
                .withMasterUrl(credential.getIp())
                .withOauthToken(credential.getToken())
                .withTrustCerts(true)
                .withConnectionTimeout(TIMEOUT)  // 120秒连接超时
                .withRequestTimeout(TIMEOUT)    // 120秒请求超时
                .withNamespace(credential.getNamespace());

        return new KubernetesClientBuilder()
                .withConfig(configBuilder.build())
                .build();
    }

    public static Pod getExecPod(KubernetesClient client, TestResourceDTO credential) {
        List<Pod> nodePods = getPods(client, credential);
        if (CollectionUtils.isEmpty(nodePods)) {
            throw new MSException("Execution node not found");
        }
        return nodePods.get(ThreadLocalRandom.current().nextInt(nodePods.size()));
    }

    public static List<Pod> getPods(KubernetesClient client, TestResourceDTO credential) {
        return client.pods()
                .inNamespace(credential.getNamespace())
                .list().getItems()
                .stream()
                .filter(s -> RUNNING_PHASE.equals(s.getStatus().getPhase()) &&
                        StringUtils.isNotBlank(credential.getDeployName()) &&
                        StringUtils.isNotBlank(s.getMetadata().getGenerateName()) &&
                        StringUtils.startsWith(s.getMetadata().getGenerateName(), credential.getDeployName()))
                .toList();
    }

    /**
     * 执行命令
     *
     * @param resource 资源
     * @param command  命令
     */
    protected static void exec(TestResourceDTO resource, Object runRequest, String command) {
        KubernetesClient client = getKubernetesClient(resource);
        try {
            if (runRequest instanceof TaskBatchRequestDTO request) {
                // 均分给每一个 Pod
                List<Pod> pods = getPods(client, resource);
                if (pods.isEmpty()) {
                    throw new MSException("No available pods found for execution.");
                }

                // Distribute tasks across nodes
                List<TaskBatchRequestDTO> distributedTasks = distributeTasksAmongNodes(request, pods.size(), resource);

                // Execute distributed tasks on each pod
                for (int i = 0; i < pods.size(); i++) {
                    Pod pod = pods.get(i);
                    TaskBatchRequestDTO subTaskRequest = distributedTasks.get(i);
                    List<String> taskKeys = subTaskRequest.getTaskItems().stream()
                            .map(taskItem -> taskItem.getReportId() + "_" + taskItem.getResourceId())
                            .toList();

                    LogUtils.info("Sending batch tasks to pod {} for execution:\n{}", pod.getMetadata().getName(), taskKeys);
                    executeCommandOnPod(client, pod, subTaskRequest, command);
                }
            } else if (runRequest instanceof TaskRequestDTO) {
                // 随机一个 Pod 执行
                Pod pod = getExecPod(client, resource);
                LogUtils.info("Executing task on pod: {}", pod.getMetadata().getName());
                executeCommandOnPod(client, pod, runRequest, command);
            } else {
                // 发送给每一个 Pod
                LogUtils.info("Stop tasks [{}] on Pods", runRequest);
                List<Pod> nodesList = getPods(client, resource);
                for (Pod pod : nodesList) {
                    executeCommandOnPod(client, pod, runRequest, command);
                }
            }
        } catch (Exception e) {
            LogUtils.error("Failed to execute command", e);
            client.close();
            throw new MSException("Failed to execute command", e);
        }
    }

    /**
     * Distributes tasks across nodes for parallel execution.
     */
    private static List<TaskBatchRequestDTO> distributeTasksAmongNodes(TaskBatchRequestDTO request, int podCount, TestResourceDTO resource) {
        List<TaskBatchRequestDTO> distributedTasks = new ArrayList<>(podCount);

        for (int i = 0; i < request.getTaskItems().size(); i++) {
            int nodeIndex = i % podCount;
            TaskBatchRequestDTO distributeTask;
            if (distributedTasks.size() < podCount) {
                distributeTask = BeanUtils.copyBean(new TaskBatchRequestDTO(), request);
                distributeTask.setTaskItems(new ArrayList<>());
                distributedTasks.add(distributeTask);
            } else {
                distributeTask = distributedTasks.get(nodeIndex);
            }
            distributeTask.getTaskInfo().setPoolSize(resource.getConcurrentNumber());
            distributeTask.getTaskItems().add(request.getTaskItems().get(i));
        }
        return distributedTasks;
    }

    /**
     * Executes the curl command on a given Kubernetes pod.
     */
    private static void executeCommandOnPod(KubernetesClient client, Pod pod, Object runRequest, String command) {
        try {
            LogUtils.info("Executing command on pod {}: 【{}】", pod.getMetadata().getName(), command);

            // Execute the command on the pod
            client.pods().inNamespace(client.getNamespace())
                    .withName(pod.getMetadata().getName())
                    .redirectingInput()
                    .writingOutput(System.out)
                    .writingError(System.err)
                    .usingListener(new SimpleListener(runRequest, client))
                    .exec(SHELL_COMMAND, "-c", command);
        } catch (Exception e) {
            LogUtils.error("Failed to execute command on pod {} ", pod.getMetadata().getName(), e);
        }
    }

    private record SimpleListener(Object runRequest, KubernetesClient client) implements ExecListener {
        @Override
        public void onOpen() {
            LogUtils.info("K8s 开启监听");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            LogUtils.error("K8s 监听失败", t);
            if (runRequest != null) {
                handleGeneralError(runRequest, t);
            }
        }

        @Override
        public void onClose(int code, String reason) {
            LogUtils.info("K8s 监听关闭：code=" + code + ", reason=" + reason);
            // 关闭客户端
            client.close();
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

    public static boolean validateNamespaceExists(TestResourceDTO testResourceDTO) {
        LogUtils.info("Test resource config: {}", testResourceDTO);
        try (KubernetesClient kubernetesClient = getKubernetesClient(testResourceDTO)) {
            // 校验 KubernetesClient 是否为空，避免后续调用产生 NullPointerException
            if (kubernetesClient == null) {
                throw new IllegalArgumentException("Kubernetes client initialization failed. Please check your configuration.");
            }

            // 直接获取 pods 并进行非空判断
            List<Pod> pods = getPods(kubernetesClient, testResourceDTO);
            if (org.apache.commons.collections.CollectionUtils.isEmpty(pods)) {
                throw new RuntimeException("No execution pods found for the given resource: " + testResourceDTO.getNamespace());
            }
        } catch (Exception e) {
            LogUtils.error("Failed to validate namespace exists", e);
            return false;
        }
        return true;
    }

}
