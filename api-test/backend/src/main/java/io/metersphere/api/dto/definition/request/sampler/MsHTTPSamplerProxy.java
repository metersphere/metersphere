package io.metersphere.api.dto.definition.request.sampler;

import io.metersphere.api.dto.definition.FakeError;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.mock.MockApiHeaders;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.CommonConfig;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.parse.api.JMeterScriptUtil;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.environment.service.CommandService;
import io.metersphere.environment.ssl.KeyStoreConfig;
import io.metersphere.environment.ssl.KeyStoreFile;
import io.metersphere.environment.ssl.MsKeyStore;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.KeystoreConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsHTTPSamplerProxy extends MsTestElement {
    private String type = ElementConstants.HTTP_SAMPLER;
    private String clazzName = MsHTTPSamplerProxy.class.getCanonicalName();
    private String protocol;
    private String domain;
    private String port;
    private String method;
    private String path;
    private String connectTimeout;
    private String responseTimeout;
    private List<KeyValue> headers;
    private Body body;
    private List<KeyValue> rest;
    private String url;
    private boolean followRedirects;
    private boolean autoRedirects;
    private boolean doMultipartPost;
    private String useEnvironment;
    private List<KeyValue> arguments;
    private MsAuthManager authManager;
    private Boolean isRefEnvironment;
    private String alias;
    private boolean customizeReq;
    private final static String DEF_TIME_OUT = "60000";
    //客户端实现
    private String implementation;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        if (StringUtils.isEmpty(this.getEnvironmentId())) {
            this.setEnvironmentId(this.useEnvironment);
        }

        // 非导出操作，且不是启用状态则跳过执行Ms
        if (!config.isOperating() && !this.isEnable()) {
            return;
        } else if (config.isOperating() && StringUtils.isNotEmpty(config.getOperatingSampleTestName())) {
            this.setName(config.getOperatingSampleTestName());
        }

        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            boolean ref = this.setRefElement();
            if (!ref) {
                return;
            }
            hashTree = this.getHashTree();
        }
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        if (StringUtils.isEmpty(this.getName())) {
            sampler.setName(ElementConstants.HTTP_SAMPLER);
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
        sampler.setContentEncoding(StandardCharsets.UTF_8.name());
        sampler.setFollowRedirects(this.isFollowRedirects());
        sampler.setAutoRedirects(this.isAutoRedirects());
        sampler.setImplementation(this.getImplementation());
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(this.isDoMultipartPost());
        if (MapUtils.isEmpty(config.getConfig()) && config.isApi()) {
            // 单独接口执行
            if (StringUtils.isNotEmpty(config.getCurrentProjectId())) {
                this.setProjectId(config.getCurrentProjectId());
            }
            String projectId = this.getProjectId();
            config.setConfig(ElementUtil.getEnvironmentConfig(this.useEnvironment, projectId));
            this.setProjectId(projectId);
        }
        config.compatible(this);
        this.initConnectAndResponseTimeout(config);
        sampler.setConnectTimeout(this.getConnectTimeout() == null ? DEF_TIME_OUT : this.getConnectTimeout());
        sampler.setResponseTimeout(this.getResponseTimeout() == null ? DEF_TIME_OUT : this.getResponseTimeout());
        HttpConfig httpConfig = getHttpConfig(config);
        setSamplerPath(config, httpConfig, sampler);
        // 请求体处理
        if (this.body != null) {
            List<KeyValue> bodyParams = this.body.getBodyParams(sampler, this.getId());
            if (StringUtils.isNotEmpty(this.body.getType()) && "Form Data".equals(this.body.getType())) {
                AtomicBoolean kvIsEmpty = new AtomicBoolean(true);
                this.body.getKvs().forEach(files -> {
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
                if (!arguments.getArguments().isEmpty()) {
                    sampler.setArguments(arguments);
                }
            }
        }
        // 失败重试
        HashTree httpSamplerTree;
        if (config.getRetryNum() > 0 && !ElementUtil.isLoop(this.getParent())) {
            final HashTree loopTree = ElementUtil.retryHashTree(this.getName(), config.getRetryNum(), tree);
            httpSamplerTree = loopTree.add(sampler);
        } else {
            httpSamplerTree = tree.add(sampler);
        }
        // 注意顺序，放在config前面，会优先于环境的请求头生效
        if (httpConfig != null && httpConfig.isMock() && StringUtils.isNotEmpty(this.getId())) {
            //如果选择的是mock环境，则自动添加一个apiHeader。
            AtomicBoolean headersHasMockApiId = new AtomicBoolean(false);
            if (CollectionUtils.isNotEmpty(this.headers)) {
                this.headers.forEach(item -> {
                    if (StringUtils.equals(item.getName(), MockApiHeaders.MOCK_API_RESOURCE_ID)) {
                        headersHasMockApiId.set(true);
                    }
                });
            }
            if (!headersHasMockApiId.get()) {
                this.headers.add(new KeyValue(MockApiHeaders.MOCK_API_RESOURCE_ID, this.getId()));
            }
        }
        if (CollectionUtils.isNotEmpty(this.headers)) {
            ElementUtil.setHeader(httpSamplerTree, this.headers, this.getName());
        }
        // 新版本符合条件 HTTP 请求头
        if (httpConfig != null && CollectionUtils.isNotEmpty(httpConfig.getHeaders())) {
            if (!this.isCustomizeReq() || this.isRefEnvironment) {
                // 如果不是自定义请求,或者引用环境则添加环境请求头
                ElementUtil.setHeader(httpSamplerTree, httpConfig.getHeaders(), this.getName());
            }
        }
        // 场景头
        if (CollectionUtils.isNotEmpty(config.getHeaders())) {
            ElementUtil.setHeader(httpSamplerTree, config.getHeaders(), this.getName());
        }
        // 环境通用请求头
        Arguments arguments = ElementUtil.getConfigArguments(config, this.getName(), this.getProjectId(), null);
        if (arguments != null) {
            httpSamplerTree.add(arguments);
        }
        //添加csv
        ElementUtil.addApiVariables(config, httpSamplerTree, this.getProjectId());
        //判断是否要开启DNS
        if (config.isEffective(this.getProjectId()) && config.get(this.getProjectId()).getCommonConfig() != null
                && config.get(this.getProjectId()).getCommonConfig().isEnableHost()) {
            MsDNSCacheManager.addEnvironmentVariables(httpSamplerTree, this.getName(), config.get(this.getProjectId()));
            MsDNSCacheManager.addEnvironmentDNS(httpSamplerTree, this.getName(), config.get(this.getProjectId()), httpConfig);
        }
        if (this.authManager != null && MsAuthManager.mechanismMap.containsKey(this.authManager.getVerification())) {
            this.authManager.setAuth(httpSamplerTree, this.authManager, sampler);
        }
        addCertificate(config, httpSamplerTree);
        if (httpConfig != null) {
            //根据配置增加全局前后至脚本
            JMeterScriptUtil.setScriptByHttpConfig(httpConfig, httpSamplerTree, config, useEnvironment, this.getEnvironmentId(), false);
            //增加误报、断言
            if (ObjectUtils.isNotEmpty(httpConfig.getFakeError())) {
                sampler.setProperty(SampleResult.MS_FAKE_ERROR, JSONUtil.toJSONString(httpConfig.getFakeError()));
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
                if (el instanceof MsJDBCPreProcessor || el instanceof MsJDBCPostProcessor) {
                    el.setParent(this);
                }
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
            MsHTTPSamplerProxy proxy = null;
            if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                ApiTestCaseWithBLOBs bloBs = CommonBeanFactory.getBean(ApiTestCaseService.class).get(this.getId());
                if (bloBs != null) {
                    this.setProjectId(bloBs.getProjectId());
                    JSONObject element = JSONUtil.parseObject(bloBs.getRequest());
                    ElementUtil.dataFormatting(element);
                    proxy = JSONUtil.parseObject(element.toString(), MsHTTPSamplerProxy.class);
                    this.setName(bloBs.getName());
                }
            }
            if (proxy != null) {
                if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
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
            String useEvnId = config.get(this.getProjectId()).getEnvironmentId();
            if (StringUtils.isNotEmpty(useEvnId) && !StringUtils.equals(useEvnId, this.getEnvironmentId())) {
                this.setEnvironmentId(useEvnId);
            }
            CommonConfig commonConfig = config.get(this.getProjectId()).getCommonConfig();
            if (commonConfig != null) {
                if (this.getConnectTimeout() == null || StringUtils.equals(this.getConnectTimeout(), DEF_TIME_OUT)) {
                    if (commonConfig.getRequestTimeout() != 0) {
                        this.setConnectTimeout(String.valueOf(commonConfig.getRequestTimeout()));
                    }
                }
                if (this.getResponseTimeout() == null || StringUtils.equals(this.getResponseTimeout(), DEF_TIME_OUT)) {
                    if (commonConfig.getResponseTimeout() != 0) {
                        this.setResponseTimeout(String.valueOf(commonConfig.getResponseTimeout()));
                    }
                }
            }
        }
    }

    private EnvironmentConfig getEnvironmentConfig(ParameterConfig config) {
        return config.get(this.getProjectId());
    }

    private HttpConfig getHttpConfig(ParameterConfig config) {
        if (config.isEffective(this.getProjectId()) || config.isEffective(config.getCurrentProjectId())) {
            EnvironmentConfig envConfig = config.get(this.getProjectId());
            if (envConfig != null) {
                String useEnvId = envConfig.getEnvironmentId();
                if (this.authManager == null && envConfig.getAuthManager() != null
                        && envConfig.getAuthManager().getAuthManager() != null) {
                    MsAuthManager authManager = new MsAuthManager();
                    BeanUtils.copyBean(authManager, envConfig.getAuthManager().getAuthManager());
                    this.authManager = authManager;
                }
                if (StringUtils.isNotEmpty(useEnvId) && !StringUtils.equals(useEnvId, this.getEnvironmentId())) {
                    this.setEnvironmentId(useEnvId);
                }
                HttpConfig httpConfig = config.matchConfig(this, envConfig.getHttpConfig());
                if (envConfig.getPreProcessor() != null) {
                    MsJSR223PreProcessor msJSR223PreProcessor = new MsJSR223PreProcessor();
                    if (envConfig.getPreProcessor() != null) {
                        BeanUtils.copyBean(msJSR223PreProcessor, envConfig.getPreProcessor());
                        httpConfig.setPreProcessor(msJSR223PreProcessor);
                    }
                }
                if (envConfig.getPostProcessor() != null) {
                    MsJSR223PostProcessor postProcessor = new MsJSR223PostProcessor();
                    if (envConfig.getPostProcessor() != null) {
                        BeanUtils.copyBean(postProcessor, envConfig.getPostProcessor());
                        httpConfig.setPostProcessor(postProcessor);
                    }
                }
                httpConfig.setGlobalScriptConfig(envConfig.getGlobalScriptConfig());
                if (CollectionUtils.isNotEmpty(envConfig.getAssertions())) {
                    httpConfig.setAssertions(ElementUtil.copyAssertion(envConfig.getAssertions()));
                }
                if ((!this.isCustomizeReq() || this.isRefEnvironment) && envConfig.isUseErrorCode()) {
                    FakeError fakeError = new FakeError();
                    fakeError.setHigherThanError(envConfig.isHigherThanError());
                    fakeError.setProjectId(this.getProjectId());
                    fakeError.setHigherThanSuccess(envConfig.isHigherThanSuccess());
                    httpConfig.setFakeError(fakeError);
                }
                return httpConfig;
            }
        }
        return null;
    }

    private void setSamplerPath(ParameterConfig config, HttpConfig httpConfig, HTTPSamplerProxy sampler) {
        try {
            if (config.isEffective(this.getProjectId())) {
                if (StringUtils.isEmpty(this.useEnvironment)) {
                    this.useEnvironment = config.get(this.getProjectId()).getEnvironmentId();
                }
                String url = httpConfig.getProtocol() + "://" + httpConfig.getSocket();
                if (isUrl()) {
                    //  正常全路径
                    fullPath(sampler, url);
                } else {
                    useEnvironmentSample(httpConfig, sampler, url);
                }
                String envPath = sampler.getPath();
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(envPath);
                    sampler.setProperty(HTTPSamplerBase.PATH, envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    String path = postQueryParameters(envPath);
                    if (HTTPConstants.DELETE.equals(this.getMethod()) && !path.startsWith("${") && !path.startsWith("/${")) {
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                    }
                    sampler.setProperty(HTTPSamplerBase.PATH, path);
                }
            } else {
                String envPath;
                try {
                    URL urlObject = new URL(url);
                    if (url.contains("${")) {
                        envPath = url;
                    } else {
                        sampler.setDomain(URLDecoder.decode(urlObject.getHost(), StandardCharsets.UTF_8));
                        envPath = urlObject.getPath();
                        sampler.setPort(urlObject.getPort());
                    }
                    sampler.setProtocol(urlObject.getProtocol());
                } catch (Exception e) {
                    envPath = url == null ? "" : url;
                }
                sampler.setProperty(HTTPSamplerBase.PATH, envPath, StandardCharsets.UTF_8.name());
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(URLDecoder.decode(URLEncoder.encode(envPath, StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                    sampler.setProperty(HTTPSamplerBase.PATH, envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    sampler.setProperty(HTTPSamplerBase.PATH, postQueryParameters(URLDecoder.decode(URLEncoder.encode(envPath, StandardCharsets.UTF_8), StandardCharsets.UTF_8)));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }

    private void useEnvironmentSample(HttpConfig httpConfig, HTTPSamplerProxy sampler, String url) throws MalformedURLException, UnsupportedEncodingException {
        if (!isCustomizeReq() || isRefEnvironment) {
            if (isCustomizeReqCompleteUrl(this.path)) {
                url = httpConfig.getProtocol() + "://" + httpConfig.getSocket();
            }
            String envPath = "";
            if (!isCustomizeReqCompleteUrl(this.path) || isRefEnvironment) {
                try {
                    URL urlObject = new URL(url);
                    if (StringUtils.isNotBlank(this.getPath())) {
                        envPath = this.getPath().startsWith("/") ? this.getPath() : StringUtils.join("/", this.getPath());
                    }
                    if (httpConfig.getSocket().contains("${")) {
                        envPath = StringUtils.isNotBlank(this.path) ? StringUtils.join(url, this.path) : url;
                    } else if (StringUtils.isNotEmpty(urlObject.getHost())) {
                        if (StringUtils.isNotBlank(urlObject.getPath()) && urlObject.getPath().endsWith("/")) {
                            envPath = StringUtils.join(urlObject.getPath().substring(0, urlObject.getPath().length() - 1), envPath);
                        } else {
                            envPath = StringUtils.join(urlObject.getPath(), envPath);
                        }
                        sampler.setDomain(URLDecoder.decode(urlObject.getHost(), StandardCharsets.UTF_8));
                        sampler.setProtocol(httpConfig.getProtocol());
                        sampler.setPort(urlObject.getPort());
                    } else {
                        sampler.setDomain("");
                        sampler.setProtocol("");
                        sampler.setPort(-1);
                    }
                } catch (Exception e) {
                    envPath = StringUtils.isNotBlank(this.path) ? StringUtils.join(url, this.path) : url;
                }
            } else {
                URL urlObject = new URL(this.path);
                envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getFile();
                sampler.setDomain(URLDecoder.decode(urlObject.getHost(), StandardCharsets.UTF_8));
                sampler.setProtocol(urlObject.getProtocol());
            }
            sampler.setProperty(HTTPSamplerBase.PATH, URLDecoder.decode(URLEncoder.encode(envPath, StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }

    private void fullPath(HTTPSamplerProxy sampler, String url) {
        try {
            if (this.isCustomizeReq() && StringUtils.isNotEmpty(this.getUrl())) {
                url = this.getUrl();
                sampler.setProperty(HTTPSamplerBase.PATH, url);
            }
            if (StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                url = url.replace(this.getPort(), "10990");
            }
            URL urlObject = new URL(url);
            String envPath;
            if (url.contains("${")) {
                envPath = url;
            } else {
                sampler.setDomain(URLDecoder.decode(urlObject.getHost(), StandardCharsets.UTF_8));
                if (urlObject.getPort() == 10990 && StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                    sampler.setProperty(HTTPSamplerBase.PORT, this.getPort());
                } else if (urlObject.getPort() != -1) {
                    sampler.setPort(urlObject.getPort());
                }
                envPath = urlObject.getPath();
            }

            sampler.setProtocol(urlObject.getProtocol());
            sampler.setProperty(HTTPSamplerBase.PATH, URLDecoder.decode(envPath, StandardCharsets.UTF_8), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            sampler.setProperty(HTTPSamplerBase.PATH, url);
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 加载SSL认证
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
                    keystoreConfig.setProperty(ElementConstants.MS_KEYSTORE_FILE_PATH, msKeyStore.getPath());
                    keystoreConfig.setProperty(ElementConstants.MS_KEYSTORE_FILE_PASSWORD, msKeyStore.getPassword());
                    httpSamplerTree.add(keystoreConfig);
                    config.getKeyStoreMap().put(this.getProjectId(), new MsKeyStore(msKeyStore.getPath(), msKeyStore.getPassword()));
                }
            }
        }
    }

    /**
     * 自定义请求如果是完整url时不拼接mock信息
     */
    private boolean isCustomizeReqCompleteUrl(String url) {
        return isCustomizeReq() && StringUtils.isNotEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private boolean isUrl() {
        // 自定义字段没有引用环境则非url
        if (this.isCustomizeReq()) {
            return !this.isRefEnvironment;
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
                    value = keyValue.isUrlEncode() ? StringUtils.join("${__urlencode(", value.replace(",", "\\,"), ")}") : value;
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
            LoggerUtil.error(this.getName(), ex);
        }
        return path;
    }

    private String postQueryParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        if (path.contains("?")) {
            stringBuffer.append("&");
        } else {
            stringBuffer.append("?");
        }
        this.getArguments().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue -> {
            stringBuffer.append(keyValue.isUrlEncode() ? StringUtils.join("${__urlencode(", keyValue.getName(), ")}") : keyValue.getName());
            if (keyValue.getValue() != null) {
                try {
                    String value = keyValue.getValue().startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue();
                    value = keyValue.isUrlEncode() ? StringUtils.join("${__urlencode(", value.replace(",", "\\,"), ")}") : value;
                    if (StringUtils.isNotEmpty(value) && value.contains(StringUtils.CR)) {
                        value = value.replaceAll(StringUtils.CR, StringUtils.EMPTY);
                    }
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
                                LogUtil.error(e.getMessage(), e);
                            }
                        }
                );
        return arguments;
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