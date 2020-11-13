package io.metersphere.api.jmeter;

import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.config.JmeterProperties;
import io.metersphere.i18n.Translator;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jorphan.collections.HashTree;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

@Service
public class DelimitJMeterService {

    @Resource
    private JmeterProperties jmeterProperties;

    public void run(String testId, String reportId, InputStream is) {
        String home = getJmeterHome();
        String jmeterProperties = home + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(jmeterProperties);
        JMeterUtils.setJMeterHome(home);
        JMeterUtils.setLocale(LocaleContextHolder.getLocale());

        try {
            Object scriptWrapper = SaveService.loadElement(is);
            HashTree testPlan = getHashTree(scriptWrapper);
            JMeterVars.addJSR223PostProcessor(testPlan);
            addBackendListener(testId, reportId, testPlan);
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

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    private void addBackendListener(String testId, String debugReportId, HashTree testPlan) {
        BackendListener backendListener = new BackendListener();
        backendListener.setName(testId);
        Arguments arguments = new Arguments();
        arguments.addArgument(DelimitBackendListenerClient.TEST_ID, testId);

        arguments.addArgument("runMode", ApiRunMode.DEBUG.name());
        arguments.addArgument("reportId", debugReportId);

        backendListener.setArguments(arguments);
        backendListener.setClassname(DelimitBackendListenerClient.class.getCanonicalName());
        testPlan.add(testPlan.getArray()[0], backendListener);
    }
}
