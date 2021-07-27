package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.HttpConfig;
import io.metersphere.api.dto.scenario.HttpConfigCondition;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.ssl.KeyStoreConfig;
import io.metersphere.api.dto.ssl.KeyStoreFile;
import io.metersphere.api.dto.ssl.MsKeyStore;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.api.service.CommandService;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.commons.constants.ConditionType;
import io.metersphere.commons.constants.DelimiterConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.RunModeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.track.service.TestPlanApiCaseService;
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
import java.util.*;
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

    @JSONField(ordinal = 36)
    private MsAuthManager authManager;

    @JSONField(ordinal = 37)
    private Boolean isRefEnvironment;

    @JSONField(ordinal = 38)
    private String alias;

    private void setRefElement() {
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
                    proxy = mapper.readValue(bloBs.getRequest(), new TypeReference<MsHTTPSamplerProxy>() {
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
                this.setHashTree(proxy.getHashTree());
                this.setMethod(proxy.getMethod());
                this.setBody(proxy.getBody());
                this.setRest(proxy.getRest());
                this.setArguments(proxy.getArguments());
                this.setHeaders(proxy.getHeaders());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.error(ex.getMessage());
        }
    }

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if(StringUtils.isEmpty(this.getEnvironmentId())){
            this.setEnvironmentId(this.useEnvironment);
        }
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            this.setRefElement();
            hashTree = this.getHashTree();
        }
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        if (StringUtils.isEmpty(this.getName())) {
            sampler.setName("HTTPSamplerProxy");
        }
        String name = this.getParentName(this.getParent());
        if (StringUtils.isNotEmpty(name) && !config.isOperating()) {
            sampler.setName(this.getName() + DelimiterConstants.SEPARATOR.toString() + name);
        }
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        sampler.setProperty("MS-ID", this.getId());
        sampler.setProperty("MS-RESOURCE-ID", this.getResourceId());
        List<String> id_names = new LinkedList<>();
        this.getScenarioSet(this, id_names);
        sampler.setProperty("MS-SCENARIO", JSON.toJSONString(id_names));

        sampler.setMethod(this.getMethod());
        sampler.setContentEncoding("UTF-8");
        sampler.setConnectTimeout(this.getConnectTimeout() == null ? "6000" : this.getConnectTimeout());
        sampler.setResponseTimeout(this.getResponseTimeout() == null ? "6000" : this.getResponseTimeout());
        sampler.setFollowRedirects(this.isFollowRedirects());
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(this.isDoMultipartPost());
        if (config.getConfig() == null) {
            // 单独接口执行
            this.setProjectId(config.getProjectId());
            config.setConfig(getEnvironmentConfig(useEnvironment));
        }

        compatible(config);

        HttpConfig httpConfig = getHttpConfig(config);

        setSamplerPath(config, httpConfig, sampler);

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
        // 新版本符合条件 HTTP 请求头
        if (httpConfig != null && CollectionUtils.isNotEmpty(httpConfig.getHeaders())) {
            if (!this.isCustomizeReq() || this.isRefEnvironment) {
                // 如果不是自定义请求,或者引用环境则添加环境请求头
                setHeader(httpSamplerTree, httpConfig.getHeaders());
            }
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

        if (this.authManager != null) {
            this.authManager.setAuth(tree, this.authManager, sampler);
        }

        addCertificate(config, httpSamplerTree);

        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                if(this.getEnvironmentId() == null){
                    el.setEnvironmentId(useEnvironment);
                }
                el.toHashTree(httpSamplerTree, el.getHashTree(), config);
            }
        }

    }

    private EnvironmentConfig getEnvironmentConfig(ParameterConfig config) {
        return config.getConfig().get(this.getProjectId());
    }

    private HttpConfig getHttpConfig(ParameterConfig config) {
        if (config.isEffective(this.getProjectId())) {
            String useEvnId = config.getConfig().get(this.getProjectId()).getApiEnvironmentid();
            if(StringUtils.isNotEmpty(useEvnId) && !StringUtils.equals(useEvnId,this.getEnvironmentId())){
                this.setEnvironmentId(useEvnId);
            }
            return getHttpConfig(config.getConfig().get(this.getProjectId()).getHttpConfig());
        }
        return null;
    }

    private void setSamplerPath(ParameterConfig config, HttpConfig httpConfig, HTTPSamplerProxy sampler) {
        try {
            if (config.isEffective(this.getProjectId())) {
                if (httpConfig == null && !isURL(this.getUrl())) {
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

                if (StringUtils.isNotEmpty(this.getUrl()) && isURL(this.getUrl())) {
                    url = this.getUrl();
                }

                if (isUrl()) {
                    if (this.isCustomizeReq()) {
                        url = this.getUrl();
                        sampler.setPath(url);
                    }
                    if (StringUtils.isNotEmpty(this.getPort()) && this.getPort().startsWith("${")) {
                        url = url.replaceAll(this.getPort(), "10990");
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
                        sampler.setPath(URLDecoder.decode(URLEncoder.encode(urlObject.getFile(), "UTF-8"), "UTF-8"));
                    } catch (Exception e) {
                        LogUtil.error(e.getMessage(), e);
                    }
                } else {
                    if (!isCustomizeReq() || isRefEnvironment) {
                        if (isCustomizeReqCompleteUrl(this.path)) {
                            url = httpConfig.getProtocol() + "://" + httpConfig.getSocket();
                        }
                        String envPath = "";
                        if (!isCustomizeReqCompleteUrl(this.path)) {
                            URL urlObject = new URL(url);
                            envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getFile();
                            if (StringUtils.isNotBlank(this.getPath())) {
                                envPath += this.getPath();
                            }
                            if (StringUtils.isNotEmpty(httpConfig.getDomain())) {
                                sampler.setDomain(URLDecoder.decode(httpConfig.getDomain(), "UTF-8"));
                                sampler.setProtocol(httpConfig.getProtocol());
                            } else {
                                sampler.setDomain("");
                                sampler.setProtocol("");
                            }
                            sampler.setPort(httpConfig.getPort());
                        } else {
                            URL urlObject = new URL(this.path);
                            envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getFile();
                            sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                            sampler.setProtocol(urlObject.getProtocol());
                        }
                        sampler.setPath(URLDecoder.decode(URLEncoder.encode(envPath, "UTF-8"), "UTF-8"));
                    }
                }
                String envPath = sampler.getPath();
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(envPath);
                    sampler.setPath(envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    String path = getPostQueryParameters(envPath);
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
                    url.replaceAll(this.getPort(), "10990");
                }
                if (url == null) {
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
                sampler.setPath(envPath);
                if (CollectionUtils.isNotEmpty(this.getRest()) && this.isRest()) {
                    envPath = getRestParameters(URLDecoder.decode(URLEncoder.encode(envPath, "UTF-8"), "UTF-8"));
                    sampler.setPath(envPath);
                }
                if (CollectionUtils.isNotEmpty(this.getArguments())) {
                    sampler.setPath(getPostQueryParameters(URLDecoder.decode(URLEncoder.encode(envPath, "UTF-8"), "UTF-8")));
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
                }

                if (StringUtils.isNotEmpty(this.alias)) {
                    String aliasVar = UUID.randomUUID().toString();
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
        if (isCustomizeReq() && (url.startsWith("http://") || url.startsWith("https://"))) {
            return true;
        }
        return false;
    }

    // 兼容旧数据
    private void compatible(ParameterConfig config) {
        if (this.isCustomizeReq() && this.isRefEnvironment == null) {
            if (StringUtils.isNotBlank(this.url)) {
                this.isRefEnvironment = false;
            } else {
                this.isRefEnvironment = true;
            }
        }

        // 数据兼容处理
        if (config.getConfig() != null && StringUtils.isNotEmpty(this.getProjectId()) && config.getConfig().containsKey(this.getProjectId())) {
            // 1.8 之后 当前正常数据
        } else if (config.getConfig() != null && config.getConfig().containsKey(getParentProjectId())) {
            // 1.8 前后 混合数据
            this.setProjectId(getParentProjectId());
        } else {
            // 1.8 之前 数据
            if (config.getConfig() != null) {
                if (!config.getConfig().containsKey(RunModeConstants.HIS_PRO_ID.toString())) {
                    // 测试计划执行
                    Iterator<String> it = config.getConfig().keySet().iterator();
                    if (it.hasNext()) {
                        this.setProjectId(it.next());
                    }
                } else {
                    this.setProjectId(RunModeConstants.HIS_PRO_ID.toString());
                }
            }
        }
    }

    private boolean isUrl() {
        // 自定义字段没有引用环境则非url
        if (this.isCustomizeReq()) {
            if (this.isRefEnvironment) {
                return false;
            }
            return true;
        }
        if (StringUtils.isNotEmpty(this.getUrl()) && isURL(this.getUrl())) {
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

    private String getParentProjectId() {
        MsTestElement parent = this.getParent();
        while (parent != null) {
            if (StringUtils.isNotBlank(parent.getProjectId())) {
                return parent.getProjectId();
            }
            parent = parent.getParent();
        }
        return "";
    }

    private String getRestParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("/");
        Map<String, String> keyValueMap = new HashMap<>();
        this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue ->
                keyValueMap.put(keyValue.getName(), keyValue.getValue() != null && keyValue.getValue().startsWith("@") ?
                        ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue())
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
                        ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue());
            }
            stringBuffer.append("&");
        });
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private Arguments httpArguments(List<KeyValue> list) {
        Arguments arguments = new Arguments();
        list.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue -> {
            HTTPArgument httpArgument = new HTTPArgument(keyValue.getName(), StringUtils.isNotEmpty(keyValue.getValue()) && keyValue.getValue().startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue());
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
        headerManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "HeaderManager" : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        //  header 也支持 mock 参数
        headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                headerManager.add(new Header(keyValue.getName(), ScriptEngineUtils.buildFunctionCallString(keyValue.getValue())))
        );
        if (headerManager.getHeaders().size() > 0) {
            tree.add(headerManager);
        }
    }

    /**
     * 按照环境规则匹配环境
     *
     * @param httpConfig
     * @return
     */
    private HttpConfig getHttpConfig(HttpConfig httpConfig) {
        boolean isNext = true;
        if (CollectionUtils.isNotEmpty(httpConfig.getConditions())) {
            for (HttpConfigCondition item : httpConfig.getConditions()) {
                if (item.getType().equals(ConditionType.PATH.name())) {
                    HttpConfig config = httpConfig.getPathCondition(this.getPath(), item);
                    if (config != null) {
                        isNext = false;
                        httpConfig = config;
                        break;
                    }
                } else if (item.getType().equals(ConditionType.MODULE.name())) {
                    ApiDefinition apiDefinition = null;
                    ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                    ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                    if (StringUtils.isNotEmpty(this.getRefType()) && this.getRefType().equals("CASE")) {
                        ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(this.getId());
                        if (caseWithBLOBs != null) {
                            apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
                        }
                    } else {
                        apiDefinition = apiDefinitionService.get(this.getId());
                        if (apiDefinition == null) {
                            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.get(this.getId());
                            if (apiTestCaseWithBLOBs != null) {
                                apiDefinition = apiDefinitionService.get(apiTestCaseWithBLOBs.getApiDefinitionId());
                            } else {
                                TestPlanApiCaseService testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
                                TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(this.getId());
                                if (testPlanApiCase != null) {
                                    ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
                                    if (caseWithBLOBs != null) {
                                        apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
                                    }
                                }
                            }
                        }
                    }
                    if (apiDefinition != null) {
                        HttpConfig config = httpConfig.getModuleCondition(apiDefinition.getModuleId(), item);
                        if (config != null) {
                            isNext = false;
                            httpConfig = config;
                            break;
                        }
                    }
                }
            }
            if (isNext) {
                for (HttpConfigCondition item : httpConfig.getConditions()) {
                    if (item.getType().equals(ConditionType.NONE.name())) {
                        httpConfig = httpConfig.initHttpConfig(item);
                        break;
                    }
                }
            }
        }
        return httpConfig;
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
        arguments.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "-KeyStoreAlias" : "KeyStoreAlias");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        arguments.addArgument(key, value, "=");
        tree.add(arguments);
    }

    private boolean isRest() {
        return this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).toArray().length > 0;
    }

    public static List<MsHTTPSamplerProxy> findHttpSampleFromHashTree(MsTestElement hashTree) {
        return findFromHashTreeByType(hashTree, MsHTTPSamplerProxy.class, null);
    }
}

