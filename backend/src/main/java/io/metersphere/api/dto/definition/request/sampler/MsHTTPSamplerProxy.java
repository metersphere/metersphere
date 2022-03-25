package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.parse.JMeterScriptUtil;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.CommonConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.ssl.KeyStoreConfig;
import io.metersphere.api.dto.ssl.KeyStoreFile;
import io.metersphere.api.dto.ssl.MsKeyStore;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.api.service.CommandService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.KeystoreConfig;
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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "HTTPSamplerProxy")
public class MsHTTPSamplerProxy extends MsTestElement {
    private String type = "HTTPSamplerProxy";
    private String clazzName = MsHTTPSamplerProxy.class.getCanonicalName();

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

    @JSONField(ordinal = 36)
    private MsAuthManager authManager;

    @JSONField(ordinal = 37)
    private Boolean isRefEnvironment;

    @JSONField(ordinal = 38)
    private String alias;

    @JSONField(ordinal = 39)
    private boolean customizeReq;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        if (StringUtils.isEmpty(this.getEnvironmentId())) {
            this.setEnvironmentId(this.useEnvironment);
        }
        // 非导出操作，且不是启用状态则跳过执行Ms
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            boolean ref = this.setRefElement();
            if (!ref) {
                LoggerUtil.debug("引用对象已经被删除：" + this.getId());
                return;
            }
            hashTree = this.getHashTree();
        }
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        if (StringUtils.isEmpty(this.getName())) {
            sampler.setName("HTTPSamplerProxy");
        }
        if (config.isOperating()) {
            String[] testNameArr = sampler.getName().split("<->");
            if (testNameArr.length > 0) {
                String testName = testNameArr[0];
                sampler.setName(testName);
            }
        }
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        ElementUtil.setBaseParams(sampler, this.getParent(), config, this.getId(), this.getIndex());
        sampler.setMethod(this.getMethod());
        sampler.setContentEncoding("UTF-8");
        sampler.setFollowRedirects(this.isFollowRedirects());
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(this.isDoMultipartPost());
        if (config.getConfig() == null) {
            // 单独接口执行
            if (StringUtils.isNotEmpty(config.getProjectId())) {
                this.setProjectId(config.getProjectId());
            }
            String projectId = this.getProjectId();
            config.setConfig(ElementUtil.getEnvironmentConfig(this.useEnvironment, projectId, this.isMockEnvironment()));
            this.setProjectId(projectId);
        }

        config.compatible(this);

        this.initConnectAndResponseTimeout(config);
        sampler.setConnectTimeout(this.getConnectTimeout() == null ? "60000" : this.getConnectTimeout());
        sampler.setResponseTimeout(this.getResponseTimeout() == null ? "60000" : this.getResponseTimeout());

        HttpConfig httpConfig = getHttpConfig(config);

        setSamplerPath(config, httpConfig, sampler);

        // 请求体处理
        if (this.body != null) {
            List<KeyValue> bodyParams = this.body.getBodyParams(sampler, this.getId());
            if (StringUtils.isNotEmpty(this.body.getType()) && "Form Data".equals(this.body.getType())) {
                AtomicBoolean kvIsEmpty = new AtomicBoolean(true);
                this.body.getKvs().forEach(files -> {
                    if (StringUtils.isNotEmpty(files.getName()) && "file".equals(files.getType()) && CollectionUtils.isNotEmpty(files.getFiles())) {
                        sampler.setDoBrowserCompatibleMultipart(true);
                    }
                    if (StringUtils.isNotEmpty(files.getName())) {
                        kvIsEmpty.set(false);
                    }
                });
                //值不为空时才会设置doMultiPart
                if (!kvIsEmpty.get()) {
                    sampler.setDoMultipart(true);
                }
            }
            if (CollectionUtils.isNotEmpty(bodyParams)) {
                Arguments arguments = httpArguments(bodyParams);
                if (arguments != null && !arguments.getArguments().isEmpty()) {
                    sampler.setArguments(arguments);
                }
            }
        }

        final HashTree httpSamplerTree = tree.add(sampler);

        // 注意顺序，放在config前面，会优先于环境的请求头生效
        if (CollectionUtils.isNotEmpty(this.headers)) {
            setHeader(httpSamplerTree, this.headers);
        }
        // 新版本符合条件 HTTP 请求头
        if (httpConfig != null && CollectionUtils.isNotEmpty(httpConfig.getHeaders())) {
            if (!this.isCustomizeReq() || this.isRefEnvironment) {
                // 如果不是自定义请求,或者引用环境则添加环境请求头
                setHeader(httpSamplerTree, httpConfig.getHeaders());
            }
        }
        // 场景头
        if (config != null && CollectionUtils.isNotEmpty(config.getHeaders())) {
            setHeader(httpSamplerTree, config.getHeaders());
        }
        // 环境通用请求头
        Arguments arguments = getConfigArguments(config);
        if (arguments != null) {
            httpSamplerTree.add(arguments);
        }
        //判断是否要开启DNS
        if (config.isEffective(this.getProjectId()) && config.getConfig().get(this.getProjectId()).getCommonConfig() != null
                && config.getConfig().get(this.getProjectId()).getCommonConfig().isEnableHost()) {
            MsDNSCacheManager.addEnvironmentVariables(httpSamplerTree, this.getName(), config.getConfig().get(this.getProjectId()));
            MsDNSCacheManager.addEnvironmentDNS(httpSamplerTree, this.getName(), config.getConfig().get(this.getProjectId()), httpConfig);
        }

        if (this.authManager != null && StringUtils.equals(this.authManager.getVerification(), "Basic Auth")) {
            this.authManager.setAuth(httpSamplerTree, this.authManager, sampler);
        }

        addCertificate(config, httpSamplerTree);

        if (httpConfig != null) {
            //根据配置增加全局前后至脚本
            JMeterScriptUtil.setScriptByHttpConfig(httpConfig, httpSamplerTree, config, useEnvironment, this.getEnvironmentId(), false);
            //增加误报、断言
            if (!config.isOperating() && CollectionUtils.isNotEmpty(httpConfig.getErrorReportAssertions())) {
                for (MsAssertions assertion : httpConfig.getErrorReportAssertions()) {
                    assertion.toHashTree(httpSamplerTree, assertion.getHashTree(), config);
                }
            }
            if (CollectionUtils.isNotEmpty(httpConfig.getAssertions())) {
                for (MsAssertions assertion : httpConfig.getAssertions()) {
                    assertion.toHashTree(httpSamplerTree, assertion.getHashTree(), config);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree = ElementUtil.order(hashTree);
            for (MsTestElement el : hashTree) {
                if (el.getEnvironmentId() == null) {
                    if (this.getEnvironmentId() == null) {
                        el.setEnvironmentId(useEnvironment);
                    } else {
                        el.setEnvironmentId(this.getEnvironmentId());
                    }
                }
                el.toHashTree(httpSamplerTree, el.getHashTree(), config);
            }
        }

        //根据配置增加全局前后至脚本
        if (httpConfig != null) {
            JMeterScriptUtil.setScriptByHttpConfig(httpConfig, httpSamplerTree, config, useEnvironment, this.getEnvironmentId(), true);
        }
    }

    private boolean setRefElement() {
        try {
            ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MsHTTPSamplerProxy proxy = null;
            if (StringUtils.equals(this.getRefType(), "CASE")) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setProjectId(bloBs.getProjectId());
                    JSONObject element = JSON.parseObject(bloBs.getRequest());
                    ElementUtil.dataFormatting(element);
                    proxy = mapper.readValue(element.toJSONString(), new TypeReference<MsHTTPSamplerProxy>() {
                    });
                    this.setName(bloBs.getName());
                }
            } else {
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setName(apiDefinition.getName());
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = mapper.readValue(apiDefinition.getRequest(), new TypeReference<MsHTTPSamplerProxy>() {
                    });
                }
            }
            if (proxy != null) {
                if (StringUtils.equals(this.getRefType(), "CASE")) {
                    ElementUtil.mergeHashTree(this, proxy.getHashTree());
                } else {
                    this.setHashTree(proxy.getHashTree());
                }
                this.setPath(proxy.getPath());
                this.setMethod(proxy.getMethod());
                this.setBody(proxy.getBody());
                this.setRest(proxy.getRest());
                this.setArguments(proxy.getArguments());
                this.setHeaders(proxy.getHeaders());
                return true;
            }
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return false;
    }

    private void initConnectAndResponseTimeout(ParameterConfig config) {
        if (config.isEffective(this.getProjectId())) {
            String useEvnId = config.getConfig().get(this.getProjectId()).getApiEnvironmentid();
            if (StringUtils.isNotEmpty(useEvnId) && !StringUtils.equals(useEvnId, this.getEnvironmentId())) {
                this.setEnvironmentId(useEvnId);
            }
            CommonConfig commonConfig = config.getConfig().get(this.getProjectId()).getCommonConfig();
            if (commonConfig != null) {
                if (this.getConnectTimeout() == null || StringUtils.equals(this.getConnectTimeout(), "60000")) {
                    if (commonConfig.getRequestTimeout() != 0) {
                        this.setConnectTimeout(String.valueOf(commonConfig.getRequestTimeout()));
                    }
                }
                if (this.getResponseTimeout() == null || StringUtils.equals(this.getResponseTimeout(), "60000")) {
                    if (commonConfig.getResponseTimeout() != 0) {
                        this.setResponseTimeout(String.valueOf(commonConfig.getResponseTimeout()));
                    }
                }
            }
        }
    }

    private EnvironmentConfig getEnvironmentConfig(ParameterConfig config) {
        return config.getConfig().get(this.getProjectId());
    }

    private HttpConfig getHttpConfig(ParameterConfig config) {
        if (config.isEffective(this.getProjectId())) {
            EnvironmentConfig environmentConfig = config.getConfig().get(this.getProjectId());
            if (environmentConfig != null) {
                String useEvnId = environmentConfig.getApiEnvironmentid();
                if (this.authManager == null && environmentConfig.getAuthManager() != null && environmentConfig.getAuthManager().containsKey("authManager")) {
                    try {
                        JSONObject authObject = environmentConfig.getAuthManager().getJSONObject("authManager");
                        if (authObject != null) {
                            if (authObject.containsKey("verification") && !StringUtils.equalsIgnoreCase(authObject.getString("verification"), "No Auth")) {
                                this.authManager = authObject.toJavaObject(MsAuthManager.class);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                if (StringUtils.isNotEmpty(useEvnId) && !StringUtils.equals(useEvnId, this.getEnvironmentId())) {
                    this.setEnvironmentId(useEvnId);
                }
                HttpConfig httpConfig = config.matchConfig(this);
                httpConfig.setPreProcessor(environmentConfig.getPreProcessor());
                httpConfig.setPostProcessor(environmentConfig.getPostProcessor());
                httpConfig.setGlobalScriptConfig(environmentConfig.getGlobalScriptConfig());
                httpConfig.setAssertions(environmentConfig.getAssertions());
                if (environmentConfig.isUseErrorCode()) {
                    httpConfig.setErrorReportAssertions(HashTreeUtil.getErrorReportByProjectId(this.getProjectId()));
                }
                return httpConfig;
            }
        }
        return null;
    }

    private void setSamplerPath(ParameterConfig config, HttpConfig httpConfig, HTTPSamplerProxy sampler) {
        try {
            if (config.isEffective(this.getProjectId())) {
                if (httpConfig == null && !ElementUtil.isURL(this.getUrl())) {
                    MSException.throwException("未匹配到环境，请检查环境配置");
                }
                if (StringUtils.isEmpty(httpConfig.getProtocol())) {
                    MSException.throwException(this.getName() + "接口，对应的环境无协议，请完善环境信息");
                }
                if (StringUtils.isEmpty(this.useEnvironment)) {
                    this.useEnvironment = config.getConfig().get(this.getProjectId()).getApiEnvironmentid();
                }
                String url = httpConfig.getProtocol() + "://" + httpConfig.getSocket();

                // 补充如果是完整URL 则用自身URL
                if (StringUtils.isNotEmpty(this.getUrl()) && ElementUtil.isURL(this.getUrl())) {
                    url = this.getUrl();
                }
                if (isUrl()) {
                    if (this.isCustomizeReq() && StringUtils.isNotEmpty(this.getUrl())) {
                        url = this.getUrl();
                        sampler.setProperty("HTTPSampler.path", url);
                    }
                    if (StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                        url = url.replace(this.getPort(), "10990");
                    }
                    try {
                        URL urlObject = new URL(url);
                        sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));

                        if (urlObject.getPort() > 0 && urlObject.getPort() == 10990 && StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                            sampler.setProperty("HTTPSampler.port", this.getPort());
                        } else {
                            sampler.setPort(urlObject.getPort());
                        }
                        sampler.setProtocol(urlObject.getProtocol());
                        sampler.setProperty("HTTPSampler.path", URLDecoder.decode(URLEncoder.encode(urlObject.getFile(), "UTF-8"), "UTF-8"));
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage(), e);
                    }
                } else {
                    if (!isCustomizeReq() || isRefEnvironment) {
                        if (isCustomizeReqCompleteUrl(this.path)) {
                            url = httpConfig.getProtocol() + "://" + httpConfig.getSocket();
                        }
                        String envPath = "";
                        if (!isCustomizeReqCompleteUrl(this.path) || isRefEnvironment) {
                            URL urlObject = new URL(url);
                            envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getFile();
                            if (StringUtils.isNotBlank(this.getPath())) {
                                envPath += this.getPath();
                            }
                            sampler.setPort(httpConfig.getPort());
                            if (StringUtils.isNotEmpty(httpConfig.getDomain())) {
                                sampler.setDomain(URLDecoder.decode(httpConfig.getDomain(), "UTF-8"));
                                sampler.setProtocol(httpConfig.getProtocol());
                            } else {
                                sampler.setDomain("");
                                sampler.setProtocol("");
                                sampler.setPort(-1);
                            }
                        } else {
                            URL urlObject = new URL(this.path);
                            envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getFile();
                            sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                            sampler.setProtocol(urlObject.getProtocol());
                        }
                        if (StringUtils.isNotEmpty(envPath) && !envPath.startsWith("/")) {
                            envPath = "/" + envPath;
                        }
                        sampler.setProperty("HTTPSampler.path", URLDecoder.decode(URLEncoder.encode(envPath, "UTF-8"), "UTF-8"));
                    }
                }
                String envPath = sampler.getPath();
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(envPath);
                    sampler.setProperty("HTTPSampler.path", envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    String path = envPath;
                    if (StringUtils.equalsIgnoreCase(this.getMethod(), "GET")) {
                        getQueryParameters(sampler);
                    } else {
                        path = postQueryParameters(envPath);
                    }
                    if (HTTPConstants.DELETE.equals(this.getMethod()) && !path.startsWith("${")) {
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                        String port = sampler.getPort() != 80 ? ":" + sampler.getPort() : "";
                        if (StringUtils.equals("https", sampler.getProtocol()) && sampler.getPort() == 443) {
                            // 解决https delete请求时，path路径带443端口，请求头的host会变成域名加443
                            port = "";
                        }
                        path = sampler.getProtocol() + "://" + sampler.getDomain() + port + path;
                    }
                    sampler.setProperty("HTTPSampler.path", path);
                }
            } else {
                String url = this.getUrl();
                if (StringUtils.isNotEmpty(url) && !url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                if (StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                    url = url.replace(this.getPort(), "10990");
                }
                if (StringUtils.isEmpty(url)) {
                    MSException.throwException("请重新选择环境");
                }
                URL urlObject = new URL(url);
                sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                if (urlObject.getPort() > 0 && urlObject.getPort() == 10990 && StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                    sampler.setProperty("HTTPSampler.port", this.getPort());
                } else {
                    sampler.setPort(urlObject.getPort());
                }
                sampler.setProtocol(urlObject.getProtocol());
                String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getFile();
                sampler.setProperty("HTTPSampler.path", envPath);
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(URLDecoder.decode(URLEncoder.encode(envPath, "UTF-8"), "UTF-8"));
                    sampler.setProperty("HTTPSampler.path", envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    if (StringUtils.equalsIgnoreCase(this.getMethod(), "GET")) {
                        getQueryParameters(sampler);
                    } else {
                        sampler.setProperty("HTTPSampler.path", postQueryParameters(URLDecoder.decode(URLEncoder.encode(envPath, "UTF-8"), "UTF-8")));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    /**
     * 加载SSL认证
     *
     * @param config
     * @param httpSamplerTree
     * @return
     */
    private void addCertificate(ParameterConfig config, HashTree httpSamplerTree) {
        if (config != null && config.isEffective(this.getProjectId()) && getEnvironmentConfig(config).getSslConfig() != null) {
            KeyStoreConfig sslConfig = getEnvironmentConfig(config).getSslConfig();
            List<KeyStoreFile> files = sslConfig.getFiles();
            if (CollectionUtils.isNotEmpty(files)) {
                MsKeyStore msKeyStore = config.getKeyStoreMap().get(this.getProjectId());
                CommandService commandService = CommonBeanFactory.getBean(CommandService.class);
                if (msKeyStore == null) {
                    msKeyStore = new MsKeyStore();
                    if (files.size() == 1) {
                        // 加载认证文件
                        KeyStoreFile file = files.get(0);
                        msKeyStore.setPath(FileUtils.BODY_FILE_DIR + "/ssl/" + file.getId() + "_" + file.getName());
                        msKeyStore.setPassword(file.getPassword());
                    } else {
                        // 合并多个认证文件
                        msKeyStore.setPath(FileUtils.BODY_FILE_DIR + "/ssl/tmp." + this.getId() + ".jks");
                        msKeyStore.setPassword("ms123...");
                        commandService.mergeKeyStore(msKeyStore.getPath(), sslConfig);
                    }
                }
                if (StringUtils.isEmpty(this.alias)) {
                    this.alias = sslConfig.getDefaultAlias();
                } else {
                    this.alias = sslConfig.getAlias(this.alias);
                }

                if (StringUtils.isNotEmpty(this.alias)) {
                    String aliasVar = "User-Defined-KeyStore-" + this.alias.trim();
                    this.addArguments(httpSamplerTree, aliasVar, this.alias.trim());
                    // 校验 keystore
                    commandService.checkKeyStore(msKeyStore.getPassword(), msKeyStore.getPath());

                    KeystoreConfig keystoreConfig = new KeystoreConfig();
                    keystoreConfig.setEnabled(true);
                    keystoreConfig.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "-KeyStore" : "KeyStore");
                    keystoreConfig.setProperty(TestElement.TEST_CLASS, KeystoreConfig.class.getName());
                    keystoreConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    keystoreConfig.setProperty("clientCertAliasVarName", aliasVar);
                    keystoreConfig.setProperty("endIndex", -1);
                    keystoreConfig.setProperty("preload", true);
                    keystoreConfig.setProperty("startIndex", 0);
                    keystoreConfig.setProperty("MS-KEYSTORE-FILE-PATH", msKeyStore.getPath());
                    keystoreConfig.setProperty("MS-KEYSTORE-FILE-PASSWORD", msKeyStore.getPassword());
                    httpSamplerTree.add(keystoreConfig);

                    config.getKeyStoreMap().put(this.getProjectId(), new MsKeyStore(msKeyStore.getPath(), msKeyStore.getPassword()));
                }
            }
        }
    }

    /**
     * 自定义请求如果是完整url时不拼接mock信息
     *
     * @param url
     * @return
     */
    private boolean isCustomizeReqCompleteUrl(String url) {
        if (isCustomizeReq() && StringUtils.isNotEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
            return true;
        }
        return false;
    }

    private boolean isUrl() {
        // 自定义字段没有引用环境则非url
        if (this.isCustomizeReq()) {
            if (this.isRefEnvironment) {
                return false;
            }
            return true;
        }
        if (StringUtils.isNotEmpty(this.getUrl()) && ElementUtil.isURL(this.getUrl())) {
            return true;
        }
        return false;
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
        List<KeyValue> list = this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid)
                .filter(KeyValue::valueIsNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(keyValue -> {
                try {
                    String value = keyValue.getValue() != null && keyValue.getValue().startsWith("@") ?
                            ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue();
                    value = keyValue.isUrlEncode() ? "${__urlencode(" + value + ")}" : value;
                    keyValueMap.put(keyValue.getName(), value);
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            });
        }
        try {
            Pattern p = Pattern.compile("(\\{)([\\w]+)(\\})");
            Matcher m = p.matcher(path);
            while (m.find()) {
                String group = m.group(2);
                if (!isVariable(path, group) && keyValueMap.containsKey(group)) {
                    path = path.replace("{" + group + "}", keyValueMap.get(group));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return path;
    }

    private void getQueryParameters(HTTPSamplerProxy sampler) {
        Arguments arguments = httpArguments(this.getArguments());
        if (arguments != null && !arguments.getArguments().isEmpty()) {
            sampler.setArguments(arguments);
        }
    }

    private String postQueryParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        if (path.indexOf("?") != -1) {
            stringBuffer.append("&");
        } else {
            stringBuffer.append("?");
        }
        this.getArguments().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue -> {
            stringBuffer.append(keyValue.getName());
            if (keyValue.getValue() != null) {
                try {
                    String value = keyValue.getValue().startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue();
                    value = keyValue.isUrlEncode() ? "${__urlencode(" + value + ")}" : value;
                    stringBuffer.append("=").append(value);
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
            stringBuffer.append("&");
        });
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private Arguments httpArguments(List<KeyValue> list) {
        Arguments arguments = new Arguments();
        list.stream().
                filter(KeyValue::isValid).
                filter(KeyValue::isEnable).forEach(keyValue -> {
                            try {
                                String value = StringUtils.isNotEmpty(keyValue.getValue()) && keyValue.getValue().startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue();
                                HTTPArgument httpArgument = new HTTPArgument(keyValue.getName(), value);
                                if (keyValue.getValue() == null) {
                                    httpArgument.setValue("");
                                }
                                httpArgument.setAlwaysEncoded(keyValue.isUrlEncode());
                                if (StringUtils.isNotBlank(keyValue.getContentType())) {
                                    httpArgument.setContentType(keyValue.getContentType());
                                }
                                if (StringUtils.equalsIgnoreCase(this.method, "get")) {
                                    if (StringUtils.isNotEmpty(httpArgument.getValue())) {
                                        arguments.addArgument(httpArgument);
                                    }
                                } else {
                                    arguments.addArgument(httpArgument);
                                }
                            } catch (Exception e) {

                            }
                        }
                );
        return arguments;
    }

    public void setHeader(HashTree tree, List<KeyValue> headers) {
        // 合并header
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "HeaderManager" : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        boolean isAdd = true;
        for (Object key : tree.keySet()) {
            if (key instanceof HeaderManager) {
                headerManager = (HeaderManager) key;
                isAdd = false;
            }
        }
        //  header 也支持 mock 参数
        List<KeyValue> keyValues = headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).collect(Collectors.toList());
        for (KeyValue keyValue : keyValues) {
            boolean hasHead = false;
            //检查是否已经有重名的Head。如果Header重复会导致执行报错
            if (headerManager.getHeaders() != null) {
                for (int i = 0; i < headerManager.getHeaders().size(); i++) {
                    Header header = headerManager.getHeader(i);
                    String headName = header.getName();
                    if (StringUtils.equals(headName, keyValue.getName()) && !StringUtils.equals(headName, "Cookie")) {
                        hasHead = true;
                        break;
                    }
                }
            }

            if (!hasHead) {
                headerManager.add(new Header(keyValue.getName(), ScriptEngineUtils.buildFunctionCallString(keyValue.getValue())));
            }
        }
        if (headerManager.getHeaders().size() > 0 && isAdd) {
            tree.add(headerManager);
        }
    }

    /**
     * 环境通用变量，这里只适用用接口定义和用例，场景自动化会加到场景中
     */
    private Arguments getConfigArguments(ParameterConfig config) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "Arguments");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        // 环境通用变量
        if (config.isEffective(this.getProjectId()) && config.getConfig().get(this.getProjectId()).getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables())) {
            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
            // 清空变量，防止重复添加
            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().clear();
        }
        if (arguments.getArguments() != null && arguments.getArguments().size() > 0) {
            return arguments;
        }
        return null;
    }

    private void addArguments(HashTree tree, String key, String value) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName("User Defined KeyStoreAlias");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        arguments.addArgument(key, value, "=");
        tree.add(arguments);
    }

    private boolean isRest() {
        return this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).toArray().length > 0;
    }

    public static List<MsHTTPSamplerProxy> findHttpSampleFromHashTree(MsTestElement hashTree) {
        return ElementUtil.findFromHashTreeByType(hashTree, MsHTTPSamplerProxy.class, null);
    }
}

