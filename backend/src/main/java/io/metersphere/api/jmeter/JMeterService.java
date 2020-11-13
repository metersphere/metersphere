package io.metersphere.api.jmeter;

import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.config.JmeterProperties;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class JMeterService {

    @Resource
    private JmeterProperties jmeterProperties;
    @Resource
    private JMXGenerator jmxGenerator;

    @PostConstruct
    public void init() {
        String JMETER_HOME = getJmeterHome();

        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public HashTree getHashTree(String testId, String testName, List<Scenario> scenarios) {
        return jmxGenerator.parse(testId, testName, scenarios);
    }

    public void run(String testId, String testName, List<Scenario> scenarios, String debugReportId) {
        try {
            init();
            HashTree testPlan = getHashTree(testId, testName, scenarios);
            JMeterVars.addJSR223PostProcessor(testPlan);
            addBackendListener(testId, debugReportId, testPlan);
            LocalRunner runner = new LocalRunner(testPlan);
            runner.run();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("api_load_script_error"));
        }
    }

    public String getJMX(HashTree hashTree) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, baos);
            LogUtil.debug(baos.toString());
            return baos.toString();
        } catch (Exception e) {
            LogUtil.warn("HashTree error, can't log jmx content");
        }
        return null;
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

    private void addBackendListener(String testId, String debugReportId, HashTree testPlan) {
        BackendListener backendListener = new BackendListener();
        backendListener.setName(testId);
        Arguments arguments = new Arguments();
        arguments.addArgument(APIBackendListenerClient.TEST_ID, testId);
        if (StringUtils.isNotBlank(debugReportId)) {
            arguments.addArgument("runMode", ApiRunMode.DEBUG.name());
            arguments.addArgument("debugReportId", debugReportId);
        }
        backendListener.setArguments(arguments);
        backendListener.setClassname(APIBackendListenerClient.class.getCanonicalName());
        testPlan.add(testPlan.getArray()[0], backendListener);
    }
}
