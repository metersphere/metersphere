package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaConfig;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.jmeter.LocalRunner;
import io.metersphere.performance.engine.Engine;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@Service
public class JMeterService {
    public static final String BASE_URL = "http://%s:%d";
    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        String JMETER_HOME = getJmeterHome();

        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public String getJmeterHome() {
        String home = getClass().getResource("/").getPath() + "jmeter";
        try {
            File file = new File(home);
            if (file.exists()) {
                return home;
            } else {
                return jmeterProperties.getHome();
            }
        } catch (Exception e) {
            return jmeterProperties.getHome();
        }
    }

    private void addDebugListener(String testId, HashTree testPlan) {
        MsDebugListener resultCollector = new MsDebugListener();
        resultCollector.setName(testId);
        resultCollector.setProperty(TestElement.TEST_CLASS, MsDebugListener.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ViewResultsFullVisualizer"));
        resultCollector.setEnabled(true);
        testPlan.add(testPlan.getArray()[0], resultCollector);
    }

    private void runLocal(JmeterRunRequestDTO request) {
        init();
        if (!FixedCapacityUtils.jmeterLogTask.containsKey(request.getReportId())) {
            FixedCapacityUtils.jmeterLogTask.put(request.getReportId(), System.currentTimeMillis());
        }
        if (StringUtils.isNotEmpty(request.getTestPlanReportId())
                && !FixedCapacityUtils.jmeterLogTask.containsKey(request.getTestPlanReportId())
                && StringUtils.equals(request.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            FixedCapacityUtils.jmeterLogTask.put(request.getTestPlanReportId(), System.currentTimeMillis());
        }
        LoggerUtil.debug("监听MessageCache.tasks当前容量：" + FixedCapacityUtils.jmeterLogTask.size());
        if (request.isDebug() && !StringUtils.equalsAny(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加同步接收结果 Listener");
            JMeterBase.addSyncListener(request, request.getHashTree(), APISingleResultListener.class.getCanonicalName());
        }
        if (request.isDebug()) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加Debug Listener");
            addDebugListener(request.getReportId(), request.getHashTree());
        } else {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加同步接收结果 Listener");
            JMeterBase.addSyncListener(request, request.getHashTree(), APISingleResultListener.class.getCanonicalName());
        }

        LocalRunner runner = new LocalRunner(request.getHashTree());
        runner.run(request.getReportId());
    }

    private void runNode(JmeterRunRequestDTO request) {
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        // 占位符
        String platformUrl = "http://localhost:8081";
        if (baseInfo != null) {
            platformUrl = baseInfo.getUrl();
        }
        // 临时存放
        String queueDetailId = request.getPlatformUrl();

        platformUrl += "/api/jmeter/download?testId="
                + request.getTestId()
                + "&reportId=" + request.getReportId()
                + "&runMode=" + request.getRunMode()
                + "&reportType=" + request.getReportType()
                + "&queueId=" + queueDetailId;

        request.setPlatformUrl(platformUrl);
        request.setKafkaConfig(KafkaConfig.getKafka());
        // 如果是K8S调用
        if (request.getPool().isK8s()) {
            try {
                LoggerUtil.error("开始发送请求[ " + request.getTestId() + " ] 到K8S节点执行");
                final Engine engine = EngineFactory.createApiEngine(request);
                engine.start();
            } catch (Exception e) {
                LoggerUtil.error("调用K8S执行请求[ " + request.getTestId() + " ] 失败：", e);
                ApiScenarioReportService apiScenarioReportService = CommonBeanFactory.getBean(ApiScenarioReportService.class);
                apiScenarioReportService.delete(request.getReportId());
                MSException.throwException(e.getMessage());
            }
        } else {
            this.send(request);
        }
    }

    private synchronized void send(JmeterRunRequestDTO request) {
        try {
            List<JvmInfoDTO> resources = GenerateHashTreeUtil.setPoolResource(request.getPoolId());
            if (CollectionUtils.isEmpty(resources)) {
                LoggerUtil.info("未获取到资源池，请检查配置【系统设置-系统-测试资源池】");
                RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                remakeReportService.remake(request);
                return;
            }

            int index = (int) (Math.random() * resources.size());
            JvmInfoDTO jvmInfoDTO = resources.get(index);
            TestResourceDTO testResource = jvmInfoDTO.getTestResource();
            String configuration = testResource.getConfiguration();
            NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
            request.setCorePoolSize(node.getMaxConcurrency());
            request.setEnable(node.isEnable());
            String nodeIp = node.getIp();
            Integer port = node.getPort();
            String uri = String.format(BASE_URL + "/jmeter/api/start", nodeIp, port);

            LoggerUtil.info("开始发送请求【 " + request.getReportId() + " 】,资源【 " + request.getTestId() + " 】" + uri + " 节点执行");
            ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
            if (result == null || !StringUtils.equals("SUCCESS", result.getBody())) {
                RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                remakeReportService.remake(request);
                LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 到" + uri + " 节点执行失败");
                LoggerUtil.info(result.getBody());
            }
        } catch (Exception e) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.remake(request);
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败：", e);
            LoggerUtil.error(e);
        }
    }


    public void run(JmeterRunRequestDTO request) {
        if (request.getPool().isPool()) {
            this.runNode(request);
        } else {
            CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).addTask(request);
        }
    }

    public void addQueue(JmeterRunRequestDTO request) {
        this.runLocal(request);
    }

    public boolean getRunningQueue(String poolId, String reportId) {
        try {
            List<JvmInfoDTO> resources = GenerateHashTreeUtil.setPoolResource(poolId);
            if (CollectionUtils.isEmpty(resources)) {
                return false;
            }
            boolean isRunning = false;
            for (JvmInfoDTO jvmInfoDTO : resources) {
                TestResourceDTO testResource = jvmInfoDTO.getTestResource();
                String configuration = testResource.getConfiguration();
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                String nodeIp = node.getIp();
                Integer port = node.getPort();
                String uri = String.format(BASE_URL + "/jmeter/get/running/queue/" + reportId, nodeIp, port);
                ResponseEntity<Boolean> result = restTemplate.getForEntity(uri, Boolean.class);
                if (result != null && result.getBody()) {
                    isRunning = true;
                    break;
                }
            }
            return isRunning;
        } catch (Exception e) {
            return false;
        }
    }
}
