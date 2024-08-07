package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.request.http.body.BinaryBody;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.springframework.http.MediaType;

/**
 * 处理 Binary 参数
 * @Author: jianxing
 * @CreateTime: 2023-12-14  19:55
 */
public class MsBinaryBodyConverter extends MsBodyConverter<BinaryBody> {
    @Override
    public String parse(HTTPSamplerProxy sampler, BinaryBody body, ParameterConfig config) {
        ApiFile file = body.getFile();
        if (file == null) {
            return null;
        }
        HTTPFileArg httpFileArg = getHttpFileArg(file);
        sampler.setHTTPFiles(new HTTPFileArg[]{httpFileArg});
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
