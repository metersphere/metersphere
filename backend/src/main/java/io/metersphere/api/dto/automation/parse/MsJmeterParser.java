package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample;
import io.github.ningyu.jmeter.plugin.dubbo.sample.MethodArgument;
import io.github.ningyu.jmeter.plugin.util.Constants;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.assertions.*;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
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
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.constants.LoopConstants;
import io.metersphere.commons.utils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.assertions.*;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MsJmeterParser extends ScenarioImportAbstractParser {
    @Override
    public ScenarioImport parse(InputStream inputSource, ApiTestImportRequest request) {
        try {
            Object scriptWrapper = SaveService.loadElement(inputSource);
            HashTree testPlan = this.getHashTree(scriptWrapper);
            MsScenario scenario = new MsScenario();
            scenario.setReferenced("REF");
            LinkedList<MsTestElement> hashTrees = new LinkedList<>();
            scenario.setHashTree(hashTrees);
            getTree(testPlan, scenario);
            this.projectId = request.getProjectId();

            ScenarioImport scenarioImport = new ScenarioImport();
            scenarioImport.setData(paseObj(scenario, request));
            scenarioImport.setProjectid(request.getProjectId());
            return scenarioImport;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ApiScenarioWithBLOBs> paseObj(MsScenario msScenario, ApiTestImportRequest request) {
        List<ApiScenarioWithBLOBs> scenarioWithBLOBsList = new ArrayList<>();
        ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        ApiScenarioModule module = buildModule(getSelectModule(request.getModuleId()), msScenario.getName());
        scenarioWithBLOBs.setName(msScenario.getName());
        scenarioWithBLOBs.setProjectId(request.getProjectId());
        if (msScenario != null && CollectionUtils.isNotEmpty(msScenario.getHashTree())) {
            scenarioWithBLOBs.setStepTotal(msScenario.getHashTree().size());
        }
        if (module != null) {
            scenarioWithBLOBs.setApiScenarioModuleId(module.getId());
            scenarioWithBLOBs.setModulePath("/" + module.getName());
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

    private void convertHttpSampler(MsHTTPSamplerProxy samplerProxy, HTTPSamplerProxy source) {
        try {
            BeanUtils.copyBean(samplerProxy, source);
            if (source != null && source.getHTTPFiles().length > 0) {
                samplerProxy.getBody().setBinary(new ArrayList<>());
                samplerProxy.getBody().setType(Body.FORM_DATA);
                List<KeyValue> keyValues = new LinkedList<>();
                for (HTTPFileArg arg : source.getHTTPFiles()) {
                    KeyValue keyValue = new KeyValue(arg.getProperty("Argument.name").toString(), arg.getProperty("Argument.value").toString());
                    keyValue.setContentType(arg.getProperty("HTTPArgument.content_type").toString());
                    keyValues.add(keyValue);
                }
                samplerProxy.getBody().setKvs(keyValues);
            }
            samplerProxy.setProtocol(RequestType.HTTP);
            if (source.getArguments() != null) {
                List<KeyValue> keyValues = new LinkedList<>();
                source.getArguments().getArgumentsAsMap().forEach((k, v) -> {
                    KeyValue keyValue = new KeyValue(k, v);
                    keyValues.add(keyValue);
                });
                if (CollectionUtils.isNotEmpty(keyValues)) {
                    samplerProxy.setArguments(keyValues);
                }
            }
            samplerProxy.setPath(source.getPath());
            samplerProxy.setMethod(source.getMethod());
            if (source.getUrl() != null) {
                samplerProxy.setUrl(source.getUrl().toString());
            }
            samplerProxy.setId(UUID.randomUUID().toString());
            samplerProxy.setType("HTTPSamplerProxy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertTCPSampler(MsTCPSampler msTCPSampler, TCPSampler tcpSampler) {
        tcpSampler.setName(tcpSampler.getName());
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

    private void getTree(HashTree tree, MsTestElement scenario) {
        // 提取数据单独处理
        MsExtract extract = new MsExtract();
        extract.setType("Extract");
        extract.setJson(new LinkedList<>());
        extract.setRegex(new LinkedList<>());
        extract.setXpath(new LinkedList<>());
        // 断言规则
        MsAssertions assertions = new MsAssertions();
        assertions.setJsonPath(new LinkedList<>());
        assertions.setJsr223(new LinkedList<>());
        assertions.setXpath2(new LinkedList<>());
        assertions.setType("Assertions");
        for (Object key : tree.keySet()) {
            MsTestElement elementNode = null;
            if (CollectionUtils.isEmpty(scenario.getHashTree())) {
                scenario.setHashTree(new LinkedList<>());
            }
            if (key instanceof TestPlan) {
                scenario.setName(((TestPlan) key).getName());
                elementNode = new MsJmeterElement();
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(key));
                elementNode.setName(jsonObject.get("name") == null ? "" : jsonObject.get("name").toString());
                ((MsJmeterElement) elementNode).setJmeterElement(key);
            } else if (key instanceof ThreadGroup) {
                elementNode = new MsScenario(((ThreadGroup) key).getName());
            } else if (key instanceof HTTPSamplerProxy) {
                elementNode = new MsHTTPSamplerProxy();
                ((MsHTTPSamplerProxy) elementNode).setBody(new Body());
                convertHttpSampler((MsHTTPSamplerProxy) elementNode, (HTTPSamplerProxy) key);
            } else if (key instanceof TCPSampler) {
                elementNode = new MsTCPSampler();
                TCPSampler tcpSampler = (TCPSampler) key;
                convertTCPSampler((MsTCPSampler) elementNode, tcpSampler);
            } else if (key instanceof DubboSample) {
                DubboSample sampler = (DubboSample) key;
                elementNode = new MsDubboSampler();
                elementNode.setType("DubboSampler");
                ((MsDubboSampler) elementNode).setProtocol("dubbo://");
                ((MsDubboSampler) elementNode).set_interface(sampler.getPropertyAsString("FIELD_DUBBO_INTERFACE"));
                ((MsDubboSampler) elementNode).setMethod(sampler.getPropertyAsString("FIELD_DUBBO_METHOD"));

                MsConfigCenter configCenter = new MsConfigCenter();
                configCenter.setProtocol(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_PROTOCOL"));
                configCenter.setGroup(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_GROUP"));
                configCenter.setNamespace(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_NAMESPACE"));
                configCenter.setUsername(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_USER_NAME"));
                configCenter.setPassword(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_PASSWORD"));
                configCenter.setAddress(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_ADDRESS"));
                configCenter.setTimeout(sampler.getPropertyAsString("FIELD_DUBBO_CONFIG_CENTER_TIMEOUT"));
                ((MsDubboSampler) elementNode).setConfigCenter(configCenter);

                MsRegistryCenter registryCenter = new MsRegistryCenter();
                registryCenter.setProtocol(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_PROTOCOL"));
                registryCenter.setAddress(sampler.getPropertyAsString("FIELD_DUBBO_ADDRESS"));
                registryCenter.setGroup(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_GROUP"));
                registryCenter.setUsername(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_USER_NAME"));
                registryCenter.setPassword(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_PASSWORD"));
                registryCenter.setTimeout(sampler.getPropertyAsString("FIELD_DUBBO_REGISTRY_TIMEOUT"));
                ((MsDubboSampler) elementNode).setRegistryCenter(registryCenter);

                MsConsumerAndService consumerAndService = new MsConsumerAndService();
                consumerAndService.setAsync(sampler.getPropertyAsString("FIELD_DUBBO_ASYNC"));
                consumerAndService.setCluster(sampler.getPropertyAsString("FIELD_DUBBO_CLUSTER"));
                consumerAndService.setConnections(sampler.getPropertyAsString("FIELD_DUBBO_CONNECTIONS"));
                consumerAndService.setGroup(sampler.getPropertyAsString("FIELD_DUBBO_GROUP"));
                consumerAndService.setLoadBalance(sampler.getPropertyAsString("FIELD_DUBBO_LOADBALANCE"));
                consumerAndService.setVersion(sampler.getPropertyAsString("FIELD_DUBBO_VERSION"));
                consumerAndService.setTimeout(sampler.getPropertyAsString("FIELD_DUBBO_TIMEOUT"));
                ((MsDubboSampler) elementNode).setConsumerAndService(consumerAndService);

                List<MethodArgument> methodArguments = Constants.getMethodArgs(sampler);
                if (CollectionUtils.isNotEmpty(methodArguments)) {
                    List<KeyValue> methodArgs = new LinkedList<>();
                    methodArguments.forEach(item -> {
                        KeyValue keyValue = new KeyValue(item.getParamType(), item.getParamValue());
                        methodArgs.add(keyValue);
                    });
                    ((MsDubboSampler) elementNode).setArgs(methodArgs);
                }

                List<MethodArgument> arguments = Constants.getAttachmentArgs(sampler);
                if (CollectionUtils.isNotEmpty(arguments)) {
                    List<KeyValue> methodArgs = new LinkedList<>();
                    arguments.forEach(item -> {
                        KeyValue keyValue = new KeyValue(item.getParamType(), item.getParamValue());
                        methodArgs.add(keyValue);
                    });
                    ((MsDubboSampler) elementNode).setAttachmentArgs(methodArgs);
                }

            } else if (key instanceof JDBCSampler) {
                MsJDBCSampler msJDBCSampler = new MsJDBCSampler();
                JDBCSampler jdbcSampler = (JDBCSampler) key;
                msJDBCSampler.setType("JDBCSampler");
                msJDBCSampler.setName(jdbcSampler.getName());
                msJDBCSampler.setProtocol("SQL");
                msJDBCSampler.setQuery(jdbcSampler.getQuery());
                msJDBCSampler.setQueryTimeout(Long.parseLong(jdbcSampler.getQueryTimeout()));
                msJDBCSampler.setResultVariable(jdbcSampler.getResultVariable());
                msJDBCSampler.setVariableNames(jdbcSampler.getVariableNames());
                elementNode = msJDBCSampler;
            } else if (key instanceof JSR223Sampler) {
                JSR223Sampler jsr223Sampler = (JSR223Sampler) key;
                elementNode = new MsJSR223Processor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
            } else if (key instanceof JSR223PostProcessor) {
                JSR223PostProcessor jsr223Sampler = (JSR223PostProcessor) key;
                elementNode = new MsJSR223PostProcessor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
            } else if (key instanceof JSR223PreProcessor) {
                JSR223PreProcessor jsr223Sampler = (JSR223PreProcessor) key;
                elementNode = new MsJSR223PreProcessor();
                BeanUtils.copyBean(elementNode, jsr223Sampler);
            } else if (key instanceof ResponseAssertion) {
                MsAssertionRegex assertionRegex = new MsAssertionRegex();
                ResponseAssertion assertion = (ResponseAssertion) key;
                assertionRegex.setDescription(assertion.getName());
                assertionRegex.setAssumeSuccess(assertion.getAssumeSuccess());
                assertionRegex.setExpression(assertion.getTestStrings().getStringValue());
                if (assertion.isTestFieldRequestData()) {
                    assertionRegex.setSubject("Response Data");
                }
                if (assertion.isTestFieldResponseCode()) {
                    assertionRegex.setSubject("Response Code");
                }
                if (assertion.isTestFieldRequestHeaders()) {
                    assertionRegex.setSubject("Response Headers");
                }
                assertions.getRegex().add(assertionRegex);
            } else if (key instanceof JSONPathAssertion) {
                MsAssertionJsonPath assertionJsonPath = new MsAssertionJsonPath();
                JSONPathAssertion jsonPathAssertion = (JSONPathAssertion) key;
                assertionJsonPath.setDescription(jsonPathAssertion.getName());
                assertionJsonPath.setExpression(jsonPathAssertion.getJsonPath());
                assertionJsonPath.setExpect(jsonPathAssertion.getExpectedValue());
                assertions.getJsonPath().add(assertionJsonPath);
            } else if (key instanceof XPath2Assertion) {
                MsAssertionXPath2 assertionXPath2 = new MsAssertionXPath2();
                XPath2Assertion xPath2Assertion = (XPath2Assertion) key;
                assertionXPath2.setExpression(xPath2Assertion.getXPathString());
                assertions.getXpath2().add(assertionXPath2);
            } else if (key instanceof JSR223Assertion) {
                MsAssertionJSR223 msAssertionJSR223 = new MsAssertionJSR223();
                JSR223Assertion jsr223Assertion = (JSR223Assertion) key;
                msAssertionJSR223.setName(jsr223Assertion.getName());
                msAssertionJSR223.setScript(jsr223Assertion.getScript());
                msAssertionJSR223.setScriptLanguage(jsr223Assertion.getScriptLanguage());
                assertions.getJsr223().add(msAssertionJSR223);
            } else if (key instanceof DurationAssertion) {
                MsAssertionDuration assertionDuration = new MsAssertionDuration();
                DurationAssertion durationAssertion = (DurationAssertion) key;
                assertionDuration.setValue(durationAssertion.getProperty("DurationAssertion.duration").getIntValue());
                assertions.setDuration(assertionDuration);
            } else if (key instanceof RegexExtractor) {
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
                regex.setType("Extract");
                regex.setExpression(regexExtractor.getRegex());
                regex.setVariable(regexExtractor.getRefName());
                extract.setName(regexExtractor.getName());
                extract.getRegex().add(regex);
            } else if (key instanceof XPath2Extractor) {
                XPath2Extractor xPath2Extractor = (XPath2Extractor) key;
                MsExtractXPath xPath = new MsExtractXPath();
                xPath.setVariable(xPath2Extractor.getRefName());
                xPath.setExpression(xPath2Extractor.getXPathQuery());
                xPath.setType("Extract");
                extract.getXpath().add(xPath);
            } else if (key instanceof JSONPostProcessor) {
                JSONPostProcessor jsonPostProcessor = (JSONPostProcessor) key;
                MsExtractJSONPath jsonPath = new MsExtractJSONPath();
                jsonPath.setVariable(jsonPostProcessor.getRefNames());
                jsonPath.setExpression(jsonPostProcessor.getJsonPathExpressions());
                jsonPath.setType("Extract");
                extract.getJson().add(jsonPath);
            } else if (key instanceof ConstantTimer) {
                elementNode = new MsConstantTimer();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("ConstantTimer");
            } else if (key instanceof IfController) {
                elementNode = new MsIfController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("IfController");
            } else if (key instanceof LoopController) {
                elementNode = new MsLoopController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("LoopController");
                ((MsLoopController) elementNode).setLoopType(LoopConstants.LOOP_COUNT.name());
                LoopController loopController = (LoopController) key;
                CountController countController = new CountController();
                countController.setLoops(loopController.getLoops());
                countController.setProceed(true);
                ((MsLoopController) elementNode).setCountController(countController);
            } else if (key instanceof WhileController) {
                elementNode = new MsLoopController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("LoopController");
                ((MsLoopController) elementNode).setLoopType(LoopConstants.WHILE.name());
                WhileController whileController = (WhileController) key;
                MsWhileController countController = new MsWhileController();
                countController.setValue(whileController.getCondition());
                ((MsLoopController) elementNode).setWhileController(countController);
            } else if (key instanceof ForeachController) {
                elementNode = new MsLoopController();
                BeanUtils.copyBean(elementNode, key);
                elementNode.setType("LoopController");
                ((MsLoopController) elementNode).setLoopType(LoopConstants.FOREACH.name());
                ForeachController foreachController = (ForeachController) key;
                MsForEachController countController = new MsForEachController();
                countController.setInputVal(foreachController.getInputValString());
                countController.setReturnVal(foreachController.getReturnValString());
                ((MsLoopController) elementNode).setForEachController(countController);
            } else {
                elementNode = new MsJmeterElement();
                elementNode.setType("JmeterElement");
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(key));
                elementNode.setName(jsonObject.get("name") == null ? "" : jsonObject.get("name").toString());
                ((MsJmeterElement) elementNode).setJmeterElement(key);
            }
            //提取参数
            if (CollectionUtils.isNotEmpty(extract.getJson()) || CollectionUtils.isNotEmpty(extract.getRegex()) || CollectionUtils.isNotEmpty(extract.getXpath())) {
                elementNode = extract;
            }
            //断言规则
            if (CollectionUtils.isNotEmpty(assertions.getRegex()) || CollectionUtils.isNotEmpty(assertions.getJsonPath())
                    || CollectionUtils.isNotEmpty(assertions.getJsr223()) || CollectionUtils.isNotEmpty(assertions.getXpath2()) || assertions.getDuration() != null) {
                elementNode = assertions;
            }

            elementNode.setResourceId(UUID.randomUUID().toString());
            elementNode.setId(UUID.randomUUID().toString());
            scenario.getHashTree().add(elementNode);
            HashTree node = tree.get(key);
            if (node != null) {
                getTree(node, elementNode);
            }
        }
    }
}
