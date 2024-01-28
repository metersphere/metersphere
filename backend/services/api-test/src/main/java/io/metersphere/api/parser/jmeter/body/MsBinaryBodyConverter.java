package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.BinaryBody;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;

/**
 * 处理 Binary 参数
 * @Author: jianxing
 * @CreateTime: 2023-12-14  19:55
 */
public class MsBinaryBodyConverter extends MsBodyConverter<BinaryBody> {
    @Override
    public void parse(HTTPSamplerProxy sampler, BinaryBody body, ParameterConfig config) {
        HTTPFileArg httpFileArg = getHttpFileArg(body);
        sampler.setHTTPFiles(new HTTPFileArg[]{httpFileArg});
    }
}
