package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSONObject;
import io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample;
import io.github.ningyu.jmeter.plugin.dubbo.sample.MethodArgument;
import io.github.ningyu.jmeter.plugin.util.Constants;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.dto.scenario.assertions.*;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.Host;
import io.metersphere.api.dto.scenario.extract.*;
import io.metersphere.api.dto.scenario.request.*;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.jmeter.assertions.DurationAssertion;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.assertions.JSR223Assertion;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Component
public class JMXGenerator {

    @Resource
    private ApiTestEnvironmentService environmentService;

    public HashTree parse(String testId, String testName, List<Scenario> scenarios) {
        HashTree jmeterTestPlanHashTree = new ListedHashTree();
        final HashTree testPlanHashTree = jmeterTestPlanHashTree.add(testPlan(testName));

        scenarios.stream().filter(Scenario::isEnable).forEach(scenario -> {
            final HashTree threadGroupHashTree = testPlanHashTree.add(threadGroup(scenario.getName()));

            EnvironmentConfig config = getEnvironmentConfig(scenario.getEnvironmentId());

            // 场景变量
            if (CollectionUtils.isNotEmpty(scenario.getVariables())) {
                threadGroupHashTree.add(arguments(scenario.getName() + " Variables", scenario.getVariables()));
            }
            // 场景请求头
            if (CollectionUtils.isNotEmpty(scenario.getHeaders())) {
                threadGroupHashTree.add(headerManager(scenario.getName() + " Headers", scenario.getHeaders()));
            }
            // 共享Cookie
            if (scenario.isEnableCookieShare()) {
                threadGroupHashTree.add(cookieManager(scenario));
            }
            // 场景JDBCDataSource
            final Map<String, String> databaseConfigMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(scenario.getDatabaseConfigs())) {
                scenario.getDatabaseConfigs().forEach(databaseConfig -> {
                    threadGroupHashTree.add(jdbcDataSource(databaseConfig));
                    databaseConfigMap.put(databaseConfig.getId(), databaseConfig.getName());
                });
            }
            // 场景TCP Config
            threadGroupHashTree.add(tcpConfig(scenario.getName() + "TCP Config", scenario.getTcpConfig()));

            // 请求
            scenario.getRequests().stream().filter(Request::isEnable).forEach(request -> {
                final HashTree samplerHashTree = new ListedHashTree();
                Object sampler;
                switch (request.getType()) {
                    case RequestType.TCP:
                        sampler = tcpSampler((TCPRequest) request);
                        // 引用环境的TCP Config
                        if (request.isUseEnvironment() && config != null) {
                            samplerHashTree.add(tcpConfig(request.getName() + "TCP Config", config.getTcpConfig()));
                        }
                        break;
                    case RequestType.DUBBO:
                        sampler = dubboSample((DubboRequest) request);
                        break;
                    case RequestType.SQL:
                        SqlRequest sqlRequest = (SqlRequest) request;
                        // 引用环境的JDBCDataSource
                        if (request.isUseEnvironment() && config != null) {
                            config.getDatabaseConfigs().forEach(databaseConfig -> {
                                if (!databaseConfigMap.containsValue(databaseConfig.getName())) {
                                    samplerHashTree.add(jdbcDataSource(databaseConfig));
                                    databaseConfigMap.put(databaseConfig.getId(), databaseConfig.getName());
                                }
                            });
                        }
                        samplerHashTree.add(arguments(sqlRequest.getName() + " Variables", sqlRequest.getVariables()));
                        sampler = jdbcSampler(sqlRequest, databaseConfigMap);
                        break;
                    default:
                        HttpRequest httpRequest = (HttpRequest) request;
                        sampler = httpSamplerProxy(httpRequest, config, testId);
                        // 请求头(包括引用环境里设置的请求头)
                        List<KeyValue> headers = httpRequest.getHeaders();
                        if (request.isUseEnvironment() && config != null) {
                            headers = merge(headers, config.getHttpConfig().getHeaders());
                        }
                        // 根据请求内容给请求头添加Content-Type
                        addContentType(headers, httpRequest.getBody());
                        samplerHashTree.add(headerManager(request.getName() + " Headers", headers));
                        break;
                }

                if (request.getController() != null && request.getController().isEnable() && request.getController().isValid()) {
                    threadGroupHashTree.add(ifController(request)).set(sampler, samplerHashTree);
                } else {
                    threadGroupHashTree.set(sampler, samplerHashTree);
                }

                if (request.isUseEnvironment() && config != null && config.getCommonConfig() != null) {
                    addEnvironmentVariables(samplerHashTree, request, config);
                    addEnvironmentDNS(samplerHashTree, request, config);
                }

                addRequestAssertions(samplerHashTree, request);
                addRequestExtractors(samplerHashTree, request);
                addJSR223Processors(samplerHashTree, request);

                if (request.getTimer() != null && request.getTimer().isEnable()) {
                    if (StringUtils.isNotBlank(request.getTimer().getDelay())) {
                        samplerHashTree.add(constantTimer(request));
                    }
                }
            });
        });
        return jmeterTestPlanHashTree;
    }

    private void addContentType(List<KeyValue> headers, Body body) {
        if (!body.isKV() && StringUtils.isNotBlank(body.getFormat())) {
            Map<String, String> map = new HashMap<>();
            map.put("json", "application/json");
            map.put("html", "text/html");
            map.put("xml", "text/xml");
            String contentType = map.get(body.getFormat());
            boolean hasContentType = headers.stream().filter(KeyValue::isEnable).anyMatch(keyValue -> keyValue.getName().equals(HTTP.CONTENT_TYPE));
            if (contentType != null && !hasContentType) {
                headers.add(new KeyValue(HTTP.CONTENT_TYPE, contentType));
            }
        }
    }

    private void addEnvironmentVariables(HashTree samplerHashTree, Request request, EnvironmentConfig config) {
        String name = request.getName() + "Environment Variables";
        samplerHashTree.add(arguments(name, config.getCommonConfig().getVariables()));
    }

    private void addEnvironmentDNS(HashTree samplerHashTree, Request request, EnvironmentConfig config) {
        if (config.getCommonConfig().isEnableHost() && CollectionUtils.isNotEmpty(config.getCommonConfig().getHosts())) {
            String domain = config.getHttpConfig().getDomain().trim();
            List<Host> hosts = new ArrayList<>();
            config.getCommonConfig().getHosts().forEach(host -> {
                if (StringUtils.isNotBlank(host.getDomain())) {
                    String hostDomain = host.getDomain().trim().replace("http://", "").replace("https://", "");
                    if (StringUtils.equals(hostDomain, domain)) {
                        host.setDomain(hostDomain); // 域名去掉协议
                        hosts.add(host);
                    }
                }
            });
            samplerHashTree.add(dnsCacheManager(request.getName() + " DNSCacheManager", hosts));
        }
    }

    private void addRequestAssertions(HashTree samplerHashTree, Request request) {
        Assertions assertions = request.getAssertions();
        if (CollectionUtils.isNotEmpty(assertions.getRegex())) {
            assertions.getRegex().stream().filter(AssertionRegex::isValid).forEach(assertion ->
                    samplerHashTree.add(responseAssertion(assertion))
            );
        }

        if (CollectionUtils.isNotEmpty(assertions.getJsonPath())) {
            assertions.getJsonPath().stream().filter(AssertionJsonPath::isValid).forEach(assertion ->
                    samplerHashTree.add(jsonPathAssertion(assertion))
            );
        }

        if (CollectionUtils.isNotEmpty(assertions.getJsr223())) {
            assertions.getJsr223().stream().filter(AssertionJSR223::isValid).forEach(assertion ->
                    samplerHashTree.add(jsr223Assertion(assertion))
            );
        }

        if (assertions.getDuration().isValid()) {
            samplerHashTree.add(durationAssertion(assertions.getDuration()));
        }
    }

    private void addRequestExtractors(HashTree samplerHashTree, Request request) {
        Extract extract = request.getExtract();
        if (CollectionUtils.isNotEmpty(extract.getRegex())) {
            extract.getRegex().stream().filter(ExtractCommon::isValid).forEach(extractRegex ->
                    samplerHashTree.add(regexExtractor(extractRegex))
            );
        }
        if (CollectionUtils.isNotEmpty(extract.getXpath())) {
            extract.getXpath().stream().filter(ExtractCommon::isValid).forEach(extractXPath ->
                    samplerHashTree.add(xPath2Extractor(extractXPath))
            );
        }
        if (CollectionUtils.isNotEmpty(extract.getJson())) {
            extract.getJson().stream().filter(ExtractCommon::isValid).forEach(extractJSONPath ->
                    samplerHashTree.add(jsonPostProcessor(extractJSONPath))
            );
        }
    }

    private void addJSR223Processors(HashTree samplerHashTree, Request request) {
        if (request.getJsr223PreProcessor() != null) {
            if (StringUtils.isNotBlank(request.getJsr223PreProcessor().getScript())) {
                samplerHashTree.add(jsr223PreProcessor(request));
            }
        }
        if (request.getJsr223PostProcessor() != null) {
            if (StringUtils.isNotBlank(request.getJsr223PostProcessor().getScript())) {
                samplerHashTree.add(jsr223PostProcessor(request));
            }
        }
    }

    private EnvironmentConfig getEnvironmentConfig(String id) {
        if (StringUtils.isBlank(id)) return null;
        ApiTestEnvironmentWithBLOBs environment = environmentService.get(id);
        if (environment != null) {
            return JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
        }
        return null;
    }

    private TestPlan testPlan(String testName) {
        TestPlan testPlan = new TestPlan(testName);
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestPlanGui"));
        testPlan.setEnabled(true);
        testPlan.setFunctionalMode(false);
        testPlan.setSerialized(true);
        testPlan.setTearDownOnShutdown(true);
        testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

    private ThreadGroup threadGroup(String name) {
        LoopController loopController = new LoopController();
        loopController.setName("LoopController");
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setEnabled(true);
        loopController.setLoops(1);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setEnabled(true);
        threadGroup.setName(name);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ThreadGroupGui"));
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setDelay(0);
        threadGroup.setDuration(0);
        threadGroup.setProperty(ThreadGroup.ON_SAMPLE_ERROR, ThreadGroup.ON_SAMPLE_ERROR_CONTINUE);
        threadGroup.setScheduler(false);
        threadGroup.setSamplerController(loopController);

        return threadGroup;
    }

    private Arguments arguments(String name, List<KeyValue> variables) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(name);
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        variables.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
        );
        return arguments;
    }

    private HeaderManager headerManager(String name, List<KeyValue> headers) {
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(name);
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                headerManager.add(new Header(keyValue.getName(), keyValue.getValue()))
        );
        return headerManager;
    }

    private CookieManager cookieManager(Scenario scenario) {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setEnabled(true);
        cookieManager.setName(scenario.getName() + " Cookie");
        cookieManager.setProperty(TestElement.TEST_CLASS, CookieManager.class.getName());
        cookieManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("CookiePanel"));
        cookieManager.setClearEachIteration(false);
        cookieManager.setControlledByThread(false);
        return cookieManager;
    }

    private DataSourceElement jdbcDataSource(DatabaseConfig databaseConfig) {
        DataSourceElement dataSourceElement = new DataSourceElement();
        dataSourceElement.setEnabled(true);
        dataSourceElement.setName(databaseConfig.getName() + " JDBCDataSource");
        dataSourceElement.setProperty(TestElement.TEST_CLASS, DataSourceElement.class.getName());
        dataSourceElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        dataSourceElement.setAutocommit(true);
        dataSourceElement.setKeepAlive(true);
        dataSourceElement.setPreinit(true);
        dataSourceElement.setDataSource(databaseConfig.getName());
        dataSourceElement.setDbUrl(databaseConfig.getDbUrl());
        dataSourceElement.setDriver(databaseConfig.getDriver());
        dataSourceElement.setUsername(databaseConfig.getUsername());
        dataSourceElement.setPassword(databaseConfig.getPassword());
        dataSourceElement.setPoolMax(String.valueOf(databaseConfig.getPoolMax()));
        dataSourceElement.setTimeout(String.valueOf(databaseConfig.getTimeout()));
        dataSourceElement.setConnectionAge("5000");
        dataSourceElement.setTrimInterval("60000");
        dataSourceElement.setTransactionIsolation("DEFAULT");
        return dataSourceElement;
    }

    private ConfigTestElement tcpConfig(String name, TCPConfig tcpConfig) {
        ConfigTestElement configTestElement = new ConfigTestElement();
        configTestElement.setEnabled(true);
        configTestElement.setName(name);
        configTestElement.setProperty(TestElement.TEST_CLASS, ConfigTestElement.class.getName());
        configTestElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPConfigGui"));
        configTestElement.setProperty(TCPSampler.CLASSNAME, tcpConfig.getClassname());
        configTestElement.setProperty(TCPSampler.SERVER, tcpConfig.getServer());
        configTestElement.setProperty(TCPSampler.PORT, tcpConfig.getPort());
        configTestElement.setProperty(TCPSampler.TIMEOUT_CONNECT, tcpConfig.getCtimeout());
        configTestElement.setProperty(TCPSampler.RE_USE_CONNECTION, tcpConfig.isReUseConnection());
        configTestElement.setProperty(TCPSampler.NODELAY, tcpConfig.isNodelay());
        configTestElement.setProperty(TCPSampler.CLOSE_CONNECTION, tcpConfig.isCloseConnection());
        configTestElement.setProperty(TCPSampler.SO_LINGER, tcpConfig.getSoLinger());
        configTestElement.setProperty(TCPSampler.EOL_BYTE, tcpConfig.getEolByte());
        configTestElement.setProperty(TCPSampler.SO_LINGER, tcpConfig.getSoLinger());
        configTestElement.setProperty(ConfigTestElement.USERNAME, tcpConfig.getUsername());
        configTestElement.setProperty(ConfigTestElement.PASSWORD, tcpConfig.getPassword());
        return configTestElement;
    }

    private HTTPSamplerProxy httpSamplerProxy(HttpRequest request, EnvironmentConfig config, String testId) {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setEnabled(true);
        sampler.setName(request.getName());
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HttpTestSampleGui"));
        sampler.setMethod(request.getMethod());
        sampler.setContentEncoding("UTF-8");
        sampler.setConnectTimeout(request.getConnectTimeout());
        sampler.setResponseTimeout(request.getResponseTimeout());
        sampler.setFollowRedirects(request.isFollowRedirects());
        sampler.setUseKeepAlive(true);
        sampler.setDoMultipart(request.isDoMultipartPost());

        try {
            if (request.isUseEnvironment()) {
                sampler.setDomain(config.getHttpConfig().getDomain());
                sampler.setPort(config.getHttpConfig().getPort());
                sampler.setProtocol(config.getHttpConfig().getProtocol());
                String url = config.getHttpConfig().getProtocol() + "://" + config.getHttpConfig().getSocket();
                URL urlObject = new URL(url);
                sampler.setDomain(config.getHttpConfig().getDomain());
                String envPath = StringUtils.equals(urlObject.getPath(), "/") ? "" : urlObject.getPath();
                if (StringUtils.isNotBlank(request.getPath())) {
                    envPath += request.getPath();
                }
                sampler.setPath(getPostQueryParameters(request, URLDecoder.decode(envPath, "UTF-8")));
            } else {
                String url = request.getUrl();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                URL urlObject = new URL(url);
                sampler.setDomain(URLDecoder.decode(urlObject.getHost(), "UTF-8"));
                sampler.setPort(urlObject.getPort());
                sampler.setProtocol(urlObject.getProtocol());
                sampler.setPath(getPostQueryParameters(request, URLDecoder.decode(urlObject.getPath(), "UTF-8")));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }

        // 请求参数
        if (CollectionUtils.isNotEmpty(request.getParameters())) {
            sampler.setArguments(httpArguments(request.getParameters()));
        }
        // 请求体
        if (!StringUtils.equals(request.getMethod(), "GET")) {
            List<KeyValue> body = new ArrayList<>();
            if (request.getBody().isKV()) {
                body = request.getBody().getKvs().stream().filter(KeyValue::isValid).collect(Collectors.toList());
                sampler.setHTTPFiles(httpFileArgs(request, testId));
            } else {
                if (StringUtils.isNotBlank(request.getBody().getRaw())) {
                    sampler.setPostBodyRaw(true);
                    KeyValue keyValue = new KeyValue("", request.getBody().getRaw());
                    keyValue.setEnable(true);
                    keyValue.setEncode(false);
                    body.add(keyValue);
                }
            }
            sampler.setArguments(httpArguments(body));
        }
        return sampler;
    }

    private String getPostQueryParameters(HttpRequest request, String path) {
        if (!StringUtils.equals(request.getMethod(), "GET")) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(path);
            stringBuffer.append("?");
            request.getParameters().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue ->
                    stringBuffer.append(keyValue.getName()).append("=").append(keyValue.getValue()).append("&")
            );
            return stringBuffer.substring(0, stringBuffer.length() - 1);
        }
        return "";
    }

    private Arguments httpArguments(List<KeyValue> list) {
        Arguments arguments = new Arguments();
        list.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue -> {
                    HTTPArgument httpArgument = new HTTPArgument(keyValue.getName(), keyValue.getValue());
                    httpArgument.setAlwaysEncoded(keyValue.isEncode());
//                    httpArgument.setUseEquals(true);
                    if (StringUtils.isNotBlank(keyValue.getContentType())) {
                        httpArgument.setContentType(keyValue.getContentType());
                    }
                    arguments.addArgument(httpArgument);
                }
        );
        return arguments;
    }

    private HTTPFileArg[] httpFileArgs(HttpRequest request, String testId) {
        final String BODY_FILE_DIR = "/opt/metersphere/data/body";
        List<HTTPFileArg> list = new ArrayList<>();
        request.getBody().getKvs().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
            if (keyValue.getFiles() != null) {
                keyValue.getFiles().forEach(file -> {
                    String paramName = keyValue.getName();
                    String path = BODY_FILE_DIR + '/' + testId + '/' + file.getId() + '_' + file.getName();
                    String mimetype = keyValue.getContentType();
                    list.add(new HTTPFileArg(path, paramName, mimetype));
                });
            }
        });
        return list.toArray(new HTTPFileArg[0]);
    }

    private List<KeyValue> merge(List<KeyValue> list1, List<KeyValue> list2) {
        Set<String> names = list1.stream().map(KeyValue::getName).collect(Collectors.toSet());
        List<KeyValue> list = new ArrayList<>(list1);
        list2.stream().filter(keyValue -> !names.contains(keyValue.getName())).forEach(list::add);
        return list;
    }

    private TCPSampler tcpSampler(TCPRequest request) {
        TCPSampler tcpSampler = new TCPSampler();
        tcpSampler.setName(request.getName());
        tcpSampler.setProperty(TestElement.TEST_CLASS, TCPSampler.class.getName());
        tcpSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPSamplerGui"));
        tcpSampler.setClassname(request.getClassname());
        tcpSampler.setServer(request.getServer());
        tcpSampler.setPort(request.getPort());
        tcpSampler.setConnectTimeout(request.getCtimeout());
        tcpSampler.setProperty(TCPSampler.RE_USE_CONNECTION, request.isReUseConnection());
        tcpSampler.setProperty(TCPSampler.NODELAY, request.isNodelay());
        tcpSampler.setCloseConnection(String.valueOf(request.isCloseConnection()));
        tcpSampler.setSoLinger(request.getSoLinger());
        tcpSampler.setEolByte(request.getEolByte());
        tcpSampler.setRequestData(request.getRequest());
        tcpSampler.setProperty(ConfigTestElement.USERNAME, request.getUsername());
        tcpSampler.setProperty(ConfigTestElement.PASSWORD, request.getPassword());

        return tcpSampler;
    }

    private JDBCSampler jdbcSampler(SqlRequest request, Map<String, String> databaseConfigMap) {
        JDBCSampler sampler = new JDBCSampler();
        sampler.setName(request.getName());
        sampler.setProperty(TestElement.TEST_CLASS, JDBCSampler.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        // request.getDataSource() 是ID，需要转换为Name
        sampler.setDataSource(databaseConfigMap.get(request.getDataSource()));
        sampler.setQuery(request.getQuery());
        sampler.setQueryTimeout(String.valueOf(request.getQueryTimeout()));
        sampler.setResultVariable(request.getResultVariable());
        sampler.setVariableNames(request.getVariableNames());
        sampler.setResultSetHandler("Store as String");
        sampler.setQueryType("Callable Statement");
        return sampler;
    }

    private DubboSample dubboSample(DubboRequest request) {
        DubboSample sampler = new DubboSample();
        sampler.setName(request.getName());
        sampler.setProperty(TestElement.TEST_CLASS, DubboSample.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DubboSampleGui"));

        Constants.setConfigCenterProtocol(request.getConfigCenter().getProtocol(), sampler);
        Constants.setConfigCenterGroup(request.getConfigCenter().getGroup(), sampler);
        Constants.setConfigCenterNamespace(request.getConfigCenter().getNamespace(), sampler);
        Constants.setConfigCenterUserName(request.getConfigCenter().getUsername(), sampler);
        Constants.setConfigCenterPassword(request.getConfigCenter().getPassword(), sampler);
        Constants.setConfigCenterAddress(request.getConfigCenter().getAddress(), sampler);
        Constants.setConfigCenterTimeout(request.getConfigCenter().getTimeout(), sampler);

        Constants.setRegistryProtocol(request.getRegistryCenter().getProtocol(), sampler);
        Constants.setRegistryGroup(request.getRegistryCenter().getGroup(), sampler);
        Constants.setRegistryUserName(request.getRegistryCenter().getUsername(), sampler);
        Constants.setRegistryPassword(request.getRegistryCenter().getPassword(), sampler);
        Constants.setRegistryTimeout(request.getRegistryCenter().getTimeout(), sampler);
        Constants.setAddress(request.getRegistryCenter().getAddress(), sampler);

        Constants.setTimeout(request.getConsumerAndService().getTimeout(), sampler);
        Constants.setVersion(request.getConsumerAndService().getVersion(), sampler);
        Constants.setGroup(request.getConsumerAndService().getGroup(), sampler);
        Constants.setConnections(request.getConsumerAndService().getConnections(), sampler);
        Constants.setLoadbalance(request.getConsumerAndService().getLoadBalance(), sampler);
        Constants.setAsync(request.getConsumerAndService().getAsync(), sampler);
        Constants.setCluster(request.getConsumerAndService().getCluster(), sampler);

        Constants.setRpcProtocol(request.getProtocol(), sampler);
        Constants.setInterfaceName(request.get_interface(), sampler);
        Constants.setMethod(request.getMethod(), sampler);

        List<MethodArgument> methodArgs = request.getArgs().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable)
                .map(keyValue -> new MethodArgument(keyValue.getName(), keyValue.getValue())).collect(Collectors.toList());
        Constants.setMethodArgs(methodArgs, sampler);

        List<MethodArgument> attachmentArgs = request.getAttachmentArgs().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable)
                .map(keyValue -> new MethodArgument(keyValue.getName(), keyValue.getValue())).collect(Collectors.toList());
        Constants.setAttachmentArgs(attachmentArgs, sampler);

        return sampler;
    }

    private DNSCacheManager dnsCacheManager(String name, List<Host> hosts) {
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.setEnabled(true);
        dnsCacheManager.setName(name);
        dnsCacheManager.setProperty(TestElement.TEST_CLASS, DNSCacheManager.class.getName());
        dnsCacheManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DNSCachePanel"));
        dnsCacheManager.setCustomResolver(true);
        hosts.forEach(host -> dnsCacheManager.addHost(host.getDomain(), host.getIp()));

        return dnsCacheManager;
    }

    private ResponseAssertion responseAssertion(AssertionRegex assertionRegex) {
        ResponseAssertion assertion = new ResponseAssertion();
        assertion.setEnabled(true);
        assertion.setName(assertionRegex.getDescription());
        assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("AssertionGui"));
        assertion.setAssumeSuccess(assertionRegex.isAssumeSuccess());
        assertion.setToContainsType();
        switch (assertionRegex.getSubject()) {
            case "Response Code":
                assertion.setTestFieldResponseCode();
                break;
            case "Response Headers":
                assertion.setTestFieldResponseHeaders();
                break;
            case "Response Data":
                assertion.setTestFieldResponseData();
                break;
        }
        return assertion;
    }

    private JSONPathAssertion jsonPathAssertion(AssertionJsonPath assertionJsonPath) {
        JSONPathAssertion assertion = new JSONPathAssertion();
        assertion.setEnabled(true);
        assertion.setName(assertionJsonPath.getDescription());
        assertion.setProperty(TestElement.TEST_CLASS, JSONPathAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("JSONPathAssertionGui"));
        assertion.setJsonPath(assertionJsonPath.getExpression());
        assertion.setExpectedValue(assertionJsonPath.getExpect());
        assertion.setJsonValidationBool(true);
        assertion.setExpectNull(false);
        assertion.setInvert(false);
        assertion.setIsRegex(true);
        return assertion;
    }

    private DurationAssertion durationAssertion(AssertionDuration assertionDuration) {
        DurationAssertion assertion = new DurationAssertion();
        assertion.setEnabled(true);
        assertion.setName("Response In Time: " + assertionDuration.getValue());
        assertion.setProperty(TestElement.TEST_CLASS, DurationAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DurationAssertionGui"));
        assertion.setAllowedDuration(assertionDuration.getValue());
        return assertion;
    }

    private JSR223Assertion jsr223Assertion(AssertionJSR223 assertionJSR223) {
        JSR223Assertion assertion = new JSR223Assertion();
        assertion.setEnabled(true);
        assertion.setName(assertionJSR223.getDesc());
        assertion.setProperty(TestElement.TEST_CLASS, JSR223Assertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        assertion.setProperty("cacheKey", "true");
        assertion.setProperty("scriptLanguage", assertionJSR223.getLanguage());
        assertion.setProperty("script", assertionJSR223.getScript());
        return assertion;
    }

    private RegexExtractor regexExtractor(ExtractRegex extractRegex) {
        RegexExtractor extractor = new RegexExtractor();
        extractor.setEnabled(true);
        extractor.setName(extractRegex.getVariable() + " RegexExtractor");
        extractor.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("RegexExtractorGui"));
        extractor.setRefName(extractRegex.getVariable());
        extractor.setRegex(extractRegex.getExpression());
        extractor.setUseField(extractRegex.getUseHeaders());
        if (extractRegex.isMultipleMatching()) {
            extractor.setMatchNumber(-1);
        }
        extractor.setTemplate("$1$");
        return extractor;
    }

    private JSONPostProcessor jsonPostProcessor(ExtractJSONPath extractJSONPath) {
        JSONPostProcessor extractor = new JSONPostProcessor();
        extractor.setEnabled(true);
        extractor.setName(extractJSONPath.getVariable() + " JSONExtractor");
        extractor.setProperty(TestElement.TEST_CLASS, JSONPostProcessor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("JSONPostProcessorGui"));
        extractor.setRefNames(extractJSONPath.getVariable());
        extractor.setJsonPathExpressions(extractJSONPath.getExpression());
        if (extractJSONPath.isMultipleMatching()) {
            extractor.setMatchNumbers("-1");
        }
        return extractor;
    }

    private XPath2Extractor xPath2Extractor(ExtractXPath extractXPath) {
        XPath2Extractor extractor = new XPath2Extractor();
        extractor.setEnabled(true);
        extractor.setName(extractXPath.getVariable() + " XPath2Extractor");
        extractor.setProperty(TestElement.TEST_CLASS, XPath2Extractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("XPath2ExtractorGui"));
        extractor.setRefName(extractXPath.getVariable());
        extractor.setXPathQuery(extractXPath.getExpression());
        if (extractXPath.isMultipleMatching()) {
            extractor.setMatchNumber(-1);
        }
        return extractor;
    }

    private JSR223PreProcessor jsr223PreProcessor(Request request) {
        JSR223PreProcessor processor = new JSR223PreProcessor();
        processor.setEnabled(true);
        processor.setName(request.getName());
        processor.setProperty(TestElement.TEST_CLASS, JSR223PreProcessor.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        processor.setProperty("cacheKey", "true");
        processor.setProperty("scriptLanguage", request.getJsr223PreProcessor().getLanguage());
        processor.setProperty("script", request.getJsr223PreProcessor().getScript());
        return processor;
    }

    private JSR223PostProcessor jsr223PostProcessor(Request request) {
        JSR223PostProcessor processor = new JSR223PostProcessor();
        processor.setEnabled(true);
        processor.setName(request.getName());
        processor.setProperty(TestElement.TEST_CLASS, JSR223PostProcessor.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        processor.setProperty("cacheKey", "true");
        processor.setProperty("scriptLanguage", request.getJsr223PostProcessor().getLanguage());
        processor.setProperty("script", request.getJsr223PostProcessor().getScript());
        return processor;
    }

    private ConstantTimer constantTimer(Request request) {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(true);
        constantTimer.setName(request.getTimer().getDelay() + " ms");
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ConstantTimerGui"));
        constantTimer.setDelay(request.getTimer().getDelay());
        return constantTimer;
    }

    private IfController ifController(Request request) {
        IfController ifController = new IfController();
        ifController.setEnabled(true);
        ifController.setName(request.getController().getLabel());
        ifController.setCondition(request.getController().getCondition());
        ifController.setProperty(TestElement.TEST_CLASS, IfController.class.getName());
        ifController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("IfControllerPanel"));
        ifController.setEvaluateAll(false);
        ifController.setUseExpression(true);
        return ifController;
    }
}
