package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.controller.loop.CountController;
import io.metersphere.api.dto.definition.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.definition.request.controller.loop.MsWhileController;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
import io.metersphere.api.dto.definition.request.extract.MsExtractRegex;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
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
            } else if (key instanceof MsDubboSampler) {

            } else if (key instanceof MsJDBCSampler) {

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
            } else if (key instanceof MsAssertions) {

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
                regex.setType("Regex");
                regex.setExpression(regexExtractor.getRegex());
                regex.setVariable(regexExtractor.getRefName());

            } else if (key instanceof XPath2Extractor) {

            } else if (key instanceof JSONPostProcessor) {

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
