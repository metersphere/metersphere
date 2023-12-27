package io.metersphere.api.parser.jmeter.processor.extract;

import io.metersphere.api.dto.request.processors.extract.JSONPathExtract;
import io.metersphere.api.dto.request.processors.extract.RegexExtract;
import io.metersphere.api.dto.request.processors.extract.XPathExtract;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:31
 */
public class ExtractConverterFactory {
    private static Map<Class, ExtractConverter> converterMap = new HashMap<>();

    static {
        converterMap.put(RegexExtract.class, new RegexExtractConverter());
        converterMap.put(JSONPathExtract.class, new JSONPathExtractConverter());
        converterMap.put(XPathExtract.class, new XPathExtractConverter());
    }

    public static ExtractConverter getConverter(Class processorClass) {
        return converterMap.get(processorClass);
    }
}
