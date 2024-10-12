package io.metersphere.api.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KubernetesProvider {

    private static final String RUNNING_PHASE = "Running";
    private static final String SHELL_COMMAND = "#!/bin/bash";

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

            String commandX = SHELL_COMMAND + StringUtils.LF + command + StringUtils.LF;

            LogUtils.info("执行命令：【 " + commandX + " 】");
            // 同步执行命令
            client.pods().inNamespace(client.getNamespace())
                    .withName(pod.getMetadata().getName())
                    .redirectingInput()
                    .writingOutput(System.out)
                    .writingError(System.err)
                    .withTTY()
                    .usingListener(new SimpleListener(runRequest))
                    .exec(commandX);
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
                // TODO: Add proper error handling based on response or task request details

            }
            throw new MSException("K8S 节点执行错误：" + t.getMessage(), t);
        }

        @Override
        public void onClose(int code, String reason) {
            LogUtils.info("K8s 监听关闭：code=" + code + ", reason=" + reason);
            // No additional actions needed for now
        }
    }
}
