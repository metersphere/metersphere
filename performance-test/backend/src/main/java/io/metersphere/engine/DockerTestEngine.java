package io.metersphere.engine;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.controller.handler.ResultHolder;
import io.metersphere.dto.NodeDTO;
import io.metersphere.request.StartTestRequest;
import io.metersphere.i18n.Translator;
import org.springframework.web.client.RestTemplate;

public class DockerTestEngine extends AbstractEngine {
    private static final String BASE_URL = "http://%s:%d";
    private RestTemplate restTemplate;
    private RestTemplate restTemplateWithTimeOut;

    public DockerTestEngine(LoadTestReportWithBLOBs loadTestReport) {
        this.init(loadTestReport);
    }

    @Override
    protected void init(LoadTestReportWithBLOBs loadTestReport) {
        super.init(loadTestReport);
        this.restTemplate = (RestTemplate) CommonBeanFactory.getBean("restTemplate");
        this.restTemplateWithTimeOut = (RestTemplate) CommonBeanFactory.getBean("restTemplateWithTimeOut");
    }

    @Override
    public void start() {
        Integer runningSumThreadNum = getRunningThreadNum();
        int totalThreadNum = resourceList.stream()
                .filter(r -> ResourceStatusEnum.VALID.name().equals(r.getStatus()))
                .map(r -> JSON.parseObject(r.getConfiguration(), NodeDTO.class).getMaxConcurrency())
                .reduce(Integer::sum)
                .orElse(0);
        if (threadNum > totalThreadNum - runningSumThreadNum) {
            MSException.throwException(Translator.get("max_thread_insufficient"));
        }
        Object[] resourceRatios = resourceList.stream()
                .filter(r -> ResourceStatusEnum.VALID.name().equals(r.getStatus()))
                .map(r -> JSON.parseObject(r.getConfiguration(), NodeDTO.class).getMaxConcurrency())
                .map(r -> r * 1.0 / totalThreadNum)
                .map(r -> String.format("%.2f", r))
                .toArray();
        // 保存一个 completeCount
        saveCompleteCount(resourceRatios);

        for (int i = 0, size = resourceList.size(); i < size; i++) {
            runTest(resourceList.get(i), resourceRatios, i);
        }
    }

    private void runTest(TestResource resource, Object[] ratios, int resourceIndex) {

        String configuration = resource.getConfiguration();
        NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
        String nodeIp = node.getIp();
        Integer port = node.getPort();

        StartTestRequest request = createStartTestRequest(resource, ratios, resourceIndex);

        String uri = String.format(BASE_URL + "/jmeter/container/start", nodeIp, port);
        try {
            ResultHolder result = restTemplate.postForObject(uri, request, ResultHolder.class);
            if (result == null) {
                MSException.throwException(Translator.get("start_engine_fail"));
            }
            if (!result.isSuccess()) {
                MSException.throwException(result.getMessage());
            }
        } catch (MSException e) {
            throw e;
        } catch (Exception e) {
            MSException.throwException("Please check node-controller status.");
        }
    }

    @Override
    public void stop() {
        String testId = loadTestReport.getTestId();
        this.resourceList.forEach(r -> {
            NodeDTO node = JSON.parseObject(r.getConfiguration(), NodeDTO.class);
            String ip = node.getIp();
            Integer port = node.getPort();

            String uri = String.format(BASE_URL + "/jmeter/container/stop/" + testId, ip, port);
            try {
                ResultHolder result = restTemplateWithTimeOut.getForObject(uri, ResultHolder.class);
                if (result == null) {
                    MSException.throwException(Translator.get("container_delete_fail"));
                }
                if (!result.isSuccess()) {
                    MSException.throwException(result.getMessage());
                }
            } catch (MSException e) {
                throw e;
            } catch (Exception e) {
                MSException.throwException("Please check node-controller status.");
            }
        });
    }

}
