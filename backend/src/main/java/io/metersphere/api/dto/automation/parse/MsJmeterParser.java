package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
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
import io.metersphere.base.domain.TestPlan;
import io.metersphere.commons.utils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
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
            scenario.setHashTree(new LinkedList<>());
            getTree(testPlan, scenario);


            ScenarioImport scenarioImport = new ScenarioImport();
            scenarioImport.setData(paseObj(scenario, request));
            scenarioImport.setProjectid(request.getProjectId());
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

    private void getTree(HashTree tree, MsScenario scenario) {
        for (Object key : tree.keySet()) {
            if (key instanceof TestPlan) {
                scenario.setName(((TestPlan) key).getName());
            } else if (key instanceof ThreadGroup) {
                MsScenario msScenario = new MsScenario(((ThreadGroup) key).getName());
                if (CollectionUtils.isEmpty(scenario.getHashTree())) {
                    List<MsTestElement> msTestElementList = new LinkedList<>();
                    msTestElementList.add(msScenario);
                }
                scenario.getHashTree().add(msScenario);
            } else if (key instanceof HTTPSamplerProxy) {
                MsHTTPSamplerProxy element = new MsHTTPSamplerProxy();
                element.setBody(new Body());
                HTTPSamplerProxy request = (HTTPSamplerProxy) key;
                convertHttpSampler(element, request);
                scenario.getHashTree().add(element);
            } else if (key instanceof TCPSampler) {
                MsTCPSampler msTCPSampler = new MsTCPSampler();
                TCPSampler tcpSampler = (TCPSampler) key;
                convertTCPSampler(msTCPSampler, tcpSampler);
                scenario.getHashTree().add(msTCPSampler);
            } else if (key instanceof MsDubboSampler) {

            } else if (key instanceof MsJDBCSampler) {

            } else if (key instanceof JSR223Sampler) {
                JSR223Sampler jsr223Sampler = (JSR223Sampler) key;
                MsJSR223Processor processor = new MsJSR223Processor();
                BeanUtils.copyBean(processor, jsr223Sampler);
                scenario.getHashTree().add(processor);
            } else if (key instanceof JSR223PostProcessor) {
                JSR223PostProcessor jsr223Sampler = (JSR223PostProcessor) key;
                MsJSR223PostProcessor processor = new MsJSR223PostProcessor();
                BeanUtils.copyBean(processor, jsr223Sampler);
                scenario.getHashTree().add(processor);
            } else if (key instanceof JSR223PreProcessor) {
                JSR223PreProcessor jsr223Sampler = (JSR223PreProcessor) key;
                MsJSR223PreProcessor processor = new MsJSR223PreProcessor();
                BeanUtils.copyBean(processor, jsr223Sampler);
                scenario.getHashTree().add(processor);
            } else if (key instanceof MsAssertions) {

            } else if (key instanceof MsExtract) {

            } else if (key instanceof MsConstantTimer) {

            } else if (key instanceof MsIfController) {

            } else if (key instanceof MsLoopController) {

            } else {
                MsJmeterElement jmeterElement = new MsJmeterElement();
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(key));
                jmeterElement.setName(jsonObject.get(TestElement.NAME) == null ? "" : jsonObject.get(TestElement.NAME).toString());
                jmeterElement.setJmeterElement(key);
                scenario.getHashTree().add(jmeterElement);
            }
            HashTree node = tree.get(key);
            if (node != null) {
                getTree(node, scenario);
            }
        }
    }
}
