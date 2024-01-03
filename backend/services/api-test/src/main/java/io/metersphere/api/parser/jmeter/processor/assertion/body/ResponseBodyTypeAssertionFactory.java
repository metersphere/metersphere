package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.api.dto.request.assertion.body.MsDocumentAssertion;
import io.metersphere.api.dto.request.assertion.body.MsJSONPathAssertion;
import io.metersphere.api.dto.request.assertion.body.MsRegexAssertion;
import io.metersphere.api.dto.request.assertion.body.MsXPathAssertion;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-03  09:42
 */
public class ResponseBodyTypeAssertionFactory {

    private static Map<Class, ResponseBodyTypeAssertionConverter> converterMap = new HashMap<>();

    static {
        converterMap.put(MsJSONPathAssertion.class, new JSONPathAssertionConverter());
        converterMap.put(MsXPathAssertion.class, new XPathAssertionConverter());
        converterMap.put(MsDocumentAssertion.class, new DocumentAssertionConverter());
        converterMap.put(MsRegexAssertion.class, new RegexAssertionConverter());
    }

    public static ResponseBodyTypeAssertionConverter getConverter(Class processorClass) {
        return converterMap.get(processorClass);
    }
}
