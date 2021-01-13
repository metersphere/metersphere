package io.metersphere.api.jmeter;

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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

@Service
public class JMeterService {

    @Resource
    private JmeterProperties jmeterProperties;

    @PostConstruct
    public void init() {
        String JMETER_HOME = getJmeterHome();

        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());
    }

    public void run(String testId, String debugReportId, InputStream is) {
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

    public void runDefinition(String testId, HashTree testPlan, String debugReportId, String runMode) {
        try {
            init();
            addBackendListener(testId, debugReportId, runMode, testPlan);
            LocalRunner runner = new LocalRunner(testPlan);
            runner.run();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("api_load_script_error"));
        }
    }
}
