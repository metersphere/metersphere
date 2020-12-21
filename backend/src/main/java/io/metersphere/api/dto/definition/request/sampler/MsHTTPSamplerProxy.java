package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "HTTPSamplerProxy")
public class MsHTTPSamplerProxy extends MsTestElement {
    private String type = "HTTPSamplerProxy";

    @JSONField(ordinal = 20)
    private String protocol;

    @JSONField(ordinal = 21)
    private String domain;

    @JSONField(ordinal = 22)
    private String port;

    @JSONField(ordinal = 23)
    private String method;

    @JSONField(ordinal = 24)
    private String path;

    @JSONField(ordinal = 25)
    private String connectTimeout;

    @JSONField(ordinal = 26)
    private String responseTimeout;

    @JSONField(ordinal = 27)
    private List<KeyValue> headers;

    @JSONField(ordinal = 28)
    private Body body;

    @JSONField(ordinal = 29)
    private List<KeyValue> rest;

    @JSONField(ordinal = 30)
    private String url;

    @JSONField(ordinal = 31)
    private boolean followRedirects;

    @JSONField(ordinal = 32)
    private boolean doMultipartPost;

    @JSONField(ordinal = 33)
    private String useEnvironment;

    @JSONField(ordinal = 34)
    private List<KeyValue> arguments;

    @JSONField(ordinal = 35)
    private Object requestResult;

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && this.getReferenced().equals("REF")) {
            this.getRefElement(this);
        }
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(true);
        sampler.setName(this.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        sampler.setMethod(this.getMethod());
        sampler.setContentEncoding("UTF-8");
        sampler.setConnectTimeout(this.getConnectTimeout() == null ? "6000" : this.getConnectTimeout());
        sampler.setResponseTimeout(this.getResponseTimeout() == null ? "6000" : this.getResponseTimeout());
        sampler.setFollowRedirects(this.isFollowRedirects());
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(this.isDoMultipartPost());
        if (useEnvironment != null) {
            ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(useEnvironment);
            config.setConfig(JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class));
        }
        try {
            if (config != null && config.getConfig() != null) {
                String url = "";
                sampler.setDomain(config.getConfig().getHttpConfig().getDomain());
                sampler.setPort(config.getConfig().getHttpConfig().getPort());
                sampler.setProtocol(config.getConfig().getHttpConfig().getProtocol());
                url = config.getConfig().getHttpConfig().getProtocol() + "://" + config.getConfig().getHttpConfig().getSocket();
                URL urlObject = new URL(url);
                String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getPath();
                if (StringUtils.isNotBlank(this.getPath())) {
                    envPath += this.getPath();
                }
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(URLDecoder.decode(envPath, "UTF-8"));
                    sampler.setPath(envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    sampler.setPath(getPostQueryParameters(URLDecoder.decode(envPath, "UTF-8")));
                }
            } else {
                String url = this.getUrl();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                URL urlObject = new URL(url);
                sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                sampler.setPort(urlObject.getPort());
                sampler.setProtocol(urlObject.getProtocol());

                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    sampler.setPath(getRestParameters(URLDecoder.decode(urlObject.getPath(), "UTF-8")));
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    sampler.setPath(getPostQueryParameters(URLDecoder.decode(urlObject.getPath(), "UTF-8")));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        // REST参数
        if (CollectionUtils.isNotEmpty(this.getArguments())) {
            sampler.setArguments(httpArguments(this.getRest()));
        }
        // 请求参数
        if (CollectionUtils.isNotEmpty(this.getArguments())) {
            sampler.setArguments(httpArguments(this.getArguments()));
        }
        // 请求体
        if (!StringUtils.equals(this.getMethod(), "GET")) {
            List<KeyValue> bodyParams = this.body.getBodyParams(sampler, this.getId());
            if (this.body.getType().equals("Form Data")) {
                sampler.setDoMultipart(true);
            }
            sampler.setArguments(httpArguments(bodyParams));
        }

        final HashTree httpSamplerTree = tree.add(sampler);
        if (CollectionUtils.isNotEmpty(this.headers)) {
            setHeader(httpSamplerTree);
        }
        //判断是否要开启DNS
        if (config != null && config.getConfig() != null && config.getConfig().getCommonConfig() != null
                && config.getConfig().getCommonConfig().isEnableHost()) {
            MsDNSCacheManager.addEnvironmentVariables(httpSamplerTree, this.getName(), config.getConfig());
            MsDNSCacheManager.addEnvironmentDNS(httpSamplerTree, this.getName(), config.getConfig());
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                el.toHashTree(httpSamplerTree, el.getHashTree(), config);
            }
        }
    }

    private String getRestParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("/");
        Map<String, String> keyValueMap = new HashMap<>();
        this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue ->
                keyValueMap.put(keyValue.getName(), keyValue.getValue())
        );

        Pattern p = Pattern.compile("(\\{)([\\w]+)(\\})");
        Matcher m = p.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String group = m.group(2);
            //替换并且把替换好的值放到sb中
            m.appendReplacement(sb, keyValueMap.get(group));
        }
        //把符合的数据追加到sb尾
        m.appendTail(sb);
        return sb.toString();
    }

    private String getPostQueryParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("?");
        this.getArguments().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue ->
                stringBuffer.append(keyValue.getName()).append("=").append(keyValue.getValue()).append("&")
        );
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private Arguments httpArguments(List<KeyValue> list) {
        Arguments arguments = new Arguments();
        list.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue -> {
                    HTTPArgument httpArgument = new HTTPArgument(keyValue.getName(), keyValue.getValue());
                    httpArgument.setAlwaysEncoded(keyValue.isEncode());
                    if (StringUtils.isNotBlank(keyValue.getContentType())) {
                        httpArgument.setContentType(keyValue.getContentType());
                    }
                    arguments.addArgument(httpArgument);
                }
        );
        return arguments;
    }

    public void setHeader(HashTree tree) {
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(this.getName() + "Headers");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                headerManager.add(new Header(keyValue.getName(), keyValue.getValue()))
        );
        tree.add(headerManager);
    }


    private boolean isRest() {
        return this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).toArray().length > 0;
    }

}
