package io.metersphere.api.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.api.engine.provider.KubernetesApiProvider;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourceReturnDTO;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.Set;

public class KubernetesApiEngine extends AbstractApiEngine {
    private final TaskRequest request;

    // 初始化API调用
    public KubernetesApiEngine(TaskRequest request) {
        super.initConfig(request);
        this.request = request;
    }

    private static Class<? extends KubernetesApiProvider> providerClass = null;

    static {
        Set<Class<? extends KubernetesApiProvider>> subTypes = new Reflections("io.metersphere.xpack").getSubTypesOf(KubernetesApiProvider.class);
        if (CollectionUtils.isNotEmpty(subTypes)) {
            providerClass = subTypes.stream().findFirst().get();
        }
    }

    @Override
    public void start() {
        LogUtils.info("k8s执行START：", request.getReportId());
        runApi(resourcePool.getTestResourceReturnDTO());
    }

    private void runApi(TestResourceReturnDTO credential) {
        try {
            KubernetesApiProvider kubernetesProvider = ConstructorUtils.invokeConstructor(providerClass, credential);
            KubernetesClient client = kubernetesProvider.getClient();
            if (client == null) {
                throw new Exception("K8S Client is null");
            }

            Pod pod = KubernetesApiExec.getExecPod(credential, client);
            StringBuffer logMsg = new StringBuffer("当前报告：【" + request.getReportId() + " 】")
                    .append(StringUtils.LF).append("namespace：").append(credential.getNamespace())
                    .append(StringUtils.LF).append("Pod信息：【 ")
                    .append(JSON.toJSONString(pod.getMetadata())).append(" 】");
            LogUtils.info(logMsg);

            // 拼接CURL执行命令
            StringBuffer command = new StringBuffer("curl -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST -d");
            command.append(StringUtils.SPACE);
            command.append("'").append(JSON.toJSONString(request)).append("'"); // 请求参数
            command.append(StringUtils.SPACE).append("--connect-timeout 30");  // 设置连接超时时间为30S
            command.append(StringUtils.SPACE).append("--max-time 120");  // 设置请求超时时间为120S
            command.append(StringUtils.SPACE).append("--retry 3");  // 设置重试次数3次
            String LOCAL_URL = "http://127.0.0.1:8082/api/run";
            command.append(StringUtils.SPACE).append(LOCAL_URL);
            KubernetesApiExec.newExecWatch(client, credential.getNamespace(), pod.getMetadata().getName(), command.toString(), request);
        } catch (Exception e) {
            // TODO: 处理异常，执行报告回滚

            LogUtils.error("当前报告：【" + request.getReportId() + "】", e);
        }
    }

    @Override
    public void stop() {
        LogUtils.info("K8S执行STOP：", request.getReportId());
    }
}
