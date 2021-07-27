package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.RunRequest;
import io.metersphere.api.dto.automation.ExecuteType;
import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.config.JmeterProperties;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.performance.engine.Engine;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

@Service
public class JMeterService {
    private static final String BASE_URL = "http://%s:%d";
    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    ResourcePoolCalculation resourcePoolCalculation;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;

    @PostConstruct
    public void init() {
        String JMETER_HOME = getJmeterHome();

        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public void runOld(String testId, String debugReportId, InputStream is) {
        init();
        try {
            Object scriptWrapper = SaveService.loadElement(is);
            HashTree testPlan = getHashTree(scriptWrapper);
            JMeterVars.addJSR223PostProcessor(testPlan);
            String runMode = StringUtils.isBlank(debugReportId) ? ApiRunMode.RUN.name() : ApiRunMode.DEBUG.name();
            addBackendListener(testId, debugReportId, runMode, testPlan);
            LocalRunner runner = new LocalRunner(testPlan);
            runner.run();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("api_load_script_error"));
        }
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

    public static HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    private void addBackendListener(String testId, String debugReportId, String runMode, HashTree testPlan) {
        BackendListener backendListener = new BackendListener();
        backendListener.setName(testId);
        Arguments arguments = new Arguments();
        arguments.addArgument(APIBackendListenerClient.TEST_ID, testId);
        if (StringUtils.isNotBlank(runMode)) {
            arguments.addArgument("runMode", runMode);
        }
        if (StringUtils.isNotBlank(debugReportId)) {
            arguments.addArgument("debugReportId", debugReportId);
        }
        backendListener.setArguments(arguments);
        backendListener.setClassname(APIBackendListenerClient.class.getCanonicalName());
        testPlan.add(testPlan.getArray()[0], backendListener);
    }

    private void addResultCollector(String testId, HashTree testPlan) {
        MsResultCollector resultCollector = new MsResultCollector();
        resultCollector.setName(testId);
        resultCollector.setProperty(TestElement.TEST_CLASS, MsResultCollector.class.getName());
        resultCollector.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ViewResultsFullVisualizer"));
        resultCollector.setEnabled(true);
        testPlan.add(testPlan.getArray()[0], resultCollector);
    }


    public void runLocal(String testId, HashTree testPlan, String debugReportId, String runMode) {
        init();
        FixedTask.tasks.put(testId, System.currentTimeMillis());
        addBackendListener(testId, debugReportId, runMode, testPlan);
        if (ExecuteType.Debug.name().equals(debugReportId) || ApiRunMode.SCENARIO.name().equals(runMode)) {
            addResultCollector(testId, testPlan);
        }
        LocalRunner runner = new LocalRunner(testPlan);
        runner.run();
    }

    public void runTest(String testId, String reportId, String runMode, String testPlanScenarioId, RunModeConfig config) {
        // 获取可以执行的资源池
        String resourcePoolId = config.getResourcePoolId();
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        RunRequest runRequest = new RunRequest();
        runRequest.setTestId(testId);
        runRequest.setReportId(reportId);
        runRequest.setPoolId(resourcePoolId);
        // 占位符
        String platformUrl = "http://localhost:8081";
        if (baseInfo != null) {
            platformUrl = baseInfo.getUrl();
        }
        platformUrl += "/api/jmeter/download?testId=" + testId + "&reportId=" + reportId + "&testPlanScenarioId" + "&runMode=" + runMode;
        if (StringUtils.isNotEmpty(testPlanScenarioId)) {
            platformUrl += "=" + testPlanScenarioId;
        }
        runRequest.setUrl(platformUrl);
        runRequest.setRunMode(runMode);
        // 如果是K8S调用
        TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(resourcePoolId);
        if (pool != null && pool.getApi() && pool.getType().equals(ResourcePoolTypeEnum.K8S.name())) {
            try {
                final Engine engine = EngineFactory.createApiEngine(runRequest);
                engine.start();
            } catch (Exception e) {
                ApiScenarioReportService apiScenarioReportService = CommonBeanFactory.getBean(ApiScenarioReportService.class);
                apiScenarioReportService.delete(reportId);
                MSException.throwException(e.getMessage());
            }
        } else {
            TestResource testResource = resourcePoolCalculation.getPool(resourcePoolId);
            String configuration = testResource.getConfiguration();
            NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
            String nodeIp = node.getIp();
            Integer port = node.getPort();
            try {
                String uri = String.format(BASE_URL + "/jmeter/api/start", nodeIp, port);
                ResponseEntity<String> result = restTemplate.postForEntity(uri, runRequest, String.class);
                if (result == null || !StringUtils.equals("SUCCESS", result.getBody())) {
                    // 清理零时报告
                    ApiScenarioReportService apiScenarioReportService = CommonBeanFactory.getBean(ApiScenarioReportService.class);
                    apiScenarioReportService.delete(reportId);
                    MSException.throwException("执行失败：" + result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MSException.throwException(e.getMessage());
            }
        }
    }
}
