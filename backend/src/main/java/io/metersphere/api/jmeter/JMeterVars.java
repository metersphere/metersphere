package io.metersphere.api.jmeter;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.HashMap;
import java.util.Map;

public class JMeterVars {
    public static Map<String, JMeterVariables> variables = new HashMap<>();

    public static void addVars(String testId, JMeterVariables vars) {
        variables.put(testId, vars);
    }
}
