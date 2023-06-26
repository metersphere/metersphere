package io.metersphere.api.parse.api;


import io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample;
import io.github.ningyu.jmeter.plugin.dubbo.sample.MethodArgument;
import io.github.ningyu.jmeter.plugin.util.Constants;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ImportPoolsDTO;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.assertions.*;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
import io.metersphere.api.dto.definition.request.extract.MsExtractJSONPath;
import io.metersphere.api.dto.definition.request.extract.MsExtractRegex;
import io.metersphere.api.dto.definition.request.extract.MsExtractXPath;
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
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.api.parse.scenario.JMeterParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.*;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.modifiers.BeanShellPreProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
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
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class JmeterDefinitionParser extends ApiImportAbstractParser<ApiDefinitionImport> {
    private final Map<Integer, List<Object>> headerMap = new HashMap<>();
    private ImportPoolsDTO dataPools;
    private final String ENV_NAME = "导入数据环境";
    private static final String P0 = "P0";


    private String planName = PropertyConstant.DEFAULT;
    private static final Integer GROUP_GLOBAL = 1;
    private static final String ALWAYS_ENCODE = "HTTPArgument.always_encode";

    @Override
    public ApiDefinitionImport parse(InputStream inputSource, ApiTestImportRequest request) {
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        try {
            Object scriptWrapper = SaveService.loadElement(inputSource);
            HashTree testPlan = this.getHashTree(scriptWrapper);
            // 优先初始化数据源及部分参数
            preInitPool(request.getProjectId(), testPlan);
            MsScenario scenario = new MsScenario();
            List<ApiDefinitionWithBLOBs> definitions = new ArrayList<>();
            List<ApiTestCaseWithBLOBs> definitionCases = new ArrayList<>();

            jmeterHashTree(testPlan, scenario);

           /* this.selectModule = ApiDefinitionImportUtil.getSelectModule(request.getModuleId());
            if (this.selectModule != null) {
                this.selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(this.selectModule.getName(), this.selectModule.getParentId());
            }
            this.apiModule = ApiDefinitionImportUtil.buildModule(this.selectModule, this.planName, this.projectId);*/

            LinkedList<MsTestElement> elements = scenario.getHashTree();
            LinkedList<MsTestElement> results = new LinkedList<>();
            getAllApiCase(elements, results);

            for (MsTestElement element : results) {
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = buildApiDefinition(element);
                if (apiDefinitionWithBLOBs != null) {
                    element.setEnable(true);
                    apiDefinitionWithBLOBs.setRequest(JSON.toJSONString(element));
                    HttpResponse defaultHttpResponse = getDefaultHttpResponse();
                    apiDefinitionWithBLOBs.setResponse(JSON.toJSONString(defaultHttpResponse));
                    definitions.add(apiDefinitionWithBLOBs);

                    ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                    BeanUtils.copyBean(apiTestCase, apiDefinitionWithBLOBs);
                    apiTestCase.setApiDefinitionId(apiDefinitionWithBLOBs.getId());
                    apiTestCase.setCaseStatus(ApiTestDataStatus.PREPARE.getValue());
                    apiTestCase.setPriority(P0);
                    definitionCases.add(apiTestCase);
                }
            }
            apiImport.setData(definitions);
            apiImport.setCases(definitionCases);
            return apiImport;
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException("当前JMX版本不兼容");
        }
        return null;
    }

    private void getAllApiCase(LinkedList<MsTestElement> elements, LinkedList<MsTestElement> results) {
        for (MsTestElement element : elements) {
            if (element instanceof MsHTTPSamplerProxy || element instanceof MsTCPSampler
                    || element instanceof MsJDBCSampler || element instanceof MsDubboSampler) {
                // todo 合并buildApiDefinition方法
                results.add(element);
            } else {
                LinkedList<MsTestElement> hashTree = element.getHashTree();
                if (hashTree != null) {
                    getAllApiCase(hashTree, results);
                }
            }
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
            } else if (key instanceof ThreadGroup) {
                HashTree group = tree.get(key);
                if (group != null) {
                    List<Object> nk = new ArrayList<>();
                    for (Object groupNode : group.keySet()) {
                        if (groupNode instanceof HeaderManager) {
                            nk.add(groupNode);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(nk)) {
                        for (Object gn : group.keySet()) {
                            if (gn instanceof HTTPSamplerProxy) {
                                if (headerMap.containsKey(gn.hashCode() + GROUP_GLOBAL)) {
                                    headerMap.get(gn.hashCode()).addAll(nk);
                                } else {
                                    List<Object> objects = new LinkedList<Object>() {{
                                        this.addAll(nk);
                                    }};
                                    headerMap.put(gn.hashCode() + GROUP_GLOBAL, objects);
                                }
                            }
                        }
                    }
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

    private void preInitPool(String projectId, HashTree hashTree) {
        // 初始化已有数据池
        initDataSource(projectId, ENV_NAME);
        // 添加当前jmx 中新的数据池
        preCreate(hashTree);
        // 更新数据源
        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        if (dataPools.getDataSources() != null) {
            dataPools.getEnvConfig().setDatabaseConfigs(new ArrayList<>(dataPools.getDataSources().values()));
        }
        if (dataPools.getIsCreate()) {
            dataPools.getTestEnvironmentWithBLOBs().setConfig(JSON.toJSONString(dataPools.getEnvConfig()));
            String id = apiTestEnvironmentService.add(dataPools.getTestEnvironmentWithBLOBs());
            dataPools.setEnvId(id);
        } else {
            dataPools.getTestEnvironmentWithBLOBs().setConfig(JSON.toJSONString(dataPools.getEnvConfig()));
            apiTestEnvironmentService.update(dataPools.getTestEnvironmentWithBLOBs());
        }
    }

    private void initDataSource(String projectId, String name) {
        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(name);
        // 这里的数据只有一条，如果多条则有问题
        List<ApiTestEnvironmentWithBLOBs> environments = apiTestEnvironmentService.selectByExampleWithBLOBs(example);
        dataPools = new ImportPoolsDTO();
        if (CollectionUtils.isNotEmpty(environments)) {
            dataPools.setIsCreate(false);
            dataPools.setTestEnvironmentWithBLOBs(environments.get(0));
            Map<String, DatabaseConfig> dataSources = new HashMap<>();
            environments.forEach(environment -> {
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig envConfig = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    dataPools.setEnvConfig(envConfig);
                    if (envConfig != null && CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                        envConfig.getDatabaseConfigs().forEach(item -> dataSources.put(item.getName(), item));
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

    private void preBuildApiDefinition(ApiDefinitionWithBLOBs apiDefinition, MsTestElement element, String protocol) {
        apiDefinition.setId(UUID.randomUUID().toString());
        apiDefinition.setName(element.getName());
        apiDefinition.setProjectId(this.projectId);
        apiDefinition.setRequest(JSON.toJSONString(element));
        if (StringUtils.isNotBlank(this.planName)) {
            apiDefinition.setModulePath("/" + this.planName);
        }
        // todo 除HTTP协议外，其它协议设置默认模块
        apiDefinition.setStatus("Prepare");
        apiDefinition.setProtocol(protocol);
        apiDefinition.setUserId(SessionUtils.getUserId());
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setCreateUser(SessionUtils.getUserId());
        apiDefinition.setCaseTotal("1");
    }

    private ApiDefinitionWithBLOBs buildApiDefinition(MsTestElement element) {
        ApiDefinitionWithBLOBs apiDefinition = null;
        if (element instanceof MsHTTPSamplerProxy) {
            apiDefinition = new ApiDefinitionWithBLOBs();
            preBuildApiDefinition(apiDefinition, element, RequestTypeConstants.HTTP);
            apiDefinition.setPath(((MsHTTPSamplerProxy) element).getPath());
            apiDefinition.setMethod(((MsHTTPSamplerProxy) element).getMethod());
            apiDefinition.setUserId(SessionUtils.getUserId());
        } else if (element instanceof MsTCPSampler) {
            apiDefinition = new ApiDefinitionWithBLOBs();
            preBuildApiDefinition(apiDefinition, element, RequestTypeConstants.TCP);
            apiDefinition.setMethod(RequestTypeConstants.TCP);
        } else if (element instanceof MsJDBCSampler) {
            apiDefinition = new ApiDefinitionWithBLOBs();
            preBuildApiDefinition(apiDefinition, element, RequestTypeConstants.SQL);
            apiDefinition.setMethod(RequestTypeConstants.SQL);
        } else if (element instanceof MsDubboSampler) {
            apiDefinition = new ApiDefinitionWithBLOBs();
            preBuildApiDefinition(apiDefinition, element, RequestTypeConstants.DUBBO);
            apiDefinition.setProtocol(RequestTypeConstants.DUBBO);
            apiDefinition.setMethod(((MsDubboSampler) element).getMethod());
        }
        return apiDefinition;
    }


    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    private void jmeterHashTree(HashTree tree, MsTestElement scenario) {
        for (Object key : tree.keySet()) {
            MsTestElement elementNode = null;
            if (CollectionUtils.isEmpty(scenario.getHashTree())) {
                scenario.setHashTree(new LinkedList<>());
            }
            // 测试计划
            if (key instanceof TestPlan) {
                this.planName = ((TestPlan) key).getName();
                elementNode = new MsJmeterElement();
                ((MsJmeterElement) elementNode).setJmeterElement(JMeterParser.objToXml(key));
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
            // 后置脚本
            else if (key instanceof JSR223PostProcessor) {
                JSR223PostProcessor jsr223Sampler = (JSR223PostProcessor) key;
                elementNode = new MsJSR223PostProcessor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
                ((MsJSR223PostProcessor) elementNode).setScript(jsr223Sampler.getPropertyAsString(ElementConstants.SCRIPT));
                ((MsJSR223PostProcessor) elementNode).setScriptLanguage(jsr223Sampler.getPropertyAsString("scriptLanguage"));
            }
            // 前置脚本
            else if (key instanceof JSR223PreProcessor) {
                JSR223PreProcessor jsr223Sampler = (JSR223PreProcessor) key;
                elementNode = new MsJSR223PreProcessor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
                ((MsJSR223PreProcessor) elementNode).setScript(jsr223Sampler.getPropertyAsString(ElementConstants.SCRIPT));
                ((MsJSR223PreProcessor) elementNode).setScriptLanguage(jsr223Sampler.getPropertyAsString("scriptLanguage"));
            } else if (key instanceof BeanShellPostProcessor) {
                elementNode = JMeterParser.getMsTestElement((BeanShellPostProcessor) key);
            } else if (key instanceof BeanShellPreProcessor) {
                elementNode = JMeterParser.getMsTestElement((BeanShellPreProcessor) key);
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
            // 平台不能识别的Jmeter步骤
            else {
                // HTTP 请求下的所有HeaderManager已经加到请求中
                if (scenario instanceof MsHTTPSamplerProxy && key instanceof HeaderManager) {
                    continue;
                }
                elementNode = new MsJmeterElement();
            }

            elementNode.setEnable(((TestElement) key).isEnabled());
            elementNode.setResourceId(UUID.randomUUID().toString());

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
                jmeterHashTree(node, elementNode);
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
            if (assertion.isNotType()) {
                assertionRegex.setExpression("(?s)^((?!" + assertionRegex.getExpression() + ").)*$");
                assertionRegex.setDescription(" not contains: " + assertionRegex.getDescription());
            }
            if (assertion.isEqualsType()) {
                assertionRegex.setExpression("^" + assertionRegex.getExpression() + "$");
                assertionRegex.setDescription(" equals: " + assertionRegex.getExpression());
            }
            if (assertion.isContainsType()) {
                assertionRegex.setExpression(".*" + assertionRegex.getExpression() + ".*");
                assertionRegex.setDescription(" contains: " + assertionRegex.getExpression());
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
            assertionJsonPath.setOption(jsonPathAssertion.getPropertyAsString(PropertyConstant.ASS_OPTION));
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
            msAssertionJSR223.setScript(jsr223Assertion.getPropertyAsString(ElementConstants.SCRIPT));
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
            } else if (regexExtractor.useHeaders()) {
                regex.setUseHeaders("true");
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

    private void convertTCPSampler(MsTCPSampler msTCPSampler, TCPSampler tcpSampler) {
        msTCPSampler.setName(tcpSampler.getName());
        msTCPSampler.setType("TCPSampler");
        msTCPSampler.setServer(tcpSampler.getServer());
        msTCPSampler.setPort(tcpSampler.getPort() + StringUtils.EMPTY);
        msTCPSampler.setCtimeout(tcpSampler.getConnectTimeout() + StringUtils.EMPTY);
        msTCPSampler.setClassname(tcpSampler.getProperty(TCPSampler.CLASSNAME).getStringValue());
        msTCPSampler.setReUseConnection(tcpSampler.getProperty(TCPSampler.RE_USE_CONNECTION).getBooleanValue());
        msTCPSampler.setNodelay(tcpSampler.getProperty(TCPSampler.NODELAY).getBooleanValue());
        msTCPSampler.setCloseConnection(tcpSampler.isCloseConnection());
        msTCPSampler.setSoLinger(tcpSampler.getSoLinger() + StringUtils.EMPTY);
        msTCPSampler.setEolByte(tcpSampler.getEolByte() + StringUtils.EMPTY);
        msTCPSampler.setRequest(tcpSampler.getRequestData());
        msTCPSampler.setUsername(tcpSampler.getProperty(ConfigTestElement.USERNAME).getStringValue());
        msTCPSampler.setPassword(tcpSampler.getProperty(ConfigTestElement.PASSWORD).getStringValue());
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
        if (StringUtils.isEmpty(elementNode.getMethod())) {
            elementNode.setMethod(elementNode.getProtocol());
        }
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
            // 处理ThreadGroup组内请求头
            if (headerMap.containsKey(key.hashCode() + GROUP_GLOBAL)) {
                List<KeyValue> keyValues = new LinkedList<>();
                headerMap.get(key.hashCode() + GROUP_GLOBAL).forEach(item -> {
                    HeaderManager headerManager = (HeaderManager) item;
                    if (headerManager.getHeaders() != null) {
                        for (int i = 0; i < headerManager.getHeaders().size(); i++) {
                            keyValues.add(new KeyValue(headerManager.getHeader(i).getName(), headerManager.getHeader(i).getValue()));
                        }
                    }
                });
                // 合并Header
                if (CollectionUtils.isNotEmpty(samplerProxy.getHeaders())) {
                    keyValues.addAll(samplerProxy.getHeaders());
                }
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
            samplerProxy.setProtocol(RequestTypeConstants.HTTP);
            samplerProxy.setConnectTimeout(source.getConnectTimeout() + StringUtils.EMPTY);
            samplerProxy.setResponseTimeout(source.getResponseTimeout() + StringUtils.EMPTY);
            samplerProxy.setPort(source.getPropertyAsString("HTTPSampler.port"));
            String bodyType = this.getBodyType(samplerProxy.getHeaders());
            if (source.getArguments() != null) {
                if (source.getPostBodyRaw()) {
                    List<KeyValue> headers = samplerProxy.getHeaders();
                    boolean jsonType = false;
                    if (CollectionUtils.isNotEmpty(headers)) {
                        for (KeyValue header : headers) {
                            if (StringUtils.equals(header.getName(), "Content-Type") && StringUtils.equals(header.getValue(), "application/json")) {
                                samplerProxy.getBody().setType(Body.JSON_STR);
                                jsonType = true;
                                break;
                            }
                        }
                    }
                    if (!jsonType) {
                        samplerProxy.getBody().setType(Body.RAW);
                    }
                    source.getArguments().getArgumentsAsMap().forEach((k, v) -> samplerProxy.getBody().setRaw(v));
                    samplerProxy.getBody().initKvs();
                } else if (StringUtils.isNotEmpty(bodyType) || ("POST".equalsIgnoreCase(source.getMethod()) && source.getArguments().getArgumentsAsMap().size() > 0)) {
                    samplerProxy.getBody().setType(Body.WWW_FROM);
                    source.getArguments().getArguments().forEach(params -> {
                        KeyValue keyValue = new KeyValue();
                        parseParams(params, keyValue);
                        samplerProxy.getBody().getKvs().add(keyValue);
                    });
                } else if (samplerProxy.getBody() != null && samplerProxy.getBody().getType().equals(Body.FORM_DATA)) {
                    source.getArguments().getArguments().forEach(params -> {
                        KeyValue keyValue = new KeyValue();
                        parseParams(params, keyValue);
                        samplerProxy.getBody().getKvs().add(keyValue);
                    });
                } else {
                    List<KeyValue> keyValues = new LinkedList<>();
                    source.getArguments().getArguments().forEach(params -> {
                        KeyValue keyValue = new KeyValue();
                        parseParams(params, keyValue);
                        keyValues.add(keyValue);
                    });
                    if (CollectionUtils.isNotEmpty(keyValues)) {
                        samplerProxy.setArguments(keyValues);
                    }
                }
                samplerProxy.getBody().initBinary();
            }
            samplerProxy.setPath(source.getPath());
            samplerProxy.setMethod(source.getMethod());
            samplerProxy.setId(UUID.randomUUID().toString());
            samplerProxy.setType("HTTPSamplerProxy");
            body.getKvs().add(new KeyValue());
            body.getBinary().add(new KeyValue());

        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void parseParams(JMeterProperty params, KeyValue keyValue) {
        if (params == null || keyValue == null) {
            return;
        }
        Object objValue = params.getObjectValue();
        if (objValue instanceof HTTPArgument) {
            HTTPArgument argument = (HTTPArgument) objValue;
            boolean propertyAsBoolean = argument.getPropertyAsBoolean(ALWAYS_ENCODE);
            keyValue.setUrlEncode(propertyAsBoolean);
            keyValue.setName(argument.getName());
            keyValue.setValue(argument.getValue());
        }
    }

    private String getBodyType(List<KeyValue> headers) {
        if (CollectionUtils.isNotEmpty(headers)) {
            List<KeyValue> keyValues = headers.stream().filter(keyValue -> "Content-Type".equals(keyValue.getName()) && "application/x-www-form-urlencoded".equals(keyValue.getValue())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(keyValues)) {
                return keyValues.get(0).getValue();
            }
        }
        return null;
    }
}
