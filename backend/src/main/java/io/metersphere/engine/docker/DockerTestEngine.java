package io.metersphere.engine.docker;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.controller.request.TestRequest;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;

public class DockerTestEngine implements Engine {
    private EngineContext context;

    RestTemplate restTemplate;

    @Override
    public boolean init(EngineContext context) {
        this.restTemplate = CommonBeanFactory.getBean(RestTemplate.class);
        // todo 初始化操作
        this.context = context;
        return true;
    }

    @Override
    public void start() {
        // todo 运行测试
//        RestTemplate restTemplate = new RestTemplate();
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
            if (StringUtils.equals((String)h.get("State"), "running")) {
                MSException.throwException("the test is running!");
            }
        }

        restTemplate.postForObject(uri, testRequest, String.class);
    }

    @Override
    public void stop() {
        // TODO 停止运行测试
//        RestTemplate restTemplate = new RestTemplate();

        String testId = context.getTestId();

        String uri = "http://localhost:8082/jmeter/container/stop/" + testId;
        restTemplate.postForObject(uri, "", String.class);

    }
}
