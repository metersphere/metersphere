package io.metersphere.engine.docker;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.controller.request.TestRequest;
import io.metersphere.dto.NodeDTO;
import io.metersphere.engine.AbstractEngine;
import io.metersphere.engine.EngineContext;
import io.metersphere.engine.EngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

public class DockerTestEngine extends AbstractEngine {

    private RestTemplate restTemplate;


    @Override
    public boolean init(LoadTestWithBLOBs loadTest, FileMetadata fileMetadata, List<FileMetadata> csvFiles) {
        super.init(loadTest, fileMetadata, csvFiles);
        this.restTemplate = CommonBeanFactory.getBean(RestTemplate.class);
        // todo 初始化操作
        return true;
    }

    @Override
    public void start() {
        Integer runningSumThreadNum = getSumThreadNum();
        Integer integer = resourceList.stream().map(r -> {
            NodeDTO nodeDTO = JSON.parseObject(r.getConfiguration(), NodeDTO.class);
            return nodeDTO.getMaxConcurrency();
        }).reduce(Integer::sum).orElse(0);

        // todo 运行测试
        EngineContext context = null;
        try {
            context = EngineFactory.createContext(loadTest, jmxFile, csvFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String testId = context.getTestId();
        String content = context.getContent();

        String uri = "http://localhost:8082/jmeter/container/start";

        TestRequest testRequest = new TestRequest();
        testRequest.setSize(1);
        testRequest.setTestId(testId);
        testRequest.setFileString(content);

        // todo 判断测试状态
        String taskStatusUri = "http://localhost:8082/jmeter/task/status/" + testId;
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
//        RestTemplate restTemplate = new RestTemplate();

        String testId = loadTest.getId();

        String uri = "http://localhost:8082/jmeter/container/stop/" + testId;
        restTemplate.postForObject(uri, "", String.class);

    }
}
