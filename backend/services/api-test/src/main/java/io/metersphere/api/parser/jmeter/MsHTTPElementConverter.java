package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.request.http.MsHTTPConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.auth.BasicAuth;
import io.metersphere.api.dto.request.http.auth.DigestAuth;
import io.metersphere.api.dto.request.http.auth.HTTPAuthConfig;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.parser.jmeter.body.MsBodyConverter;
import io.metersphere.api.parser.jmeter.body.MsBodyConverterFactory;
import io.metersphere.api.parser.jmeter.body.MsFormDataBodyConverter;
import io.metersphere.api.parser.jmeter.body.MsWWWFormBodyConverter;
import io.metersphere.jmeter.mock.Mock;
import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.GlobalParams;
import io.metersphere.project.dto.environment.host.Host;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.http.HttpConfigPathMatchRule;
import io.metersphere.project.dto.environment.http.SelectModule;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.*;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.springframework.http.HttpMethod;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.*;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsHTTPElementConverter extends AbstractJmeterElementConverter<MsHTTPElement> {

    public static final String URL_ENCODE = "${__urlencode(%s)}";
    public static final String COOKIE = "Cookie";

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    @Override
    public void toHashTree(HashTree tree, MsHTTPElement msHTTPElement, ParameterConfig config) {
        if (BooleanUtils.isFalse(msHTTPElement.getEnable())) {
            LogUtils.info("MsHTTPElement is disabled");
            return;
        }

        ApiParamConfig apiParamConfig = (ApiParamConfig) config;
        HttpConfig httpConfig = getHttpConfig(msHTTPElement, apiParamConfig);
        EnvironmentInfoDTO envConfig = apiParamConfig.getEnvConfig(msHTTPElement.getProjectId());

        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setName(msHTTPElement.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(HTTP_TEST_SAMPLE_GUI));

        setStepIdentification(msHTTPElement, config, sampler);

        sampler.setMethod(msHTTPElement.getMethod());
        // path 设置完整的url
        sampler.setPath(getPath(msHTTPElement, httpConfig));

        setHttpOtherConfig(msHTTPElement.getOtherConfig(), sampler);

        // 处理请求体
        handleBody(sampler, msHTTPElement, config);

        HashTree httpTree = tree.add(sampler);

        // 处理环境变量
        Arguments envArguments = getEnvArguments(msHTTPElement, envConfig);
        Optional.ofNullable(envArguments).ifPresent(httpTree::add);

        // 处理请求头
        HeaderManager httpHeader = getHttpHeader(msHTTPElement, apiParamConfig, httpConfig);
        Optional.ofNullable(httpHeader).ifPresent(httpTree::add);
        //处理host
        DNSCacheManager dnsCacheManager = getEnvDns(msHTTPElement.getName(), envConfig, httpConfig);
        Optional.ofNullable(dnsCacheManager).ifPresent(httpTree::add);

        HTTPAuthConfig authConfig = msHTTPElement.getAuthConfig();

        // 处理认证信息
        AuthManager authManager = getAuthManager(authConfig);
        Optional.ofNullable(authManager).ifPresent(httpTree::add);

        parseChild(httpTree, msHTTPElement, config);
    }


    /**
     * 设置超时时间等配置
     *
     * @param msHTTPConfig
     * @param sampler
     */
    private void setHttpOtherConfig(MsHTTPConfig msHTTPConfig, HTTPSamplerProxy sampler) {
        sampler.setConnectTimeout(msHTTPConfig.getConnectTimeout().toString());
        sampler.setResponseTimeout(msHTTPConfig.getResponseTimeout().toString());
        sampler.setFollowRedirects(msHTTPConfig.getFollowRedirects());
        sampler.setAutoRedirects(msHTTPConfig.getAutoRedirects());
    }

    private static final Map<String, AuthManager.Mechanism> mechanismMap = HashMap.newHashMap(2);
    private static final Map<String, BiConsumer<Authorization, HTTPAuthConfig>> authHanlerMap = HashMap.newHashMap(2);

    static {
        mechanismMap.put(HTTPAuthConfig.HTTPAuthType.BASIC.name(), AuthManager.Mechanism.BASIC);
        mechanismMap.put(HTTPAuthConfig.HTTPAuthType.DIGEST.name(), AuthManager.Mechanism.DIGEST);
        authHanlerMap.put(HTTPAuthConfig.HTTPAuthType.BASIC.name(), (authorization, httpAuth) -> {
            BasicAuth basicAuth = httpAuth.getBasicAuth();
            authorization.setUser(basicAuth.getUserName());
            authorization.setPass(basicAuth.getPassword());
        });
        authHanlerMap.put(HTTPAuthConfig.HTTPAuthType.DIGEST.name(), (authorization, httpAuth) -> {
            DigestAuth digestAuth = httpAuth.getDigestAuth();
            authorization.setUser(digestAuth.getUserName());
            authorization.setPass(digestAuth.getPassword());
        });
    }

    /**
     * 获取认证配置
     *
     * @param authConfig
     * @return
     */
    private AuthManager getAuthManager(HTTPAuthConfig authConfig) {
        if (authConfig == null || !authConfig.isHTTPAuthValid()) {
            return null;
        }

        Authorization auth = new Authorization();
        auth.setURL(StringUtils.EMPTY);
        auth.setMechanism(mechanismMap.get(authConfig.getAuthType()));
        authHanlerMap.get(authConfig.getAuthType()).accept(auth, authConfig);

        AuthManager authManager = new AuthManager();
        authManager.setEnabled(true);
        authManager.setName("AuthManager");
        authManager.setProperty(TestElement.TEST_CLASS, AuthManager.class.getName());
        authManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(AUTH_PANEL));
        authManager.addAuth(auth);
        return authManager;
    }

    /**
     * 添加场景和环境变量
     *
     * @param msHTTPElement
     * @param envInfo
     */
    private Arguments getEnvArguments(MsHTTPElement msHTTPElement, EnvironmentInfoDTO envInfo) {
        if (envInfo == null) {
            return null;
        }

        List<CommonVariables> envVariables = envInfo.getConfig().getCommonVariables();
        if (CollectionUtils.isEmpty(envVariables)) {
            return null;
        }

        return JmeterTestElementParserHelper.getArguments(msHTTPElement.getName(), envVariables);
    }

    /**
     * 设置步骤标识
     * 当前步骤唯一标识，结果和步骤匹配的关键
     *
     * @param msHTTPElement
     * @param config
     * @param sampler
     */
    private void setStepIdentification(MsHTTPElement msHTTPElement, ParameterConfig config, HTTPSamplerProxy sampler) {
        sampler.setProperty(ElementProperty.MS_RESOURCE_ID.name(), msHTTPElement.getResourceId());
        sampler.setProperty(ElementProperty.MS_STEP_ID.name(), msHTTPElement.getStepId());
        sampler.setProperty(ElementProperty.MS_REPORT_ID.name(), config.getReportId());
        sampler.setProperty(ElementProperty.PROJECT_ID.name(), msHTTPElement.getProjectId());
    }

    private String getPath(MsHTTPElement msHTTPElement, HttpConfig httpConfig) {
        String url = msHTTPElement.getPath();
        if (httpConfig != null) {
            // 接口调试没有环境，不取环境的配置
            if (StringUtils.startsWithIgnoreCase(httpConfig.getHostname(), "http")) {
                url = httpConfig.getHostname() + ("/" + url).replace("//", "/");
            } else {
                String protocol = httpConfig.getProtocol().toLowerCase();
                url = protocol + "://" + (httpConfig.getHostname() + "/" + url).replace("//", "/");
            }

        }
        url = getPathWithQueryRest(msHTTPElement, url);
        return getPathWithQuery(url, msHTTPElement.getQuery());
    }

    /**
     * 替换 rest 参数
     *
     * @param msHTTPElement
     * @param path
     * @return
     */
    private String getPathWithQueryRest(MsHTTPElement msHTTPElement, String path) {
        List<RestParam> rest = msHTTPElement.getRest();
        if (CollectionUtils.isEmpty(rest)) {
            return path;
        }

        rest = rest.stream()
                .filter(RestParam::getEnable)
                .filter(RestParam::isValid)
                .filter(RestParam::isNotBlankValue)
                .toList();

        if (CollectionUtils.isEmpty(rest)) {
            return path;
        }

        Map<String, String> keyValueMap = new HashMap<>();
        for (RestParam restParam : rest) {
            try {
                String value = restParam.getValue();
                value = Mock.buildFunctionCallString(value);
                value = BooleanUtils.isTrue(restParam.getEncode()) ? String.format(URL_ENCODE, value.replace(",", "\\,")) : value;
                keyValueMap.put(restParam.getKey(), value);
            } catch (Exception e) {
                LogUtils.error(e);
            }
        }

        try {
            Pattern p = Pattern.compile("(\\{)([\\w]+)(\\})");
            Matcher m = p.matcher(path);
            while (m.find()) {
                String group = m.group(2);
                if (!isRestVariable(path, group) && keyValueMap.containsKey(group)) {
                    path = path.replace("{" + group + "}", keyValueMap.get(group));
                }
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return path;
    }

    private boolean isRestVariable(String path, String value) {
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

    private HeaderManager getHttpHeader(MsHTTPElement msHTTPElement, ApiParamConfig apiParamConfig, HttpConfig httpConfig) {
        Map<String, String> headerMap = new HashMap<>();

        // 获取全局参数中的请求头
        GlobalParams globalParams = apiParamConfig.getGlobalParams();
        if (globalParams != null) {
            setHeaderMap(headerMap, globalParams.getHeaders());
        }

        // 获取环境中的请求头
        if (httpConfig != null && CollectionUtils.isNotEmpty(httpConfig.getHeaders())) {
            Boolean enableGlobalCookie = apiParamConfig.getEnableGlobalCookie();
            List<KeyValueEnableParam> envHeaders = httpConfig.getHeaders();
            if (BooleanUtils.isFalse(enableGlobalCookie)) {
                // 如果不启用全局 cookie，则过滤 cookie
                envHeaders = envHeaders.stream()
                        .filter(header -> !StringUtils.equalsIgnoreCase(header.getKey(), COOKIE))
                        .toList();
            }
            setHeaderMap(headerMap, envHeaders);
        }

        // 获取请求中的请求头
        if (CollectionUtils.isNotEmpty(msHTTPElement.getHeaders())) {
            setHeaderMap(headerMap, msHTTPElement.getHeaders());
        }

        if (headerMap.isEmpty()) {
            return null;
        }

        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(StringUtils.isNotEmpty(msHTTPElement.getName()) ? msHTTPElement.getName() + "_HeaderManager" : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(HEADER_PANEL));
        headerMap.forEach((k, v) -> headerManager.add(new Header(k, Mock.buildFunctionCallString(v))));
        return headerManager;
    }


    private void setHeaderMap(Map<String, String> headerMap, List<? extends KeyValueEnableParam> headers) {
        if (CollectionUtils.isEmpty(headers)) {
            return;
        }
        headers.stream()
                .filter(KeyValueEnableParam::getEnable)
                .filter(KeyValueParam::isValid)
                .forEach(header -> {
                    if (StringUtils.equalsIgnoreCase(header.getKey(), COOKIE)) {
                        String cookieValue = header.getValue();
                        if (headerMap.get(COOKIE) != null && header.getValue() != null) {
                            // 合并 cookie
                            cookieValue = headerMap.get(COOKIE) + ";" + header.getValue();
                        }
                        headerMap.put(COOKIE, cookieValue);
                    } else {
                        headerMap.put(header.getKey(), header.getValue());
                    }
                });
    }

    /**
     * 获取环境 http 配置
     *
     * @param msHTTPElement
     * @param config
     * @return
     */
    private HttpConfig getHttpConfig(MsHTTPElement msHTTPElement, ApiParamConfig config) {
        ApiParamConfig apiParamConfig = config;
        EnvironmentInfoDTO envConfig = apiParamConfig.getEnvConfig();
        if (envConfig == null) {
            return null;
        }
        // http配置按优先级排序
        List<HttpConfig> httpConfigs = envConfig.getConfig().getHttpConfig()
                .stream()
                .sorted(Comparator.comparing(HttpConfig::getModuleMatchRuleOrder))
                .toList();
        for (HttpConfig httpConfig : httpConfigs) {
            boolean match;
            if (httpConfig.isPathMatchRule()) {
                // 匹配路径
                HttpConfigPathMatchRule pathMatchRule = httpConfig.getPathMatchRule();
                HttpConfigPathMatchRule.MatchRuleCondition matchRuleCondition =
                        EnumValidator.validateEnum(HttpConfigPathMatchRule.MatchRuleCondition.class, pathMatchRule.getCondition());
                match = matchRuleCondition.match(pathMatchRule.getPath(), msHTTPElement.getPath());
            } else if (httpConfig.isModuleMatchRule()) {
                // 匹配模块
                Set<String> moduleIds = httpConfig.getModuleMatchRule().getModules()
                        .stream()
                        .map(SelectModule::getModuleId)
                        .collect(Collectors.toSet());
                match = moduleIds.contains(msHTTPElement.getModuleId());
            } else {
                // 无条件匹配
                match = true;
            }
            if (match) {
                return httpConfig;
            }
        }
        return null;
    }

    /**
     * 解析body参数
     *
     * @param sampler
     * @param msHTTPElement
     */
    private void handleBody(HTTPSamplerProxy sampler, MsHTTPElement msHTTPElement, ParameterConfig config) {
        Body body = msHTTPElement.getBody();
        // 请求体处理
        if (body != null) {
            MsBodyConverter converter = MsBodyConverterFactory.getConverter(body.getBodyClassByType());

            // 这里get请求，不处理 form-date 和 www-form-urlencoded 类型的参数
            // 否则会被 jmeter 作为 query 参数
            if (StringUtils.equalsIgnoreCase(msHTTPElement.getMethod(), HttpMethod.GET.name())
                    && (converter instanceof MsWWWFormBodyConverter || converter instanceof MsFormDataBodyConverter)) {
                return;
            }
            converter.parse(sampler, body.getBodyDataByType(), config);
        }
    }

    /**
     * 将 query 参数添加到 url 上
     *
     * @param path
     * @param query
     * @return
     */
    private String getPathWithQuery(String path, List<QueryParam> query) {
        if (CollectionUtils.isEmpty(query)) {
            return path;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        if (path.contains("?")) {
            stringBuffer.append("&");
        } else {
            stringBuffer.append("?");
        }
        query.stream()
                .filter(KeyValueEnableParam::getEnable)
                .filter(KeyValueParam::isValid)
                .forEach(queryParam -> {
                    stringBuffer.append(queryParam.getEncode() ? String.format(URL_ENCODE, queryParam.getKey()) : queryParam.getKey());
                    if (queryParam.getValue() != null) {
                        try {
                            String value = Mock.buildFunctionCallString(queryParam.getValue());
                            value = queryParam.getEncode() ? String.format(URL_ENCODE, value.replace(",", "\\,")) : value;
                            if (StringUtils.isNotEmpty(value) && value.contains(StringUtils.CR)) {
                                value = value.replaceAll(StringUtils.CR, StringUtils.EMPTY);
                            }
                            stringBuffer.append("=").append(value);
                        } catch (Exception e) {
                            LogUtils.error(e);
                        }
                    }
                    stringBuffer.append("&");
                });
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private DNSCacheManager getEnvDns(String name , EnvironmentInfoDTO envConfig, HttpConfig httpConfig) {
        if (envConfig == null ||
                envConfig.getConfig() == null ||
                envConfig.getConfig().getHostConfig() == null ||
                BooleanUtils.isFalse(envConfig.getConfig().getHostConfig().getEnable()) ||
                httpConfig == null) {
            return null;
        }
        String domain = httpConfig.getHostname().trim();
        List<Host> hosts = new ArrayList<>();
        envConfig.getConfig().getHostConfig().getHosts().forEach(host -> {
            if (StringUtils.isNotBlank(host.getDomain())) {
                String hostDomain = host.getDomain().trim().replace(HTTP, StringUtils.EMPTY).replace(HTTPS, StringUtils.EMPTY);
                if (StringUtils.equals(hostDomain, domain)) {
                    host.setDomain(hostDomain); // 域名去掉协议
                    hosts.add(host);
                }
            }
        });
        if (CollectionUtils.isNotEmpty(hosts)) {
            return dnsCacheManager(name + "DNSCacheManager", hosts);
        }
        return null;
    }

    private static DNSCacheManager dnsCacheManager(String name, List<Host> hosts) {
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.setEnabled(true);
        dnsCacheManager.setName(name);
        dnsCacheManager.setProperty(TestElement.TEST_CLASS, DNSCacheManager.class.getName());
        dnsCacheManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DNSCachePanel"));
        dnsCacheManager.setCustomResolver(false);
        dnsCacheManager.setClearEachIteration(true);
        hosts.forEach(host -> dnsCacheManager.addHost(host.getDomain(), host.getIp()));

        return dnsCacheManager;
    }
}
