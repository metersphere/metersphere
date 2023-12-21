package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.api.dto.request.http.body.WWWFormBody;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-14  20:34
 */
public class MsWWWFormBodyConverter extends MsBodyConverter<WWWFormBody> {
    @Override
    public void parse(HTTPSamplerProxy sampler, WWWFormBody body, ParameterConfig config) {
        List<FormDataKV> fromValues = body.getFromValues();
        List<FormDataKV> validFromValues = fromValues.stream().filter(FormDataKV::isValid).collect(Collectors.toList());
        sampler.setArguments(getArguments(validFromValues));
    }
}
