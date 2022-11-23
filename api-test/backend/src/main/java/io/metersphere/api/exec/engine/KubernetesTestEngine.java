package io.metersphere.api.exec.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExtendedParameter;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.resourcepool.engine.provider.ClientCredential;
import io.metersphere.xpack.resourcepool.engine.provider.KubernetesProvider;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.util.Set;

public class KubernetesTestEngine extends AbstractEngine {
    private JmeterRunRequestDTO runRequest;

    // 初始化API调用
    public KubernetesTestEngine(JmeterRunRequestDTO runRequest) {
        super.initApiConfig(runRequest);
        this.runRequest = runRequest;
    }

    private static Class<? extends KubernetesProvider> providerClass = null;

    static {
        Set<Class<? extends KubernetesProvider>> subTypes = new Reflections("io.metersphere.xpack").getSubTypesOf(KubernetesProvider.class);
        if (CollectionUtils.isNotEmpty(subTypes)) {
            providerClass = subTypes.stream().findFirst().get();
        }
    }

    @Override
    public void start() {
        LogUtil.info("k8s 执行开始", resourceList.size());
        resourceList.forEach(r -> {
            runApi(r);
        });
    }

    private void runApi(TestResource resource) {
        try {
            ClientCredential clientCredential = JSON.parseObject(resource.getConfiguration(), ClientCredential.class);
            KubernetesProvider kubernetesProvider = ConstructorUtils.invokeConstructor(providerClass, clientCredential);
            KubernetesClient client = kubernetesProvider.getClient();
            Pod pod = KubernetesApiExec.getExecPod(clientCredential, client);

            StringBuffer logMsg = new StringBuffer("当前报告：【" + runRequest.getReportId() + "】资源：【" + runRequest.getTestId() + "】")
                    .append(StringUtils.LF).append("namespace：").append(clientCredential.getNamespace())
                    .append(StringUtils.LF).append("Pod信息：【 ")
                    .append(JSON.toJSONString(pod.getMetadata())).append(" 】");
            LoggerUtil.info(logMsg);
            String path = "api/start";
            if (runRequest.getHashTree() != null) {
                path = "debug";
                if (runRequest.isDebug() && !StringUtils.equalsAny(runRequest.getRunMode(), ApiRunMode.DEFINITION.name())) {
                    runRequest.getExtendedParameters().put(ExtendedParameter.SAVE_RESULT, true);
                } else if (!runRequest.isDebug()) {
                    runRequest.getExtendedParameters().put(ExtendedParameter.SAVE_RESULT, true);
                }
                runRequest.setHashTree(null);
                LoggerUtil.info("进入DEBUG执行模式", runRequest.getReportId());
            }
            // 拼接CURL执行命令
            StringBuffer command = new StringBuffer("curl -H \"Accept: application/json\" -H \"Content-type: application/json\" -X POST -d").append(StringUtils.SPACE);
            command.append("'").append(JSON.toJSONString(runRequest)).append("'"); // 请求参数
            command.append(StringUtils.SPACE).append("--connect-timeout 30");  // 设置连接超时时间为30S
            command.append(StringUtils.SPACE).append("--max-time 120");  // 设置请求超时时间为120S
            command.append(StringUtils.SPACE).append("--retry 3");  // 设置重试次数3次
            command.append(StringUtils.SPACE).append("http://127.0.0.1:8082/jmeter/").append(path);
            KubernetesApiExec.newExecWatch(client, clientCredential.getNamespace(), pod.getMetadata().getName(), command.toString());
        } catch (Exception e) {
            LoggerUtil.error("当前报告：【" + runRequest.getReportId() + "】资源：【" + runRequest.getTestId() + "】CURL失败：", e);
            MSException.throwException(e);
        }
    }

    @Override
    public void stop() {

    }
}
