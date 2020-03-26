package io.metersphere.engine.docker;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.constants.ResourceStatusEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.controller.request.TestRequest;
import io.metersphere.dto.NodeDTO;
import io.metersphere.engine.AbstractEngine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.kubernetes.registry.RegistryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DockerTestEngine extends AbstractEngine {
    private static final String BASE_URL = "http://%s:%d";

    private RestTemplate restTemplate;
    private RegistryService registryService;

    public DockerTestEngine(LoadTestWithBLOBs loadTest) {
        this.init(loadTest);
    }

    @Override
    protected void init(LoadTestWithBLOBs loadTest) {
        super.init(loadTest);
        this.restTemplate = CommonBeanFactory.getBean(RestTemplate.class);
        this.registryService = CommonBeanFactory.getBean(RegistryService.class);
        // todo 初始化操作
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
            MSException.throwException("Insufficient resources");
        }
        List<Integer> resourceRatio = resourceList.stream()
                .filter(r -> ResourceStatusEnum.VALID.name().equals(r.getStatus()))
                .map(r -> JSON.parseObject(r.getConfiguration(), NodeDTO.class).getMaxConcurrency())
                .collect(Collectors.toList());

        for (int i = 0, size = resourceList.size(); i < size; i++) {
            int ratio = resourceRatio.get(i);
            double realThreadNum = ((double) ratio / totalThreadNum) * threadNum;
            runTest(resourceList.get(i), Math.round(realThreadNum));
        }

    }

    private void runTest(TestResource resource, long realThreadNum) {
        // todo 运行测试
        EngineContext context = null;
        try {
            context = EngineFactory.createContext(loadTest, realThreadNum);
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
        testRequest.setImage(registryService.getRegistry() + JMETER_IMAGE);

        // todo 判断测试状态
        String taskStatusUri = String.format(BASE_URL + "/jmeter/task/status/" + testId, nodeIp, port);
        List containerList = restTemplate.getForObject(taskStatusUri, List.class);
        for (int i = 0; i < containerList.size(); i++) {
            HashMap h = (HashMap) containerList.get(i);
            if (StringUtils.equals((String) h.get("State"), "running")) {
                MSException.throwException("the test is running!");
            }
        }

        restTemplate.postForObject(uri, testRequest, String.class);
    }

    @Override
    public void stop() {
        // TODO 停止运行测试
        String testId = loadTest.getId();
        this.resourceList.forEach(r -> {
            NodeDTO node = JSON.parseObject(r.getConfiguration(), NodeDTO.class);
            String ip = node.getIp();
            Integer port = node.getPort();

            String uri = String.format(BASE_URL + "/jmeter/container/stop/" + testId, ip, port);
            restTemplate.postForObject(uri, "", String.class);
        });


    }
}
