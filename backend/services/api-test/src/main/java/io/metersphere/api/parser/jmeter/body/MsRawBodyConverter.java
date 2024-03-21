package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;

/**
 * 处理 Raw 格式参数
 * @Author: jianxing
 * @CreateTime: 2023-12-14  21:15
 */
public class MsRawBodyConverter extends MsBodyConverter<RawBody> {
    @Override
    public String parse(HTTPSamplerProxy sampler, RawBody body, ParameterConfig config) {
        handleRowBody(sampler, body.getValue());
        return null;
    }
}
