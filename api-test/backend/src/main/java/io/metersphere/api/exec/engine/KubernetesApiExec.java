package io.metersphere.api.exec.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.service.RemakeReportService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.resourcepool.engine.provider.ClientCredential;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class KubernetesApiExec {

    public static Pod getExecPod(ClientCredential credential, KubernetesClient client) {
        List<Pod> pods = client.pods().inNamespace(credential.getNamespace()).list().getItems();
        if (CollectionUtils.isEmpty(pods)) {
            MSException.throwException("Execution node not found");
        }
        List<Pod> nodePods = pods.stream().filter(s -> s.getStatus().getPhase().equals("Running") && s.getMetadata().getGenerateName().startsWith(credential.getDeployName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(nodePods)) {
            MSException.throwException("Execution node not found");
        }
        return nodePods.get(new Random().nextInt(nodePods.size()));
    }

    public static void newExecWatch(KubernetesClient client, String namespace, String podName, String command, JmeterRunRequestDTO runRequest) {
        LoggerUtil.info("CURL 命令：【 " + command + " 】");
        client.pods().inNamespace(namespace).withName(podName)
                .readingInput(System.in)
                .writingOutput(System.out)
                .writingError(System.err)
                .withTTY()
                .usingListener(new SimpleListener(runRequest))
                .exec("sh", "-c", command);
    }

    private record SimpleListener(JmeterRunRequestDTO runRequest) implements ExecListener {

        @Override
            public void onOpen() {
                LoggerUtil.info("K8s命令执行监听 onOpen ", runRequest.getReportId());
            }

            @Override
            public void onFailure(Throwable t, Response response) {
                LoggerUtil.info("进入K8s onFailure处理");
                if (runRequest != null) {
                    LoggerUtil.info("请求参数：", JSON.toJSONString(runRequest));
                    RemakeReportService apiScenarioReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                    apiScenarioReportService.testEnded(runRequest, StringUtils.join("K8s执行异常：", t.getMessage()));
                } else {
                    MSException.throwException("K8S 节点执行错误：" + t.getMessage());
                }
                LoggerUtil.error("K8S 节点执行错误：", t.getMessage());
            }

            @Override
            public void onClose(int code, String reason) {
                LoggerUtil.info(code + "_" + reason, runRequest.getReportId());
                LoggerUtil.info("K8s命令执行监听 onClose ", runRequest.getReportId());
            }
        }
}
