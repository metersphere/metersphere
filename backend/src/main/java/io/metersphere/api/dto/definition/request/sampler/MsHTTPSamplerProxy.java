package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ScriptEngineUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
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

//    @JSONField(ordinal = 35)
//    private Object requestResult;

    @JSONField(ordinal = 36)
    private MsAuthManager authManager;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {

        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            this.getRefElement(this);
        }
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        String name = this.getParentName(this.getParent(), config);
        if (StringUtils.isNotEmpty(name) && !config.isOperating()) {
            sampler.setName(this.getName() + "<->" + name);
        }
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        sampler.setMethod(this.getMethod());
        sampler.setContentEncoding("UTF-8");
        sampler.setConnectTimeout(this.getConnectTimeout() == null ? "6000" : this.getConnectTimeout());
        sampler.setResponseTimeout(this.getResponseTimeout() == null ? "6000" : this.getResponseTimeout());
        sampler.setFollowRedirects(this.isFollowRedirects());
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(this.isDoMultipartPost());
        if (config != null && config.getConfig() != null) {
            config.setConfig(config.getConfig());
        } else {
//            config.setConfig(getEnvironmentConfig(useEnvironment));
        }

        // 添加环境中的公共变量
        Arguments arguments = this.addArguments(config);
        if (arguments != null) {
            tree.add(arguments);
        }
        try {
            if (config != null && config.getConfig() != null) {
                String url = config.getConfig().get(this.getProjectId()).getHttpConfig().getProtocol() + "://" + config.getConfig().get(this.getProjectId()).getHttpConfig().getSocket();
                // 补充如果是完整URL 则用自身URL
                boolean isUrl = false;
                if (StringUtils.isNotEmpty(this.getUrl()) && isURL(this.getUrl())) {
                    url = this.getUrl();
                    isUrl = true;
                }
                URL urlObject = new URL(url);
                if (isUrl) {
                    sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                    sampler.setPort(urlObject.getPort());
                    sampler.setProtocol(urlObject.getProtocol());
                } else {
                    sampler.setDomain(config.getConfig().get(this.getProjectId()).getHttpConfig().getDomain());
                    sampler.setPort(config.getConfig().get(this.getProjectId()).getHttpConfig().getPort());
                    sampler.setProtocol(config.getConfig().get(this.getProjectId()).getHttpConfig().getProtocol());
                }
                String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getPath();
                if (StringUtils.isNotBlank(this.getPath()) && !isUrl) {
                    envPath += this.getPath();
                    sampler.setPath(envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(URLDecoder.decode(envPath, "UTF-8"));
                    sampler.setPath(envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    String path = getPostQueryParameters(URLDecoder.decode(envPath, "UTF-8"));
                    if (HTTPConstants.DELETE.equals(this.getMethod())) {
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                        String port = sampler.getPort() != 80 ? ":" + sampler.getPort() : "";
                        path = sampler.getProtocol() + "://" + sampler.getDomain() + port + path;
                    }
                    sampler.setPath(path);
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
                String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getPath();
                sampler.setPath(envPath);
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(URLDecoder.decode(envPath, "UTF-8"));
                    sampler.setPath(envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    sampler.setPath(getPostQueryParameters(URLDecoder.decode(envPath, "UTF-8")));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        // REST参数
        if (CollectionUtils.isNotEmpty(this.getRest())) {
            sampler.setArguments(httpArguments(this.getRest()));
        }
        // 请求参数
        if (CollectionUtils.isNotEmpty(this.getArguments())) {
            sampler.setArguments(httpArguments(this.getArguments()));
        }
        // 请求体
        if (!StringUtils.equals(this.getMethod(), "GET")) {
            if (this.body != null) {
                List<KeyValue> bodyParams = this.body.getBodyParams(sampler, this.getId());
                if (StringUtils.isNotEmpty(this.body.getType()) && "Form Data".equals(this.body.getType())) {
                    sampler.setDoMultipart(true);
                }
                if (CollectionUtils.isNotEmpty(bodyParams)) {
                    sampler.setArguments(httpArguments(bodyParams));
                }
            }
        }

        final HashTree httpSamplerTree = tree.add(sampler);

        // 注意顺序，放在config前面，会优先于环境的请求头生效
        if (CollectionUtils.isNotEmpty(this.headers)) {
            setHeader(httpSamplerTree, this.headers);
        }

        // 通用请求Headers
        if (config != null && config.getConfig() != null && config.getConfig().get(this.getProjectId()).getHttpConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getHttpConfig().getHeaders())) {
            setHeader(httpSamplerTree, config.getConfig().get(this.getProjectId()).getHttpConfig().getHeaders());
        }

        //判断是否要开启DNS
        if (config != null && config.getConfig() != null && config.getConfig().get(this.getProjectId()).getCommonConfig() != null
                && config.getConfig().get(this.getProjectId()).getCommonConfig().isEnableHost()) {
            MsDNSCacheManager.addEnvironmentVariables(httpSamplerTree, this.getName(), config.getConfig().get(this.getProjectId()));
            MsDNSCacheManager.addEnvironmentDNS(httpSamplerTree, this.getName(), config.getConfig().get(this.getProjectId()));
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                el.toHashTree(httpSamplerTree, el.getHashTree(), config);
            }
        }
        if (this.authManager != null) {
            this.authManager.toHashTree(tree, hashTree, config);
        }
    }

    private boolean isVariable(String path, String value) {
        Pattern p = Pattern.compile("(\\$\\{)([\\w]+)(\\})");
        Matcher m = p.matcher(path);
        while (m.find()) {
            String group = m.group(2);
            if (group.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String getRestParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("/");
        Map<String, String> keyValueMap = new HashMap<>();
        this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue ->
                keyValueMap.put(keyValue.getName(), keyValue.getValue() != null && keyValue.getValue().startsWith("@") ?
                        ScriptEngineUtils.calculate(keyValue.getValue()) : keyValue.getValue())
        );
        try {
            Pattern p = Pattern.compile("(\\{)([\\w]+)(\\})");
            Matcher m = p.matcher(path);
            while (m.find()) {
                String group = m.group(2);
                if (!isVariable(path, group)) {
                    path = path.replace("{" + group + "}", keyValueMap.get(group));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return path;
    }

    private String getPostQueryParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("?");
        this.getArguments().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue -> {
            stringBuffer.append(keyValue.getName());
            if (keyValue.getValue() != null) {
                stringBuffer.append("=").append(keyValue.getValue().startsWith("@") ?
                        ScriptEngineUtils.calculate(keyValue.getValue()) : keyValue.getValue());
            }
            stringBuffer.append("&");
        });
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private Arguments httpArguments(List<KeyValue> list) {
        Arguments arguments = new Arguments();
        list.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue -> {
                    HTTPArgument httpArgument = new HTTPArgument(keyValue.getName(), StringUtils.isNotEmpty(keyValue.getValue()) && keyValue.getValue().startsWith("@") ? ScriptEngineUtils.calculate(keyValue.getValue()) : keyValue.getValue());
                    if (keyValue.getValue() == null) {
                        httpArgument.setValue("");
                    }
                    httpArgument.setAlwaysEncoded(keyValue.isEncode());
                    if (StringUtils.isNotBlank(keyValue.getContentType())) {
                        httpArgument.setContentType(keyValue.getContentType());
                    }
                    arguments.addArgument(httpArgument);
                }
        );
        return arguments;
    }

    public void setHeader(HashTree tree, List<KeyValue> headers) {
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                headerManager.add(new Header(keyValue.getName(), keyValue.getValue()))
        );
        tree.add(headerManager);
    }

    public boolean isURL(String str) {
        //转换为小写
        try {
            new URL(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isRest() {
        return this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).toArray().length > 0;
    }

}
