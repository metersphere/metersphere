package io.metersphere.api.exec.engine;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.service.RemakeReportService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.resourcepool.engine.provider.ClientCredential;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        Pod pod = nodePods.get(new Random().nextInt(nodePods.size()));
        return pod;
    }

    public static ExecWatch newExecWatch(KubernetesClient client, String namespace, String podName, String command) {
        LoggerUtil.info("CURL 命令：【 " + command + " 】");
        return client.pods().inNamespace(namespace).withName(podName)
                .readingInput(System.in)
                .writingOutput(System.out)
                .writingError(System.err)
                .withTTY()
                .usingListener(new SimpleListener())
                .exec("sh", "-c", command);
    }

    private static String getQuery(String content) {
        Pattern regex = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = regex.matcher(content);
        StringBuilder sql = new StringBuilder();
        while (matcher.find()) {
            sql.append(matcher.group(1) + ",");
        }
        if (sql.length() > 0) {
            sql.deleteCharAt(sql.length() - 1);
        }
        return sql.toString();
    }

    private static class SimpleListener implements ExecListener {
        @Override
        public void onOpen(Response response) {
            LoggerUtil.info("The shell will remain open for 10 seconds.");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            List<String> value = response.request().url().queryParameterValues("command");
            if (CollectionUtils.isNotEmpty(value) && value.size() > 2 && value.get(2).startsWith("curl")) {
                String query = "{" + KubernetesApiExec.getQuery(value.get(2)) + "}";
                JmeterRunRequestDTO runRequest = JSON.parseObject(query, JmeterRunRequestDTO.class);
                if (runRequest != null) {
                    RemakeReportService apiScenarioReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                    apiScenarioReportService.testEnded(runRequest, response.networkResponse().message());
                } else {
                    MSException.throwException("K8S 节点执行错误：" + response.networkResponse().message());
                }
            } else {
                MSException.throwException("K8S 节点执行错误：" + response.networkResponse().message());
            }
            LoggerUtil.error("K8S 节点执行错误：" + JSON.toJSONString(value));
            LoggerUtil.error("K8S 节点执行错误：" + response.networkResponse());
        }

        @Override
        public void onClose(int code, String reason) {
            LoggerUtil.info("The shell will now close.");
        }
    }
}
