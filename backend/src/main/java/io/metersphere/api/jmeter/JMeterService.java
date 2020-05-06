package io.metersphere.api.jmeter;

import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Field;

@Service
public class JMeterService {

    public void run(InputStream is) {
        JMeterUtils.loadJMeterProperties("/Users/q4speed/Downloads/apache-jmeter-5.2.1/bin/jmeter.properties");
        JMeterUtils.setJMeterHome("/Users/q4speed/Downloads/apache-jmeter-5.2.1");
        try {
            Object scriptWrapper = SaveService.loadElement(is);
            HashTree testPlan = getHashTree(scriptWrapper);

            LocalRunner runner = new LocalRunner(testPlan);
            runner.run();
        } catch (Exception e) {
            MSException.throwException(Translator.get("api_load_script_error"));
        }
    }

    public HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }
}
