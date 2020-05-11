package io.metersphere.api.jmeter;

import io.metersphere.commons.exception.MSException;
import io.metersphere.config.JmeterProperties;
import io.metersphere.i18n.Translator;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Field;

import javax.annotation.Resource;

@Service
public class JMeterService {

    @Resource
    private JmeterProperties jmeterProperties;

    public void run(InputStream is) {
        String JMETER_HOME = jmeterProperties.getHome();
        String JMETER_PROPERTIES = JMETER_HOME + "/bin/jmeter.properties";
        JMeterUtils.loadJMeterProperties(JMETER_PROPERTIES);
        JMeterUtils.setJMeterHome(JMETER_HOME);
        try {
            Object scriptWrapper = SaveService.loadElement(is);
            HashTree testPlan = getHashTree(scriptWrapper);

            LocalRunner runner = new LocalRunner(testPlan);
            runner.run();
        } catch (Exception e) {
            MSException.throwException(Translator.get("api_load_script_error"));
        }
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }
}
