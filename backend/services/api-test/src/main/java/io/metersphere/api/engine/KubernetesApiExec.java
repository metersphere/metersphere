package io.metersphere.api.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.pool.TestResourceReturnDTO;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

public class KubernetesApiExec {

    public static Pod getExecPod(TestResourceReturnDTO credential, KubernetesClient client) {
        List<Pod> pods = client.pods().inNamespace(credential.getNamespace()).list().getItems();
        if (CollectionUtils.isEmpty(pods)) {
            throw new MSException("Execution node not found");
        }
        List<Pod> nodePods = pods.stream().filter(s -> s.getStatus().getPhase().equals("Running") && s.getMetadata().getGenerateName().startsWith(credential.getDeployName())).toList();
        if (CollectionUtils.isEmpty(nodePods)) {
            throw new MSException("Execution node not found");
        }
        return nodePods.get(new Random().nextInt(nodePods.size()));
    }

    public static void newExecWatch(KubernetesClient client, String namespace, String podName, String command, TaskRequest runRequest) {
        LogUtils.info("CURL 命令：【 " + command + " 】");
        client.pods().inNamespace(namespace).withName(podName)
                .redirectingInput()
                .writingOutput(System.out)
                .writingError(System.err)
                .withTTY()
                .usingListener(new SimpleListener(runRequest))
                .exec("sh", "-c", command);
    }

    private record SimpleListener(TaskRequest runRequest) implements ExecListener {
        @Override
        public void onOpen() {
            LogUtils.info("K8s命令执行监听 onOpen ", runRequest.getReportId());
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            LogUtils.info("进入K8s onFailure处理");
            if (runRequest != null) {
                LogUtils.info("请求参数：", JSON.toJSONString(runRequest));
                // TODO: 处理结束逻辑

            } else {
                throw new MSException("K8S 节点执行错误：" + t.getMessage());
            }
            LogUtils.error("K8S 节点执行错误：", t.getMessage());
        }

        @Override
        public void onClose(int code, String reason) {
            LogUtils.info(code + "_" + reason, runRequest.getReportId());
            LogUtils.info("K8s命令执行监听 onClose ", runRequest.getReportId());
        }
    }
}
