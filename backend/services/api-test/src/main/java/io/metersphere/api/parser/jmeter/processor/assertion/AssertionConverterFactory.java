package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.project.api.assertion.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:31
 */
public class AssertionConverterFactory {
    private static final Map<Class<?>, AssertionConverter> converterMap = new HashMap<>();

    static {
        converterMap.put(MsResponseCodeAssertion.class, new ResponseCodeAssertionConverter());
        converterMap.put(MsResponseHeaderAssertion.class, new ResponseHeaderAssertionConverter());
        converterMap.put(MsResponseBodyAssertion.class, new ResponseBodyAssertionConverter());
        converterMap.put(MsResponseTimeAssertion.class, new ResponseTimeAssertionConverter());
        converterMap.put(MsScriptAssertion.class, new ScriptAssertionConverter());
        converterMap.put(MsVariableAssertion.class, new VariableAssertionConverter());
    }

    public static AssertionConverter getConverter(Class<?> processorClass) {
        return converterMap.get(processorClass);
    }
}
