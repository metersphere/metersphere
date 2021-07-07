package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample;
import io.github.ningyu.jmeter.plugin.dubbo.sample.MethodArgument;
import io.github.ningyu.jmeter.plugin.util.Constants;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ImportPoolsDTO;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.assertions.*;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.controller.MsTransactionController;
import io.metersphere.api.dto.definition.request.controller.loop.CountController;
import io.metersphere.api.dto.definition.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.definition.request.controller.loop.MsWhileController;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
import io.metersphere.api.dto.definition.request.extract.MsExtractJSONPath;
import io.metersphere.api.dto.definition.request.extract.MsExtractRegex;
import io.metersphere.api.dto.definition.request.extract.MsExtractXPath;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsConfigCenter;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsConsumerAndService;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsRegistryCenter;
import io.metersphere.api.dto.definition.request.timer.MsConstantTimer;
import io.metersphere.api.dto.definition.request.unknown.MsJmeterElement;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.LoopConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.*;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MsJmeterParser extends ApiImportAbstractParser<ScenarioImport> {
    private final String ENV_NAME = "导入数据环境";
    /**
     * todo 存放单个请求下的Header 为了和平台对应
     */
    private Map<Integer, List<Object>> headerMap = new HashMap<>();

    @Override
    public ScenarioImport parse(InputStream inputSource, ApiTestImportRequest request) {
        try {
            Object scriptWrapper = SaveService.loadElement(inputSource);
            HashTree testPlan = this.getHashTree(scriptWrapper);
            // 优先初始化数据源及部分参数
            preInitPool(request.getProjectId(), testPlan);

            MsScenario scenario = new MsScenario();
            scenario.setReferenced("IMPORT");
            jmterHashTree(testPlan, scenario);
            this.projectId = request.getProjectId();
            ScenarioImport scenarioImport = new ScenarioImport();
            scenarioImport.setData(paseObj(scenario, request));
            scenarioImport.setProjectId(request.getProjectId());
            return scenarioImport;
        } catch (Exception e) {
            e.printStackTrace();
            MSException.throwException("当前JMX版本不兼容");
        }
        return null;
    }

    private List<ApiScenarioWithBLOBs> paseObj(MsScenario msScenario, ApiTestImportRequest request) {
        List<ApiScenarioWithBLOBs> scenarioWithBLOBsList = new ArrayList<>();
        ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        ApiScenarioModule selectModule = null;
        String selectModulePath = null;
        if (StringUtils.isNotBlank(request.getModuleId())) {
            selectModule = ApiScenarioImportUtil.getSelectModule(request.getModuleId());
            if (selectModule != null) {
                selectModulePath = ApiScenarioImportUtil.getSelectModulePath(selectModule.getName(), selectModule.getParentId());
            }
        }
        ApiScenarioModule module = ApiScenarioImportUtil.buildModule(selectModule, msScenario.getName(), this.projectId);
        scenarioWithBLOBs.setName(request.getFileName());
        scenarioWithBLOBs.setProjectId(request.getProjectId());
        if (msScenario != null && CollectionUtils.isNotEmpty(msScenario.getHashTree())) {
            scenarioWithBLOBs.setStepTotal(msScenario.getHashTree().size());
        }
        if (module != null) {
            scenarioWithBLOBs.setApiScenarioModuleId(module.getId());
            if (StringUtils.isNotBlank(selectModulePath)) {
                scenarioWithBLOBs.setModulePath(selectModulePath + "/" + module.getName());
            } else {
                scenarioWithBLOBs.setModulePath("/" + module.getName());
            }
        }
        scenarioWithBLOBs.setId(UUID.randomUUID().toString());
        scenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(msScenario));
        scenarioWithBLOBsList.add(scenarioWithBLOBs);
        return scenarioWithBLOBsList;
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    public boolean isProtocolDefaultPort(HTTPSamplerProxy source) {
        String portAsString = source.getPropertyAsString("HTTPSampler.port");
        if (portAsString != null && !portAsString.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public String url(String protocol, String host, String port, String file) {
        protocol = protocol.toLowerCase();
        if (StringUtils.isNotEmpty(file) && !file.startsWith("/")) {
            file += "/";
        }
        return protocol + "://" + host + ":" + port + file;
    }

    public String getUrl(HTTPSamplerProxy source) throws MalformedURLException {
        String path = source.getPath();
        // Request Defaults
        if (StringUtils.isEmpty(source.getDomain())) {
            return null;
        }
        if (!path.startsWith("http://") && !path.startsWith("https://")) {
            String domain = source.getDomain();
            String protocol = source.getProtocol();
            String method = source.getMethod();
            StringBuilder pathAndQuery = new StringBuilder(100);
            if ("file".equalsIgnoreCase(protocol)) {
                domain = null;
            } else if (!path.startsWith("/")) {
                pathAndQuery.append('/');
            }

            pathAndQuery.append(path);
            if ("GET".equals(method) || "DELETE".equals(method) || "OPTIONS".equals(method)) {
                String queryString = source.getQueryString(source.getContentEncoding());
                if (queryString.length() > 0) {
                    if (path.contains("?")) {
                        pathAndQuery.append("&");
                    } else {
                        pathAndQuery.append("?");
                    }

                    pathAndQuery.append(queryString);
                }
            }
            String portAsString = source.getPropertyAsString("HTTPSampler.port");
            return this.isProtocolDefaultPort(source) ? new URL(protocol, domain, pathAndQuery.toString()).toExternalForm() : this.url(protocol, domain, portAsString, pathAndQuery.toString());
        } else {
            return new URL(path).toExternalForm();
        }
    }

    private String getBodyType(List<KeyValue> headers) {
        if (CollectionUtils.isNotEmpty(headers)) {
            List<KeyValue> keyValues = headers.stream().filter(keyValue -> keyValue.getName().equals("Content-Type") && keyValue.getValue().equals("application/x-www-form-urlencoded")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(keyValues)) {
                return keyValues.get(0).getValue();
            }
        }
        return null;
    }

    private void convertHttpSampler(MsHTTPSamplerProxy samplerProxy, Object key) {
        try {
            HTTPSamplerProxy source = (HTTPSamplerProxy) key;
            BeanUtils.copyBean(samplerProxy, source);
            samplerProxy.setRest(new ArrayList<KeyValue>() {{
                this.add(new KeyValue());
            }});
            samplerProxy.setArguments(new ArrayList<KeyValue>() {{
                this.add(new KeyValue());
            }});
            // 处理HTTP协议的请求头
            if (headerMap.containsKey(key.hashCode())) {
                List<KeyValue> keyValues = new LinkedList<>();
                headerMap.get(key.hashCode()).forEach(item -> {
                    HeaderManager headerManager = (HeaderManager) item;
                    if (headerManager.getHeaders() != null) {
                        for (int i = 0; i < headerManager.getHeaders().size(); i++) {
                            keyValues.add(new KeyValue(headerManager.getHeader(i).getName(), headerManager.getHeader(i).getValue()));
                        }
                    }
                });
                samplerProxy.setHeaders(keyValues);
            }
            // 初始化body
            Body body = new Body();
            body.init();
            body.initKvs();
            body.getKvs().clear();
            body.initBinary();
            body.getBinary().clear();
            samplerProxy.setBody(body);
            if (source != null && source.getHTTPFiles().length > 0) {
                samplerProxy.getBody().initBinary();
                samplerProxy.getBody().setType(Body.FORM_DATA);
                List<KeyValue> keyValues = new LinkedList<>();
                for (HTTPFileArg arg : source.getHTTPFiles()) {
                    List<BodyFile> files = new LinkedList<>();
                    BodyFile file = new BodyFile();
                    file.setId(arg.getParamName());
                    String fileName = arg.getPath();
                    if (fileName.indexOf("/") != -1) {
                        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                    }
                    file.setName(fileName);
                    files.add(file);

                    KeyValue keyValue = new KeyValue(arg.getParamName(), arg.getParamName());
                    keyValue.setContentType(arg.getMimeType());
                    keyValue.setType("file");
                    keyValue.setFiles(files);
                    keyValues.add(keyValue);
                }
                samplerProxy.getBody().setKvs(keyValues);
            }
            samplerProxy.setProtocol(RequestType.HTTP);
            samplerProxy.setConnectTimeout(source.getConnectTimeout() + "");
            samplerProxy.setResponseTimeout(source.getResponseTimeout() + "");
            samplerProxy.setPort(source.getPropertyAsString("HTTPSampler.port"));
            samplerProxy.setDomain(source.getDomain());
            String bodyType = this.getBodyType(samplerProxy.getHeaders());
            if (source.getArguments() != null) {
                if (source.getPostBodyRaw()) {
                    samplerProxy.getBody().setType(Body.RAW);
                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                        samplerProxy.getBody().setRaw(v);
                    });
                    samplerProxy.getBody().initKvs();
                } else if (StringUtils.isNotEmpty(bodyType) || (source.getMethod().equalsIgnoreCase("POST") && source.getArguments().getArgumentsAsMap().size() > 0)) {
                    samplerProxy.getBody().setType(Body.WWW_FROM);
                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                        KeyValue keyValue = new KeyValue(k, v);
                        samplerProxy.getBody().getKvs().add(keyValue);
                    });
                } else if (samplerProxy.getBody() != null && samplerProxy.getBody().getType().equals(Body.FORM_DATA)) {
                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                        KeyValue keyValue = new KeyValue(k, v);
                        samplerProxy.getBody().getKvs().add(keyValue);
                    });
                } else {
                    List<KeyValue> keyValues = new LinkedList<>();
                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                        KeyValue keyValue = new KeyValue(k, v);
                        keyValues.add(keyValue);
                    });
                    if (CollectionUtils.isNotEmpty(keyValues)) {
                        samplerProxy.setArguments(keyValues);
                    }
                }
                samplerProxy.getBody().initBinary();
            }
            // samplerProxy.setPath(source.getPath());
            samplerProxy.setMethod(source.getMethod());
            if (this.getUrl(source) != null) {
                samplerProxy.setUrl(this.getUrl(source));
                samplerProxy.setPath(null);
            }
            samplerProxy.setId(UUID.randomUUID().toString());
            samplerProxy.setType("HTTPSamplerProxy");
            body.getKvs().add(new KeyValue());
            body.getBinary().add(new KeyValue());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertTCPSampler(MsTCPSampler msTCPSampler, TCPSampler tcpSampler) {
        msTCPSampler.setName(tcpSampler.getName());
        msTCPSampler.setType("TCPSampler");
        msTCPSampler.setServer(tcpSampler.getServer());
        msTCPSampler.setPort(tcpSampler.getPort() + "");
        msTCPSampler.setCtimeout(tcpSampler.getConnectTimeout() + "");
        msTCPSampler.setReUseConnection(tcpSampler.getProperty(TCPSampler.RE_USE_CONNECTION).getBooleanValue());
        msTCPSampler.setNodelay(tcpSampler.getProperty(TCPSampler.NODELAY).getBooleanValue());
        msTCPSampler.setCloseConnection(tcpSampler.isCloseConnection());
        msTCPSampler.setSoLinger(tcpSampler.getSoLinger() + "");
        msTCPSampler.setEolByte(tcpSampler.getEolByte() + "");
        msTCPSampler.setRequest(tcpSampler.getRequestData());
        msTCPSampler.setUsername(tcpSampler.getProperty(ConfigTestElement.USERNAME).getStringValue());
        msTCPSampler.setPassword(tcpSampler.getProperty(ConfigTestElement.PASSWORD).getStringValue());
    }

    private void convertDubboSample(MsDubboSampler elementNode, DubboSample sampler) {
        elementNode.setName(sampler.getName());
        elementNode.setType("DubboSampler");
        elementNode.set_interface(sampler.getPropertyAsString("FIELD_DUBBO_INTERFACE"));
        elementNode.setMethod(sampler.getPropertyAsString("FIELD_DUBBO_METHOD"));

        MsConfigCenter configCenter = new MsConfigCenter();
        configCenter.setProtocol(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_PROTOCOL"));
        configCenter.setGroup(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_GROUP"));
        configCenter.setNamespace(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_NAMESPACE"));
        configCenter.setUsername(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_USER_NAME"));
        configCenter.setPassword(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_PASSWORD"));
        configCenter.setAddress(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_ADDRESS"));
        configCenter.setTimeout(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_TIMEOUT"));
        elementNode.setConfigCenter(configCenter);

        MsRegistryCenter registryCenter = new MsRegistryCenter();
        registryCenter.setProtocol(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_PROTOCOL"));
        registryCenter.setAddress(sampler.getPropertyAsString("FIELD_DUBBO_ADDRESS"));
        registryCenter.setGroup(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_GROUP"));
        registryCenter.setUsername(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_USER_NAME"));
        registryCenter.setPassword(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_PASSWORD"));
        registryCenter.setTimeout(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_TIMEOUT"));
        elementNode.setRegistryCenter(registryCenter);

        MsConsumerAndService consumerAndService = new MsConsumerAndService();
        consumerAndService.setAsync(sampler.getPropertyAsString("FIELD_DUBBO_ASYNC"));
        consumerAndService.setCluster(sampler.getPropertyAsString("FIELD_DUBBO_CLUSTER"));
        consumerAndService.setConnections(sampler.getPropertyAsString("FIELD_DUBBO_CONNECTIONS"));
        consumerAndService.setGroup(sampler.getPropertyAsString("FIELD_DUBBO_GROUP"));
        consumerAndService.setLoadBalance(sampler.getPropertyAsString("FIELD_DUBBO_LOADBALANCE"));
        consumerAndService.setVersion(sampler.getPropertyAsString("FIELD_DUBBO_VERSION"));
        consumerAndService.setTimeout(sampler.getPropertyAsString("FIELD_DUBBO_TIMEOUT"));
        elementNode.setConsumerAndService(consumerAndService);

        List<MethodArgument> methodArguments = Constants.getMethodArgs(sampler);
        List<KeyValue> methodArgs = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(methodArguments)) {
            methodArguments.forEach(item -> {
                KeyValue keyValue = new KeyValue(item.getParamType(), item.getParamValue());
                methodArgs.add(keyValue);
            });
        }
        elementNode.setArgs(methodArgs);

        List<MethodArgument> arguments = Constants.getAttachmentArgs(sampler);
        List<KeyValue> attachmentArgs = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(arguments)) {
            arguments.forEach(item -> {
                KeyValue keyValue = new KeyValue(item.getParamType(), item.getParamValue());
                attachmentArgs.add(keyValue);
            });
        }
        elementNode.setAttachmentArgs(attachmentArgs);
    }

    /**
     * 优先初始化数据池
     */
    private void preInitPool(String projectId, HashTree hashTree) {
        // 初始化已有数据池
        initDataSource(projectId, ENV_NAME);
        // 添加当前jmx 中新的数据池
        preCreate(hashTree);
        // 更新数据源
        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        if (dataPools.getDataSources() != null) {
            dataPools.getEnvConfig().setDatabaseConfigs(new ArrayList<>(dataPools.getDataSources().values()));
        }
        if (dataPools.getIsCreate()) {
            dataPools.getTestEnvironmentWithBLOBs().setConfig(JSON.toJSONString(dataPools.getEnvConfig()));
            String id = environmentService.add(dataPools.getTestEnvironmentWithBLOBs());
            dataPools.setEnvId(id);
        } else {
            dataPools.getTestEnvironmentWithBLOBs().setConfig(JSON.toJSONString(dataPools.getEnvConfig()));
            environmentService.update(dataPools.getTestEnvironmentWithBLOBs());
        }
    }

    private void preCreate(HashTree tree) {
        for (Object key : tree.keySet()) {
            // JDBC 数据池
            if (key instanceof DataSourceElement) {
                DataSourceElement dataSourceElement = (DataSourceElement) key;
                DatabaseConfig newConfig = new DatabaseConfig();
                newConfig.setUsername(dataSourceElement.getPropertyAsString("username"));
                newConfig.setPassword(dataSourceElement.getPropertyAsString("password"));
                newConfig.setDriver(dataSourceElement.getPropertyAsString("driver"));
                newConfig.setDbUrl(dataSourceElement.getPropertyAsString("dbUrl"));
                newConfig.setName(dataSourceElement.getPropertyAsString("dataSource"));
                newConfig.setPoolMax(dataSourceElement.getPropertyAsInt("poolMax"));
                newConfig.setTimeout(dataSourceElement.getPropertyAsInt("timeout"));
                if (dataPools != null && dataPools.getDataSources() != null && dataPools.getDataSources().containsKey(dataSourceElement.getPropertyAsString("dataSource"))) {
                    DatabaseConfig config = dataPools.getDataSources().get(dataSourceElement.getPropertyAsString("dataSource"));
                    newConfig.setId(config.getId());
                    dataPools.getDataSources().put(dataSourceElement.getPropertyAsString("dataSource"), newConfig);
                } else {
                    newConfig.setId(UUID.randomUUID().toString());
                    if (dataPools.getDataSources() == null) {
                        dataPools.setDataSources(new HashMap<>());
                    }
                    dataPools.getDataSources().put(dataSourceElement.getPropertyAsString("dataSource"), newConfig);
                }
            } else if (key instanceof HTTPSamplerProxy) {
                // 把HTTP 请求下的HeaderManager 取出来
                HashTree node = tree.get(key);
                if (node != null) {
                    for (Object nodeKey : node.keySet()) {
                        if (nodeKey instanceof HeaderManager) {
                            if (headerMap.containsKey(key.hashCode())) {
                                headerMap.get(key.hashCode()).add(nodeKey);
                            } else {
                                List<Object> objects = new LinkedList<Object>() {{
                                    this.add(nodeKey);
                                }};
                                headerMap.put(key.hashCode(), objects);
                            }
                        }
                    }
                }
            }

            // 递归子项
            HashTree node = tree.get(key);
            if (node != null) {
                preCreate(node);
            }
        }
    }

    private ImportPoolsDTO dataPools;

    private void initDataSource(String projectId, String name) {
        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(name);
        // 这里的数据只有一条，如果多条则有问题
        List<ApiTestEnvironmentWithBLOBs> environments = environmentService.selectByExampleWithBLOBs(example);
        dataPools = new ImportPoolsDTO();
        if (CollectionUtils.isNotEmpty(environments)) {
            dataPools.setIsCreate(false);
            dataPools.setTestEnvironmentWithBLOBs(environments.get(0));
            Map<String, DatabaseConfig> dataSources = new HashMap<>();
            environments.forEach(environment -> {
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig envConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    dataPools.setEnvConfig(envConfig);
                    if (envConfig != null && CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                        envConfig.getDatabaseConfigs().forEach(item -> {
                            dataSources.put(item.getName(), item);
                        });
                    }
                }
                dataPools.setEnvId(environment.getId());
                dataPools.setDataSources(dataSources);
            });
        } else {
            dataPools.setIsCreate(true);
            ApiTestEnvironmentWithBLOBs apiTestEnvironmentWithBLOBs = new ApiTestEnvironmentWithBLOBs();
            apiTestEnvironmentWithBLOBs.setId(UUID.randomUUID().toString());
            dataPools.setEnvId(apiTestEnvironmentWithBLOBs.getId());
            dataPools.setEnvConfig(new EnvironmentConfig());
            apiTestEnvironmentWithBLOBs.setName(ENV_NAME);
            apiTestEnvironmentWithBLOBs.setProjectId(projectId);
            dataPools.setTestEnvironmentWithBLOBs(apiTestEnvironmentWithBLOBs);
        }
    }

    private void convertJDBCSampler(MsJDBCSampler msJDBCSampler, JDBCSampler jdbcSampler) {
        msJDBCSampler.setType("JDBCSampler");
        msJDBCSampler.setName(jdbcSampler.getName());
        msJDBCSampler.setProtocol("SQL");
        msJDBCSampler.setQuery(jdbcSampler.getPropertyAsString("query"));
        msJDBCSampler.setQueryTimeout(jdbcSampler.getPropertyAsInt("queryTimeout"));
        msJDBCSampler.setResultVariable(jdbcSampler.getPropertyAsString("resultVariable"));
        msJDBCSampler.setVariableNames(jdbcSampler.getPropertyAsString("variableNames"));
        msJDBCSampler.setEnvironmentId(dataPools.getEnvId());
        if (dataPools.getDataSources() != null && dataPools.getDataSources().get(jdbcSampler.getPropertyAsString("dataSource")) != null) {
            msJDBCSampler.setDataSourceId(dataPools.getDataSources().get(jdbcSampler.getPropertyAsString("dataSource")).getId());
        }
        msJDBCSampler.setVariables(new LinkedList<>());
    }

    private void convertMsExtract(MsExtract extract, Object key) {
        // 提取数据单独处理
        extract.setType("Extract");
        extract.setJson(new LinkedList<>());
        extract.setRegex(new LinkedList<>());
        extract.setXpath(new LinkedList<>());
        if (key instanceof RegexExtractor) {
            MsExtractRegex regex = new MsExtractRegex();
            RegexExtractor regexExtractor = (RegexExtractor) key;
            if (regexExtractor.useRequestHeaders()) {
                regex.setUseHeaders("request_headers");
            } else if (regexExtractor.useBody()) {
                regex.setUseHeaders("false");
            } else if (regexExtractor.useUnescapedBody()) {
                regex.setUseHeaders("unescaped");
            } else if (regexExtractor.useBodyAsDocument()) {
                regex.setUseHeaders("as_document");
            } else if (regexExtractor.useUrl()) {
                regex.setUseHeaders("URL");
            }
            regex.setExpression(regexExtractor.getRegex());
            regex.setVariable(regexExtractor.getRefName());
            extract.setName(regexExtractor.getName());
            extract.getRegex().add(regex);
        } else if (key instanceof XPath2Extractor) {
            XPath2Extractor xPath2Extractor = (XPath2Extractor) key;
            MsExtractXPath xPath = new MsExtractXPath();
            xPath.setVariable(xPath2Extractor.getRefName());
            xPath.setExpression(xPath2Extractor.getXPathQuery());

            extract.setName(xPath2Extractor.getName());
            extract.getXpath().add(xPath);
        } else if (key instanceof JSONPostProcessor) {
            JSONPostProcessor jsonPostProcessor = (JSONPostProcessor) key;
            String[] names = StringUtils.isNotEmpty(jsonPostProcessor.getRefNames()) ? jsonPostProcessor.getRefNames().split(";") : null;
            String[] values = StringUtils.isNotEmpty(jsonPostProcessor.getJsonPathExpressions()) ? jsonPostProcessor.getJsonPathExpressions().split(";") : null;
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    MsExtractJSONPath jsonPath = new MsExtractJSONPath();
                    jsonPath.setVariable(names[i]);
                    if (values != null && values.length > i) {
                        jsonPath.setExpression(values[i]);
                    }
                    jsonPath.setMultipleMatching(jsonPostProcessor.getComputeConcatenation());
                    extract.setName(jsonPostProcessor.getName());
                    extract.getJson().add(jsonPath);
                }
            }
        }
    }

    private void convertMsAssertions(MsAssertions assertions, Object key) {
        assertions.setJsonPath(new LinkedList<>());
        assertions.setJsr223(new LinkedList<>());
        assertions.setXpath2(new LinkedList<>());
        assertions.setRegex(new LinkedList<>());
        assertions.setDuration(new MsAssertionDuration());
        assertions.setType("Assertions");
        if (key instanceof ResponseAssertion) {
            MsAssertionRegex assertionRegex = new MsAssertionRegex();
            ResponseAssertion assertion = (ResponseAssertion) key;
            assertionRegex.setDescription(assertion.getName());
            assertionRegex.setAssumeSuccess(assertion.getAssumeSuccess());
            if (assertion.getTestStrings() != null && !assertion.getTestStrings().isEmpty()) {
                assertionRegex.setExpression(assertion.getTestStrings().get(0).getStringValue());
            }
            if (assertion.isTestFieldResponseData()) {
                assertionRegex.setSubject("Response Data");
            }
            if (assertion.isTestFieldResponseCode()) {
                assertionRegex.setSubject("Response Code");
            }
            if (assertion.isTestFieldResponseHeaders()) {
                assertionRegex.setSubject("Response Headers");
            }
            assertions.setName(assertion.getName());
            assertions.getRegex().add(assertionRegex);
        } else if (key instanceof JSONPathAssertion) {
            MsAssertionJsonPath assertionJsonPath = new MsAssertionJsonPath();
            JSONPathAssertion jsonPathAssertion = (JSONPathAssertion) key;
            assertionJsonPath.setDescription(jsonPathAssertion.getName());
            assertionJsonPath.setExpression(jsonPathAssertion.getJsonPath());
            assertionJsonPath.setExpect(jsonPathAssertion.getExpectedValue());
            assertionJsonPath.setOption(jsonPathAssertion.getPropertyAsString("ASS_OPTION"));
            assertions.setName(jsonPathAssertion.getName());
            assertions.getJsonPath().add(assertionJsonPath);
        } else if (key instanceof XPath2Assertion) {
            MsAssertionXPath2 assertionXPath2 = new MsAssertionXPath2();
            XPath2Assertion xPath2Assertion = (XPath2Assertion) key;
            assertionXPath2.setExpression(xPath2Assertion.getXPathString());
            assertions.setName(xPath2Assertion.getName());
            assertions.getXpath2().add(assertionXPath2);
        } else if (key instanceof JSR223Assertion) {
            MsAssertionJSR223 msAssertionJSR223 = new MsAssertionJSR223();
            JSR223Assertion jsr223Assertion = (JSR223Assertion) key;
            msAssertionJSR223.setName(jsr223Assertion.getName());
            msAssertionJSR223.setDesc(jsr223Assertion.getName());
            msAssertionJSR223.setScript(jsr223Assertion.getPropertyAsString("script"));
            msAssertionJSR223.setScriptLanguage(jsr223Assertion.getPropertyAsString("scriptLanguage"));
            assertions.setName(jsr223Assertion.getName());

            assertions.getJsr223().add(msAssertionJSR223);
        } else if (key instanceof DurationAssertion) {
            MsAssertionDuration assertionDuration = new MsAssertionDuration();
            DurationAssertion durationAssertion = (DurationAssertion) key;
            assertionDuration.setValue(durationAssertion.getProperty("DurationAssertion.duration").getIntValue());
            assertions.setName(durationAssertion.getName());
            assertions.setDuration(assertionDuration);
        }
    }

    /**
     * 把节点对象转成XML，不然执行时无法正常转换
     *
     * @param obj
     * @return
     */
    public static String objToXml(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SaveService.saveElement(obj, baos);
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.warn("HashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    private void jmterHashTree(HashTree tree, MsTestElement scenario) {
        for (Object key : tree.keySet()) {
            MsTestElement elementNode;
            if (CollectionUtils.isEmpty(scenario.getHashTree())) {
                scenario.setHashTree(new LinkedList<>());
            }
            // 测试计划
            if (key instanceof TestPlan) {
                scenario.setName(((TestPlan) key).getName());
                elementNode = new MsJmeterElement();
                elementNode.setName(((TestPlan) key).getName());
                ((MsJmeterElement) elementNode).setJmeterElement(objToXml(key));
                ((MsJmeterElement) elementNode).setElementType(key.getClass().getSimpleName());
            }
            // 线程组
            else if (key instanceof ThreadGroup) {
                elementNode = new MsScenario(((ThreadGroup) key).getName());
            }
            // HTTP请求
            else if (key instanceof HTTPSamplerProxy) {
                elementNode = new MsHTTPSamplerProxy();
                ((MsHTTPSamplerProxy) elementNode).setBody(new Body());
                convertHttpSampler((MsHTTPSamplerProxy) elementNode, key);
            }
            // TCP请求
            else if (key instanceof TCPSampler) {
                elementNode = new MsTCPSampler();
                TCPSampler tcpSampler = (TCPSampler) key;
                convertTCPSampler((MsTCPSampler) elementNode, tcpSampler);
            }
            // DUBBO请求
            else if (key instanceof DubboSample) {
                DubboSample sampler = (DubboSample) key;
                elementNode = new MsDubboSampler();
                convertDubboSample((MsDubboSampler) elementNode, sampler);
            }
            // JDBC请求
            else if (key instanceof JDBCSampler) {
                elementNode = new MsJDBCSampler();
                JDBCSampler jdbcSampler = (JDBCSampler) key;
                convertJDBCSampler((MsJDBCSampler) elementNode, jdbcSampler);
            }
            // JSR自定义脚本
            else if (key instanceof JSR223Sampler) {
                JSR223Sampler jsr223Sampler = (JSR223Sampler) key;
                elementNode = new MsJSR223Processor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
                ((MsJSR223Processor) elementNode).setScript(jsr223Sampler.getPropertyAsString("script"));
                ((MsJSR223Processor) elementNode).setScriptLanguage(jsr223Sampler.getPropertyAsString("scriptLanguage"));
            }
            // 后置脚本
            else if (key instanceof JSR223PostProcessor) {
                JSR223PostProcessor jsr223Sampler = (JSR223PostProcessor) key;
                elementNode = new MsJSR223PostProcessor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
                ((MsJSR223PostProcessor) elementNode).setScript(jsr223Sampler.getPropertyAsString("script"));
                ((MsJSR223PostProcessor) elementNode).setScriptLanguage(jsr223Sampler.getPropertyAsString("scriptLanguage"));
            }
            // 前置脚本
            else if (key instanceof JSR223PreProcessor) {
                JSR223PreProcessor jsr223Sampler = (JSR223PreProcessor) key;
                elementNode = new MsJSR223PreProcessor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
                ((MsJSR223PreProcessor) elementNode).setScript(jsr223Sampler.getPropertyAsString("script"));
                ((MsJSR223PreProcessor) elementNode).setScriptLanguage(jsr223Sampler.getPropertyAsString("scriptLanguage"));
            }
            // 断言规则
            else if (key instanceof ResponseAssertion || key instanceof JSONPathAssertion || key instanceof XPath2Assertion || key instanceof JSR223Assertion || key instanceof DurationAssertion) {
                elementNode = new MsAssertions();
                convertMsAssertions((MsAssertions) elementNode, key);
            }
            // 提取参数
            else if (key instanceof RegexExtractor || key instanceof XPath2Extractor || key instanceof JSONPostProcessor) {
                elementNode = new MsExtract();
                convertMsExtract((MsExtract) elementNode, key);
            }
            // 定时器
            else if (key instanceof ConstantTimer) {
                elementNode = new MsConstantTimer();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("ConstantTimer");
            }
            // IF条件控制器，这里平台方式和jmeter 不同，暂时不处理
//            else if (key instanceof IfController) {
//                elementNode = new MsIfController();
//                BeanUtils.copyBean(elementNode, key);
//                elementNode.setType("IfController");
//            }
            // 次数循环控制器
            else if (key instanceof LoopController) {
                elementNode = new MsLoopController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("LoopController");
                ((MsLoopController) elementNode).setLoopType(LoopConstants.LOOP_COUNT.name());
                LoopController loopController = (LoopController) key;
                CountController countController = new CountController();
                countController.setLoops(loopController.getLoops());
                countController.setProceed(true);
                ((MsLoopController) elementNode).setCountController(countController);
            }
            // While循环控制器
            else if (key instanceof WhileController) {
                elementNode = new MsLoopController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("LoopController");
                ((MsLoopController) elementNode).setLoopType(LoopConstants.WHILE.name());
                WhileController whileController = (WhileController) key;
                MsWhileController countController = new MsWhileController();
                countController.setValue(whileController.getCondition());
                ((MsLoopController) elementNode).setWhileController(countController);
            }
            // Foreach 循环控制器
            else if (key instanceof ForeachController) {
                elementNode = new MsLoopController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("LoopController");
                ((MsLoopController) elementNode).setLoopType(LoopConstants.FOREACH.name());
                ForeachController foreachController = (ForeachController) key;
                MsForEachController countController = new MsForEachController();
                countController.setInputVal(foreachController.getInputValString());
                countController.setReturnVal(foreachController.getReturnValString());
                ((MsLoopController) elementNode).setForEachController(countController);
            } else if (key instanceof TransactionController) {
                TransactionController transactionController = (TransactionController) key;
                elementNode = new MsTransactionController();
                elementNode.setName(transactionController.getName());
                ((MsTransactionController) elementNode).setGenerateParentSample(transactionController.isGenerateParentSample());
                ((MsTransactionController) elementNode).setIncludeTimers(transactionController.isIncludeTimers());
            }
            // 平台不能识别的Jmeter步骤
            else {
                // HTTP 请求下的所有HeaderManager已经加到请求中
                if (scenario instanceof MsHTTPSamplerProxy && key instanceof HeaderManager) {
                    continue;
                }
                elementNode = new MsJmeterElement();
                elementNode.setType("JmeterElement");
                TestElement testElement = (TestElement) key;
                elementNode.setName(testElement.getName());
                ((MsJmeterElement) elementNode).setJmeterElement(objToXml(key));
                ((MsJmeterElement) elementNode).setElementType(key.getClass().getSimpleName());
            }
            elementNode.setEnable(((TestElement) key).isEnabled());
            elementNode.setResourceId(UUID.randomUUID().toString());
            elementNode.setId(UUID.randomUUID().toString());
            elementNode.setIndex(scenario.getHashTree().size() + 1 + "");
            // 提取参数
            if (elementNode instanceof MsExtract) {
                if (CollectionUtils.isNotEmpty(((MsExtract) elementNode).getJson()) || CollectionUtils.isNotEmpty(((MsExtract) elementNode).getRegex()) || CollectionUtils.isNotEmpty(((MsExtract) elementNode).getXpath())) {
                    scenario.getHashTree().add(elementNode);
                }
            }
            //断言规则
            else if (elementNode instanceof MsAssertions) {
                if (CollectionUtils.isNotEmpty(((MsAssertions) elementNode).getRegex()) || CollectionUtils.isNotEmpty(((MsAssertions) elementNode).getJsonPath())
                        || CollectionUtils.isNotEmpty(((MsAssertions) elementNode).getJsr223()) || CollectionUtils.isNotEmpty(((MsAssertions) elementNode).getXpath2()) || ((MsAssertions) elementNode).getDuration() != null) {
                    scenario.getHashTree().add(elementNode);
                }
            }
            // 争取其他请求
            else {
                scenario.getHashTree().add(elementNode);
            }
            // 递归子项
            HashTree node = tree.get(key);
            if (node != null) {
                jmterHashTree(node, elementNode);
            }
        }
    }
}
