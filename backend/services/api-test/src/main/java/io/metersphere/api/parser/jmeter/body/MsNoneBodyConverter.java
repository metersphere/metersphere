package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.NoneBody;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-14  21:15
 */
public class MsNoneBodyConverter extends MsBodyConverter<NoneBody> {
    @Override
    public String parse(HTTPSamplerProxy sampler, NoneBody body, ParameterConfig config) {
        // do nothing
        return null;
    }
}
