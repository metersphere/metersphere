package io.metersphere.api.parser.jmeter.body;

import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.FormDataKV;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理 form-data 类型的请求体
 * @Author: jianxing
 * @CreateTime: 2023-12-14  15:18
 */
public class MsFormDataBodyConverter extends MsBodyConverter<FormDataBody> {

    @Override
    public void parse(HTTPSamplerProxy sampler, FormDataBody body, ParameterConfig config) {
        List<FormDataKV> fromValues = body.getFromValues();
        List<FormDataKV> validFromValues = fromValues.stream().filter(FormDataKV::isValid).collect(Collectors.toList());
        List<FormDataKV> fileFromValues = validFromValues.stream().filter(FormDataKV::isFile).collect(Collectors.toList());
        List<FormDataKV> textFromValues = validFromValues.stream().filter(kv -> !kv.isFile()).collect(Collectors.toList());
        sampler.setDoMultipart(true);
        sampler.setHTTPFiles(getHttpFileArg(fileFromValues));
        sampler.setArguments(getArguments(textFromValues));
    }


    /**
     * 解析文件类型的参数
     * @param fileFromValues
     * @return
     */
    private HTTPFileArg[] getHttpFileArg(List<FormDataKV> fileFromValues) {
        if (CollectionUtils.isEmpty(fileFromValues)) {
            return new HTTPFileArg[0];
        }
        List<HTTPFileArg> list = new ArrayList<>();
        if (fileFromValues != null) {
            fileFromValues.forEach(formDataKV -> {
                String paramName = formDataKV.getKey();
                formDataKV.getFiles().forEach(file -> {
                    HTTPFileArg fileArg = getHttpFileArg(file);
                    fileArg.setParamName(paramName);
                    String mimetype = formDataKV.getContentType();
                    if (StringUtils.isBlank(mimetype)) {
                        fileArg.setMimeType(mimetype);
                    }
                    list.add(fileArg);
                });
            });
        }
        return list.toArray(new HTTPFileArg[0]);
    }
}
