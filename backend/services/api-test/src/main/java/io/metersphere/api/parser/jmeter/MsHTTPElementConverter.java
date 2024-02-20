package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.QueryParam;
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
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.http.HttpConfigPathMatchRule;
import io.metersphere.project.dto.environment.http.SelectModule;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.springframework.http.HttpMethod;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.HEADER_PANEL;
import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.HTTP_TEST_SAMPLE_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsHTTPElementConverter extends AbstractJmeterElementConverter<MsHTTPElement> {

    public static final String URL_ENCODE = "${__urlencode(%s)}";
    public static final String COOKIE = "Cookie";

    @Override
    public void toHashTree(HashTree tree, MsHTTPElement msHTTPElement, ParameterConfig config) {
        if (BooleanUtils.isFalse(msHTTPElement.getEnable())) {
            LogUtils.info("MsHTTPElement is disabled");
            return;
        }

        ApiParamConfig apiParamConfig = (ApiParamConfig) config;
        HttpConfig httpConfig = getHttpConfig(msHTTPElement, apiParamConfig);

        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setName(msHTTPElement.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(HTTP_TEST_SAMPLE_GUI));

        setStepIdentification(msHTTPElement, config, sampler);

        sampler.setMethod(msHTTPElement.getMethod());
        // path 设置完整的url
        sampler.setPath(getPath(msHTTPElement, httpConfig));

        // 处理请求体
        handleBody(sampler, msHTTPElement, config);

        HashTree httpTree = tree.add(sampler);
        // 处理请求头
        HeaderManager httpHeader = getHttpHeader(msHTTPElement, apiParamConfig, httpConfig);
        if (httpHeader != null) {
            httpTree.add(httpHeader);
        }

        parseChild(httpTree, msHTTPElement, config);
    }

    /**
     * 设置步骤标识
     * 当前步骤唯一标识，结果和步骤匹配的关键
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
            String protocol = httpConfig.getProtocol().toLowerCase();
            url = protocol + "://" + (httpConfig.getUrl() + "/" + url).replace("//", "/");
        }
        return getPathWithQuery(url, msHTTPElement.getQuery());
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
                            String value = queryParam.getValue().startsWith("@") ? Mock.buildFunctionCallString(queryParam.getValue()) : queryParam.getValue();
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
}
