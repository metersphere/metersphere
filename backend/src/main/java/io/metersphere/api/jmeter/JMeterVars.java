package io.metersphere.api.jmeter;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JMeterVars {
    public static Map<String, JMeterVariables> variables = new HashMap<>();

    public static void addVars(String testId, JMeterVariables vars, String extract) {
        JMeterVariables vs = new JMeterVariables();
        if (!StringUtils.isEmpty(extract) && vars != null) {
            List<String> extracts = Arrays.asList(extract.split(";"));
            extracts.forEach(item -> {
                vs.put(item, vars.get(item));
            });
        }
        variables.put(testId, vs);
    }
}
