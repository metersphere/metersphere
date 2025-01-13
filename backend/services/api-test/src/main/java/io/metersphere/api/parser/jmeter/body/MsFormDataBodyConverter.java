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
 *
 * @Author: jianxing
 * @CreateTime: 2023-12-14  15:18
 */
public class MsFormDataBodyConverter extends MsBodyConverter<FormDataBody> {

    @Override
    public String parse(HTTPSamplerProxy sampler, FormDataBody body, ParameterConfig config) {
        List<FormDataKV> formValues = body.getFormValues();
        sampler.setDoMultipart(true);
        if (CollectionUtils.isEmpty(formValues)) {
            return null;
        }
        List<FormDataKV> validFormValues = formValues.stream()
                .filter(FormDataKV::getEnable)
                .filter(FormDataKV::isValid)
                .collect(Collectors.toList());
        List<FormDataKV> fileFormValues = validFormValues.stream().filter(FormDataKV::isFile).collect(Collectors.toList());
        List<FormDataKV> textFormValues = validFormValues.stream().filter(kv -> !kv.isFile()).collect(Collectors.toList());
        sampler.setHTTPFiles(getHttpFileArg(fileFormValues));
        sampler.setArguments(getArguments(textFormValues));
        return null;
    }


    /**
     * 解析文件类型的参数
     *
     * @param fileFormValues
     * @return
     */
    private HTTPFileArg[] getHttpFileArg(List<FormDataKV> fileFormValues) {
        if (CollectionUtils.isEmpty(fileFormValues)) {
            return new HTTPFileArg[0];
        }
        List<HTTPFileArg> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fileFormValues)) {
            fileFormValues.forEach(formDataKV -> {
                String paramName = formDataKV.getKey();
                if (formDataKV.getFiles() != null) {
                    formDataKV.getFiles().forEach(file -> {
                        HTTPFileArg fileArg = getHttpFileArg(file);
                        fileArg.setParamName(paramName);
                        String mimetype = formDataKV.getContentType();
                        if (StringUtils.isNotBlank(mimetype)) {
                            fileArg.setMimeType(mimetype);
                        }
                        list.add(fileArg);
                    });
                }
            });
        }
        return list.toArray(new HTTPFileArg[0]);
    }
}
