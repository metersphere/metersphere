package io.metersphere.api.jmeter;


import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.exec.engine.EngineFactory;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.jmeter.utils.ServerConfig;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.config.KafkaConfig;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ExtendedParameter;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.config.JmeterProperties;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.*;
import io.metersphere.engine.Engine;
import io.metersphere.jmeter.JMeterBase;
import io.metersphere.jmeter.LocalRunner;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.RemakeReportService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
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
    public static final String POOL = "POOL";

    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private BaseProjectApplicationService projectApplicationService;

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

    /**
     * 添加调试监听
     *
     * @param testId
     * @param testPlan
     */
    private void addDebugListener(String testId, HashTree testPlan) {
        MsDebugListener resultCollector = new MsDebugListener();
        resultCollector.setName(testId);
        resultCollector.setProperty(TestElement.TEST_CLASS, MsDebugListener.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ViewResultsFullVisualizer"));
        resultCollector.setEnabled(true);

        // 添加DEBUG标示
        HashTree test = ArrayUtils.isNotEmpty(testPlan.getArray()) ? testPlan.getTree(testPlan.getArray()[0]) : null;
        if (test != null && ArrayUtils.isNotEmpty(test.getArray()) && test.getArray()[0] instanceof ThreadGroup) {
            ThreadGroup group = (ThreadGroup) test.getArray()[0];
            group.setProperty(BackendListenerConstants.MS_DEBUG.name(), true);
        }
        testPlan.add(testPlan.getArray()[0], resultCollector);
    }

    private void runLocal(JmeterRunRequestDTO request) {
        init();
        // 接口用例集成报告/测试计划报告日志记录
        if (StringUtils.isNotEmpty(request.getTestPlanReportId())
                && StringUtils.equals(request.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            FixedCapacityUtil.put(request.getTestPlanReportId(), new StringBuffer(""));
        } else {
            // 报告日志记录
            FixedCapacityUtil.put(request.getReportId(), new StringBuffer(""));
        }
        LoggerUtil.debug("监听MessageCache.tasks当前容量：" + FixedCapacityUtil.size());
        if (request.isDebug() && !StringUtils.equalsAny(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加同步接收结果 Listener");
            JMeterBase.addBackendListener(request, request.getHashTree(), MsApiBackendListener.class.getCanonicalName());
        }

        if (MapUtils.isNotEmpty(request.getExtendedParameters())
                && request.getExtendedParameters().containsKey(ExtendedParameter.SYNC_STATUS)
                && (Boolean) request.getExtendedParameters().get(ExtendedParameter.SYNC_STATUS)) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加Debug Listener");
            addDebugListener(request.getReportId(), request.getHashTree());
        }

        if (request.isDebug()) {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加Debug Listener");
            addDebugListener(request.getReportId(), request.getHashTree());
        } else {
            LoggerUtil.debug("为请求 [ " + request.getReportId() + " ] 添加同步接收结果 Listener");
            JMeterBase.addBackendListener(request, request.getHashTree(), MsApiBackendListener.class.getCanonicalName());
        }

        LoggerUtil.info("资源：[" + request.getTestId() + "] 加入JMETER中开始执行", request.getReportId());
        LocalRunner runner = new LocalRunner(request.getHashTree());
        runner.run(request.getReportId());
    }

    private void runNode(JmeterRunRequestDTO request) {
        request.setKafkaConfig(KafkaConfig.getKafka());
        // 如果是K8S调用
        if (request.getPool().isK8s()) {
            try {
                LoggerUtil.info("开始发送请求[ " + request.getTestId() + " ] 到K8S节点执行", request.getReportId());
                final Engine engine = EngineFactory.createApiEngine(request);
                engine.start();
            } catch (Exception e) {
                RemakeReportService apiScenarioReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                apiScenarioReportService.testEnded(request, e.getMessage());
                LoggerUtil.error("调用K8S执行请求[ " + request.getTestId() + " ]失败：", request.getReportId(), e);
            }
        } else if ((MapUtils.isNotEmpty(request.getExtendedParameters())
                && request.getExtendedParameters().containsKey(ExtendedParameter.SYNC_STATUS)
                && (Boolean) request.getExtendedParameters().get(ExtendedParameter.SYNC_STATUS))
                || request.isDebug()) {
            this.nodeDebug(request);
        } else {
            this.send(request);
        }
    }

    private synchronized void nodeDebug(JmeterRunRequestDTO request) {
        try {
            if (request.isDebug() && !StringUtils.equalsAny(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
                request.getExtendedParameters().put(ExtendedParameter.SAVE_RESULT, true);
            } else if (!request.isDebug()) {
                request.getExtendedParameters().put(ExtendedParameter.SAVE_RESULT, true);
            }
            List<TestResource> resources = GenerateHashTreeUtil.setPoolResource(request.getPoolId());
            String uri = null;
            int index = (int) (Math.random() * resources.size());
            String configuration = resources.get(index).getConfiguration();
            if (StringUtils.isNotEmpty(configuration)) {
                NodeDTO node = com.alibaba.fastjson.JSON.parseObject(configuration, NodeDTO.class);
                uri = String.format(BASE_URL + "/jmeter/debug", node.getIp(), node.getPort());
            }
            if (StringUtils.isEmpty(uri)) {
                LoggerUtil.info("未获取到资源池，请检查配置【系统设置-系统-测试资源池】", request.getReportId());
                MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
            }
            request.getExtendedParameters().put(ExtendedParameter.JMX, new MsTestPlan().getJmx(request.getHashTree()));
            request.setHashTree(null);
            LoggerUtil.info("开始发送请求【 " + request.getTestId() + " 】到 " + uri + " 节点执行", request.getReportId());
            ResponseEntity<String> result = restTemplate.postForEntity(uri, request, String.class);
            if (result == null || !StringUtils.equals("SUCCESS", result.getBody())) {
                LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 到" + uri + " 节点执行失败", request.getReportId());
                LoggerUtil.info(result);
                MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
            }
        } catch (Exception e) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.remake(request);
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败,进行数据回滚：", request.getReportId(), e);
            MSException.throwException("调用资源池执行失败，请检查资源池是否配置正常");
        }
    }

    private synchronized void send(JmeterRunRequestDTO request) {
        try {
            if (redisTemplate.opsForValue().get(SmoothWeighted.EXEC_INDEX + request.getPoolId()) != null) {
                long index = Long.parseLong(redisTemplate.opsForValue().get(SmoothWeighted.EXEC_INDEX + request.getPoolId()).toString());
                redisTemplate.opsForValue().set(SmoothWeighted.EXEC_INDEX + request.getPoolId(), index + 1);
            }
            ServerConfig config = SmoothWeighted.calculate(request.getPoolId(), redisTemplate);
            if (config == null) {
                config = SmoothWeighted.getResource(request.getPoolId());
            }
            if (config == null) {
                LoggerUtil.info("未获取到资源池，请检查配置【系统设置-系统-测试资源池】", request.getReportId());
                RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                remakeReportService.remake(request);
                return;
            }
            request.setCorePoolSize(config.getCorePoolSize());
            request.setEnable(config.isEnable());
            LoggerUtil.info("开始发送请求【 " + request.getTestId() + " 】到 " + config.getUrl() + " 节点执行", request.getReportId());
            ResponseEntity<String> result = restTemplate.postForEntity(config.getUrl(), request, String.class);
            if (result == null || !StringUtils.equals("SUCCESS", result.getBody())) {
                RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
                remakeReportService.remake(request);
                LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 到" + config.getUrl() + " 节点执行失败", request.getReportId());
                LoggerUtil.info(result.getBody());
            }
        } catch (Exception e) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.remake(request);
            LoggerUtil.error("发送请求[ " + request.getTestId() + " ] 执行失败,进行数据回滚：", request.getReportId(), e);
        }
    }


    public void run(JmeterRunRequestDTO request) {
        if (request.getPool().isPool() && StringUtils.isNotBlank(request.getRunMode())) {
            this.runNode(request);
        } else if (request.getHashTree() != null) {
            //解析hashTree，是否含有文件库文件
            HashTreeUtil.initRepositoryFiles(request);
            CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).addTask(request);
        }
    }

    public void addQueue(JmeterRunRequestDTO request) {
        this.runLocal(request);
    }

    public boolean getRunningQueue(String poolId, String reportId) {
        try {
            List<TestResource> resources = GenerateHashTreeUtil.setPoolResource(poolId);
            if (CollectionUtils.isEmpty(resources)) {
                return false;
            }
            boolean isRunning = false;
            for (TestResource testResource : resources) {
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

    public void verifyPool(String projectId, RunModeConfigDTO runConfig) {
        // 检查是否禁用了本地执行
        if (runConfig != null && StringUtils.isEmpty(runConfig.getResourcePoolId())) {
            BaseSystemConfigDTO configDTO = systemParameterService.getBaseInfo();
            if (StringUtils.equals(configDTO.getRunMode(), POOL)) {
                ProjectConfig config = projectApplicationService.getProjectConfig(projectId);
                if (config == null || !config.getPoolEnable() || StringUtils.isEmpty(config.getResourcePoolId())) {
                    MSException.throwException("请在【项目设置-应用管理-接口测试】中选择资源池");
                }
                runConfig = runConfig == null ? new RunModeConfigDTO() : runConfig;
                runConfig.setResourcePoolId(config.getResourcePoolId());
            }
        }
    }
}
