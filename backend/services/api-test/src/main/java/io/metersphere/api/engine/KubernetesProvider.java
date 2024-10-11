package io.metersphere.api.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
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
     * 同步执行命令
     *
     * @param resource
     * @param command
     * @throws Exception
     */
    protected static void exec(TestResourceDTO resource, Object runRequest, String scriptId, String command) throws Exception {
        // 防止执行非法命令
        if (StringUtils.isEmpty(command) || !StringUtils.contains(command, scriptId)) {
            throw new MSException("Invalid command: " + command);
        }
        ExecWatch execWatch = null;
        try (KubernetesClient client = getKubernetesClient(resource)) {
            Pod pod = getExecPod(client, resource);
            LogUtils.info("当前执行 Pod：【 " + pod.getMetadata().getName() + " 】");
            // 创建文件
            String parameters = "cat <<EOF > " + scriptId + StringUtils.LF + JSON.toFormatJSONString(runRequest) + StringUtils.LF + "EOF" + StringUtils.LF;

            // 删除文件
            String deleteFile = "rm -f " + scriptId + StringUtils.LF;

            String commandX = SHELL_COMMAND + StringUtils.LF + parameters + command + StringUtils.LF + deleteFile;

            LogUtils.info("执行命令：【 " + commandX + " 】");
            // 同步执行命令
            execWatch = client.pods().inNamespace(client.getNamespace())
                    .withName(pod.getMetadata().getName())
                    .redirectingInput()
                    .writingOutput(System.out)
                    .writingError(System.err)
                    .withTTY().exec(commandX);

            // 等待命令执行完成，获取结果
            try (InputStream error = execWatch.getError()) {
                // 判断是否有错误输出
                if (error != null && error.available() > 0) {
                    throw new MSException("Kubernetes exec error");
                }
            }
        } finally {
            // 确保 ExecWatch 被关闭以释放资源
            if (execWatch != null) {
                execWatch.close();
            }
        }
    }

    /**
     * 异步执行命令
     *
     * @param resource
     * @param runRequest
     * @param command
     */
    public static void execCommand(TestResourceDTO resource, Object runRequest, String command) {
        try (KubernetesClient client = getKubernetesClient(resource)) {
            Pod pod = getExecPod(client, resource);
            LogUtils.info("当前执行 Pod：【 " + pod.getMetadata().getName() + " 】");
            client.pods().inNamespace(client.getNamespace()).withName(pod.getMetadata().getName())
                    .redirectingInput()
                    .writingOutput(System.out)
                    .writingError(System.err)
                    .withTTY()
                    .usingListener(new SimpleListener(runRequest))
                    .exec(SHELL_COMMAND, "-c", command);
        } catch (Exception e) {
            throw new MSException("Error during Kubernetes execution: " + e.getMessage(), e);
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
