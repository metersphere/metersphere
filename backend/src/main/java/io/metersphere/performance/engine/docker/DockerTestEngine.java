package io.metersphere.performance.engine.docker;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.controller.ResultHolder;
import io.metersphere.dto.NodeDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.performance.engine.AbstractEngine;
import io.metersphere.performance.engine.EngineContext;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.performance.engine.docker.request.TestRequest;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

public class DockerTestEngine extends AbstractEngine {
    private static final String BASE_URL = "http://%s:%d";
    private RestTemplate restTemplate;
    private RestTemplate restTemplateWithTimeOut;

    public DockerTestEngine(LoadTestWithBLOBs loadTest) {
        this.init(loadTest);
    }

    @Override
    protected void init(LoadTestWithBLOBs loadTest) {
        super.init(loadTest);
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
        List<Integer> resourceRatio = resourceList.stream()
                .filter(r -> ResourceStatusEnum.VALID.name().equals(r.getStatus()))
                .map(r -> JSON.parseObject(r.getConfiguration(), NodeDTO.class).getMaxConcurrency())
                .collect(Collectors.toList());

        for (int i = 0, size = resourceList.size(); i < size; i++) {
            int ratio = resourceRatio.get(i);
            double realThreadNum = ((double) ratio / totalThreadNum) * threadNum;
            runTest(resourceList.get(i), Math.round(realThreadNum), i);
        }

    }

    private void runTest(TestResource resource, long realThreadNum, int resourceIndex) {
        EngineContext context = null;
        try {
            context = EngineFactory.createContext(loadTest, resource.getId(), realThreadNum, this.getStartTime(), this.getReportId(), resourceIndex);
        } catch (MSException e) {
            throw e;
        } catch (Exception e) {
            MSException.throwException(e);
        }

        String configuration = resource.getConfiguration();
        NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
        String nodeIp = node.getIp();
        Integer port = node.getPort();
        String testId = context.getTestId();
        String content = context.getContent();

        String uri = String.format(BASE_URL + "/jmeter/container/start", nodeIp, port);

        TestRequest testRequest = new TestRequest();
        testRequest.setSize(1);
        testRequest.setTestId(testId);
        testRequest.setFileString(content);
        testRequest.setImage(JMETER_IMAGE);
        testRequest.setTestData(context.getTestData());
        testRequest.setEnv(context.getEnv());


        ResultHolder result = restTemplate.postForObject(uri, testRequest, ResultHolder.class);
        if (result == null) {
            MSException.throwException(Translator.get("start_engine_fail"));
        }
        if (!result.isSuccess()) {
            MSException.throwException(result.getMessage());
        }
    }

    @Override
    public void stop() {
        String testId = loadTest.getId();
        this.resourceList.forEach(r -> {
            NodeDTO node = JSON.parseObject(r.getConfiguration(), NodeDTO.class);
            String ip = node.getIp();
            Integer port = node.getPort();

            String uri = String.format(BASE_URL + "/jmeter/container/stop/" + testId, ip, port);
            ResultHolder result = restTemplateWithTimeOut.getForObject(uri, ResultHolder.class);
            if (result == null) {
                MSException.throwException(Translator.get("container_delete_fail"));
            }
            if (!result.isSuccess()) {
                MSException.throwException(result.getMessage());
            }
        });
    }
}
