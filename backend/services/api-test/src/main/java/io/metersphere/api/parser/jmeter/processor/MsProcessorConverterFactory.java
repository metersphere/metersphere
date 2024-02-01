package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.project.api.processor.ExtractPostProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.api.processor.TimeWaitingProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-21  19:19
 */
public class MsProcessorConverterFactory {

    private static final Map<Class<?>, MsProcessorConverter> preConverterMap = new HashMap<>();
    private static final Map<Class<?>, MsProcessorConverter> postConverterMap = new HashMap<>();

    static {
        preConverterMap.put(ScriptProcessor.class, new ScriptPreProcessorConverter());
        preConverterMap.put(SQLProcessor.class, new SqlPreProcessorConverter());
        preConverterMap.put(TimeWaitingProcessor.class, new TimeWaitingProcessorConverter());

        postConverterMap.put(ScriptProcessor.class, new ScriptPostProcessorConverter());
        postConverterMap.put(SQLProcessor.class, new SqlPostProcessorConverter());
        postConverterMap.put(TimeWaitingProcessor.class, new TimeWaitingProcessorConverter());
        postConverterMap.put(ExtractPostProcessor.class, new ExtractPostProcessorConverter());
    }

    public static MsProcessorConverter getPreConverter(Class<?> processorClass) {
        return preConverterMap.get(processorClass);
    }

    public static MsProcessorConverter getPostConverter(Class<?> processorClass) {
        return postConverterMap.get(processorClass);
    }
}
