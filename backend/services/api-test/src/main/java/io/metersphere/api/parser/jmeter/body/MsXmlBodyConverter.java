package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.XmlBody;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.springframework.http.MediaType;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-14  21:15
 */
public class MsXmlBodyConverter extends MsBodyConverter<XmlBody> {
    @Override
    public String parse(HTTPSamplerProxy sampler, XmlBody body, ParameterConfig config) {
        handleRowBody(sampler, body.getValue());
        return MediaType.TEXT_XML_VALUE;
    }
}
