package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-21  19:19
 */
public class MsBodyConverterFactory {

    private static final Map<Class<?>, MsBodyConverter> converterMap = new HashMap<>();

    static {
        converterMap.put(RawBody.class, new MsRawBodyConverter());
        converterMap.put(NoneBody.class, new MsNoneBodyConverter());
        converterMap.put(FormDataBody.class, new MsFormDataBodyConverter());
        converterMap.put(WWWFormBody.class, new MsWWWFormBodyConverter());
        converterMap.put(JsonBody.class, new MsJsonBodyConverter());
        converterMap.put(XmlBody.class, new MsXmlBodyConverter());
        converterMap.put(BinaryBody.class, new MsBinaryBodyConverter());
    }

    public static MsBodyConverter getConverter(Class<?> bodyClassByType) {
        return converterMap.get(bodyClassByType);
    }
}
