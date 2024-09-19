package io.metersphere.api.parser.ms;

import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.collections.HashTree;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  11:36
 */
public class HTTPSamplerConverter extends AbstractMsElementConverter<HTTPSamplerProxy> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, HTTPSamplerProxy httpSampler, HashTree hashTree) {
        MsHTTPElement msHTTPElement = this.convertHttpSampler(httpSampler);
        parent.getChildren().add(msHTTPElement);

        //默认生成一个空的body
        MsCommonElement msCommonElement = new MsCommonElement();
        LinkedList<AbstractMsTestElement> children = new LinkedList<>();
        children.add(msCommonElement);
        msHTTPElement.setChildren(children);

        parseChild(msHTTPElement, httpSampler, hashTree);
    }

    private MsHTTPElement convertHttpSampler(HTTPSamplerProxy source) {
        MsHTTPElement samplerProxy = new MsHTTPElement();
        samplerProxy.setQuery(new ArrayList<>());
        samplerProxy.setRest(new ArrayList<>());
        try {

            BeanUtils.copyBean(samplerProxy, source);
            // 处理HTTP协议的请求头
            List<MsHeader> headerKvList = new LinkedList<>();
            HeaderManager headerManager = source.getHeaderManager();
            if (headerManager != null && headerManager.getHeaders() != null) {
                for (int i = 0; i < headerManager.getHeaders().size(); i++) {
                    String headerKey = headerManager.getHeader(i).getName();
                    String value = headerManager.getHeader(i).getValue();
                    headerKvList.add(new MsHeader() {{
                        this.setKey(headerKey);
                        this.setValue(value);
                    }});
                }
            }
            samplerProxy.setHeaders(headerKvList);
            // 初始化body
            Body body = new Body();
            body.setBodyType(Body.BodyType.NONE.name());
            body.setJsonBody(new JsonBody());
            body.setFormDataBody(new FormDataBody());
            body.setWwwFormBody(new WWWFormBody());
            body.setRawBody(new RawBody());
            body.setXmlBody(new XmlBody());
            samplerProxy.setBody(body);
            if (source.getHTTPFiles().length > 0) {
                samplerProxy.getBody().setBodyType(Body.BodyType.FORM_DATA.name());
                List<FormDataKV> keyValues = new LinkedList<>();
                for (HTTPFileArg arg : source.getHTTPFiles()) {
                    FormDataKV keyValue = getFormDataKV(arg);
                    keyValues.add(keyValue);
                }
                samplerProxy.getBody().setFormDataBody(new FormDataBody() {{
                    this.setFormValues(keyValues);
                }});
            }
            samplerProxy.getOtherConfig().setConnectTimeout((long) source.getConnectTimeout());
            samplerProxy.getOtherConfig().setResponseTimeout((long) source.getResponseTimeout());
            samplerProxy.getOtherConfig().setFollowRedirects(source.getFollowRedirects());
            samplerProxy.getOtherConfig().setAutoRedirects(source.getAutoRedirects());

            if (source.getArguments() != null) {
                String bodyType = this.getBodyType(samplerProxy.getHeaders());
                if (source.getPostBodyRaw()) {
                    List<MsHeader> headers = samplerProxy.getHeaders();
                    boolean jsonType = false;
                    if (CollectionUtils.isNotEmpty(headers)) {
                        for (MsHeader header : headers) {
                            if (StringUtils.equals(header.getKey(), "Content-Type") && StringUtils.equals(header.getValue(), "application/json")) {
                                samplerProxy.getBody().setBodyType(Body.BodyType.JSON.name());
                                jsonType = true;
                                break;
                            }
                        }
                    }
                    if (!jsonType) {
                        samplerProxy.getBody().setBodyType(Body.BodyType.RAW.name());
                    }
                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> samplerProxy.getBody().setRawBody(new RawBody() {{
                        this.setValue(v);
                    }}));
                } else if (StringUtils.isNotEmpty(bodyType) || (StringUtils.equalsAnyIgnoreCase(source.getMethod(), "POST", "PUT") && !source.getArguments().getArgumentsAsMap().isEmpty())) {
                    samplerProxy.getBody().setBodyType(Body.BodyType.WWW_FORM.name());
                    List<WWWFormKV> keyValues = new LinkedList<>();
                    source.getArguments().getArguments().forEach(params -> {
                        WWWFormKV keyValue = new WWWFormKV();
                        parseParams(params, keyValue);
                        keyValues.add(keyValue);
                    });
                    samplerProxy.getBody().setWwwFormBody(new WWWFormBody() {{
                        this.setFormValues(keyValues);
                    }});
                } else if (samplerProxy.getBody() != null && samplerProxy.getBody().getBodyType().equals(Body.BodyType.FORM_DATA.name())) {
                    source.getArguments().getArguments().forEach(params -> {
                        FormDataKV keyValue = new FormDataKV();
                        parseParams(params, keyValue);
                        samplerProxy.getBody().getFormDataBody().getFormValues().add(keyValue);
                    });
                } else {
                    List<QueryParam> keyValues = new LinkedList<>();
                    source.getArguments().getArguments().forEach(params -> {
                        QueryParam keyValue = new QueryParam();
                        parseParams(params, keyValue);
                        keyValues.add(keyValue);
                    });
                    if (CollectionUtils.isNotEmpty(keyValues)) {
                        samplerProxy.setQuery(keyValues);
                    }
                }
            }
            samplerProxy.setPath(source.getPath());
            samplerProxy.setMethod(source.getMethod());
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return samplerProxy;
    }

    private String getBodyType(List<MsHeader> headers) {
        if (CollectionUtils.isNotEmpty(headers)) {
            List<MsHeader> keyValues = headers.stream().filter(keyValue -> "Content-Type".equals(keyValue.getKey()) && "application/x-www-form-urlencoded".equals(keyValue.getValue())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(keyValues)) {
                return keyValues.getFirst().getValue();
            }
        }
        return null;
    }

    @NotNull
    private static FormDataKV getFormDataKV(HTTPFileArg arg) {
        ApiFile file = new ApiFile();
        file.setFileId(arg.getParamName());
        String fileName = arg.getPath();
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        file.setFileName(fileName);

        FormDataKV keyValue = new FormDataKV();
        keyValue.setKey(arg.getParamName());
        keyValue.setValue(arg.getParamName());
        keyValue.setContentType(arg.getMimeType());
        keyValue.setContentType("file");
        keyValue.setFiles(Collections.singletonList(file));
        return keyValue;
    }

    private void parseParams(JMeterProperty params, KeyValueEnableParam keyValue) {
        if (params == null || keyValue == null) {
            return;
        }
        Object objValue = params.getObjectValue();
        if (objValue instanceof HTTPArgument) {
            HTTPArgument argument = (HTTPArgument) objValue;
            keyValue.setKey(argument.getName());
            keyValue.setValue(argument.getValue());
        }
    }
}
