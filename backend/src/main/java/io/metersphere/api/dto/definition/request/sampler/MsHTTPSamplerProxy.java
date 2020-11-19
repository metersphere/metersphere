package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.definition.request.prop.BoolProp;
import io.metersphere.api.dto.definition.request.prop.StringProp;
import io.metersphere.api.dto.scenario.AuthConfig;
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
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "HTTPSamplerProxy")
public class MsHTTPSamplerProxy extends MsTestElement {
    private String type = "HTTPSamplerProxy";

    @JSONField(ordinal = 10)
    private StringProp protocol;

    @JSONField(ordinal = 11)
    private StringProp domain;

    @JSONField(ordinal = 12)
    private StringProp port;

    @JSONField(ordinal = 13)
    private StringProp method;

    @JSONField(ordinal = 14)
    private StringProp path;

    @JSONField(ordinal = 15)
    private StringProp connectTimeout;
    @JSONField(ordinal = 16)

    private StringProp responseTimeout;
    @JSONField(ordinal = 17)

    private List<KeyValue> arguments;

    @JSONField(ordinal = 18)
    private Body body;

    @JSONField(ordinal = 19)
    private List<KeyValue> rest;

    @JSONField(ordinal = 20)
    private AuthConfig authConfig;

    @JSONField(ordinal = 21)
    private BoolProp followRedirects;

    @JSONField(ordinal = 22)
    private BoolProp doMultipartPost;

    @JSONField(ordinal = 23)
    private String useEnvironment;

    @JSONField(ordinal = 24)
    private String url;


    public void toHashTree(HashTree tree, List<MsTestElement> hashTree) {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(true);
        sampler.setName(this.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        sampler.setMethod(this.getMethod().getValue());
        sampler.setContentEncoding("UTF-8");
        sampler.setConnectTimeout(this.getConnectTimeout().getValue() == null ? "6000" : this.getConnectTimeout().getValue());
        sampler.setResponseTimeout(this.getResponseTimeout().getValue() == null ? "6000" : this.getResponseTimeout().getValue());
        sampler.setFollowRedirects(this.getFollowRedirects() != null ? this.getFollowRedirects().isValue() : true);
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(this.getDoMultipartPost() != null ? this.getDoMultipartPost().isValue() : true);
        EnvironmentConfig config = null;
        if (useEnvironment != null) {
            ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(useEnvironment);
            config = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
        }
        try {
            if (config != null) {
                String url = "";
                sampler.setDomain(config.getHttpConfig().getDomain());
                sampler.setPort(config.getHttpConfig().getPort());
                sampler.setProtocol(config.getHttpConfig().getProtocol());
                url = config.getHttpConfig().getProtocol() + "://" + config.getHttpConfig().getSocket();
                URL urlObject = new URL(url);
                String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getPath();
                if (StringUtils.isNotBlank(this.getPath().getValue())) {
                    envPath += this.getPath().getValue();
                }
                sampler.setPath(getPostQueryParameters(URLDecoder.decode(envPath, "UTF-8")));
            } else {
                String url = this.getUrl();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                URL urlObject = new URL(url);
                sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                sampler.setPort(urlObject.getPort());
                sampler.setProtocol(urlObject.getProtocol());
                sampler.setPath(getPostQueryParameters(URLDecoder.decode(urlObject.getPath(), "UTF-8")));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }

        // 请求参数
        if (CollectionUtils.isNotEmpty(this.getArguments())) {
            sampler.setArguments(httpArguments(this.getArguments()));
        }
        // rest参数处理
        if (CollectionUtils.isNotEmpty(this.getRest())) {
            sampler.setArguments(httpArguments(this.getRest()));
        }

        // 请求体
        if (!StringUtils.equals(this.getMethod().getValue(), "GET")) {
            List<KeyValue> body = new ArrayList<>();
            if (this.getBody().isKV()) {
                body = this.getBody().getKvs().stream().filter(KeyValue::isValid).collect(Collectors.toList());
                sampler.setHTTPFiles(httpFileArgs());
            } else if (this.getBody().isBinary()) {
                // 上传二进制数据处理
            } else if (this.getBody().isJson()) {

            } else {
                if (StringUtils.isNotBlank(this.getBody().getRaw())) {
                    sampler.setPostBodyRaw(true);
                    KeyValue keyValue = new KeyValue("", this.getBody().getRaw());
                    keyValue.setEnable(true);
                    keyValue.setEncode(false);
                    body.add(keyValue);
                }
                if (StringUtils.isNotBlank(this.getBody().getXml())) {
                    sampler.setPostBodyRaw(true);
                    KeyValue keyValue = new KeyValue("", this.getBody().getXml());
                    keyValue.setEnable(true);
                    keyValue.setEncode(false);
                    body.add(keyValue);
                }
            }
            sampler.setArguments(httpArguments(body));
        }

        final HashTree httpSamplerTree = tree.add(sampler);

        //判断是否要开启DNS
        if (config != null && config.getCommonConfig() != null && config.getCommonConfig().isEnableHost()) {
            MsDNSCacheManager.addEnvironmentVariables(httpSamplerTree, this.getName(), config);
            MsDNSCacheManager.addEnvironmentDNS(httpSamplerTree, this.getName(), config);
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(httpSamplerTree, el.getHashTree());
            });
        }
    }

    private String getPostQueryParameters(String path) {
        if (!StringUtils.equals(this.getMethod().getValue(), "GET")) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(path);
            stringBuffer.append("?");
            this.getArguments().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue ->
                    stringBuffer.append(keyValue.getName()).append("=").append(keyValue.getValue()).append("&")
            );
            return stringBuffer.substring(0, stringBuffer.length() - 1);
        }
        return path;
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

    private HTTPFileArg[] httpFileArgs() {
        final String BODY_FILE_DIR = "/opt/metersphere/data/body";
        List<HTTPFileArg> list = new ArrayList<>();
        this.getBody().getKvs().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
            if (keyValue.getFiles() != null) {
                keyValue.getFiles().forEach(file -> {
                    String paramName = keyValue.getName();
                    String path = BODY_FILE_DIR + '/' + this.getId() + '/' + file.getId() + '_' + file.getName();
                    String mimetype = keyValue.getContentType();
                    list.add(new HTTPFileArg(path, paramName, mimetype));
                });
            }
        });
        return list.toArray(new HTTPFileArg[0]);
    }


}
