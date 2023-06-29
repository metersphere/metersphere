package io.metersphere.api.dto.definition.request.sampler;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample;
import io.github.ningyu.jmeter.plugin.dubbo.sample.MethodArgument;
import io.github.ningyu.jmeter.plugin.util.Constants;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsConfigCenter;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsConsumerAndService;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsRegistryCenter;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.GlobalScriptFilterRequest;
import io.metersphere.api.parse.api.JMeterScriptUtil;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsDubboSampler extends MsTestElement {
    private String clazzName = MsDubboSampler.class.getCanonicalName();
    /**
     * type 必须放最前面，以便能够转换正确的类
     */
    private String type = ElementConstants.DUBBO_SAMPLER;
    private final String protocol = "dubbo://";
    @JsonProperty(value = "interface")
    private String _interface;
    private String method;
    private MsConfigCenter configCenter;
    private MsRegistryCenter registryCenter;
    private MsConsumerAndService consumerAndService;
    private List<KeyValue> args;
    private List<KeyValue> attachmentArgs;
    private String useEnvironment;
    private boolean customizeReq;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        } else if (config.isOperating() && StringUtils.isNotEmpty(config.getOperatingSampleTestName())) {
            this.setName(config.getOperatingSampleTestName());
        }
        if (this.getReferenced() != null && "Deleted".equals(this.getReferenced())) {
            return;
        }

        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            boolean ref = this.setRefElement();
            if (!ref) {
                return;
            }
            hashTree = this.getHashTree();
        }
        // 失败重试
        HashTree testPlanTree;
        if (config.getRetryNum() > 0 && !ElementUtil.isLoop(this.getParent())) {
            final HashTree loopTree = ElementUtil.retryHashTree(this.getName(), config.getRetryNum(), tree);
            testPlanTree = loopTree.add(dubboSample(config));
        } else {
            testPlanTree = tree.add(dubboSample(config));
        }
        //添加全局前后置脚本
        EnvironmentConfig envConfig = null;
        if (config.getConfig() != null) {
            envConfig = config.getConfig().get(this.getProjectId());
        }
        //处理全局前后置脚本(步骤内)
        String environmentId = this.getEnvironmentId();
        if (environmentId == null) {
            if (StringUtils.isEmpty(this.useEnvironment) && envConfig != null) {
                environmentId = envConfig.getEnvironmentId();
            } else {
                environmentId = this.useEnvironment;
            }
        }
        //根据配置将脚本放置在私有脚本之前
        JMeterScriptUtil.setScriptByEnvironmentConfig(envConfig, testPlanTree, GlobalScriptFilterRequest.DUBBO.name(), environmentId, config, false);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree = ElementUtil.order(hashTree);
            hashTree.forEach(el -> {
                el.toHashTree(testPlanTree, el.getHashTree(), config);
            });
        }

        //根据配置将脚本放置在私有脚本之后
        JMeterScriptUtil.setScriptByEnvironmentConfig(envConfig, testPlanTree, GlobalScriptFilterRequest.DUBBO.name(), environmentId, config, true);
    }

    private boolean setRefElement() {
        try {
            MsDubboSampler proxy = null;
            if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
                if (bloBs != null) {
                    this.setProjectId(bloBs.getProjectId());
                    JSONObject element = JSONUtil.parseObject(bloBs.getRequest());
                    ElementUtil.dataFormatting(element);
                    proxy = JSONUtil.parseObject(element.toString(), MsDubboSampler.class);
                    this.setName(bloBs.getName());
                }
            } else {
                ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
                if (apiDefinition != null) {
                    this.setProjectId(apiDefinition.getProjectId());
                    proxy = JSONUtil.parseObject(apiDefinition.getRequest(), MsDubboSampler.class);
                    this.setName(apiDefinition.getName());
                }
            }
            if (proxy != null) {
                if (StringUtils.equals(this.getRefType(), CommonConstants.CASE)) {
                    ElementUtil.mergeHashTree(this, proxy.getHashTree());
                } else {
                    this.setHashTree(proxy.getHashTree());
                }
                this.setMethod(proxy.getMethod());
                this.set_interface(proxy.get_interface());
                this.setAttachmentArgs(proxy.getAttachmentArgs());
                this.setArgs(proxy.getArgs());
                this.setConsumerAndService(proxy.getConsumerAndService());
                this.setRegistryCenter(proxy.getRegistryCenter());
                this.setConfigCenter(proxy.getConfigCenter());
                return true;
            }
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return false;
    }

    private DubboSample dubboSample(ParameterConfig config) {
        DubboSample sampler = new DubboSample();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        if (StringUtils.isEmpty(this.getName())) {
            sampler.setName("DubboSamplerProxy");
        }
        if (config.isOperating()) {
            String[] testNameArr = sampler.getName().split("<->");
            if (testNameArr.length > 0) {
                String testName = testNameArr[0];
                sampler.setName(testName);
            }
        }
        sampler.setProperty(TestElement.TEST_CLASS, DubboSample.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DubboSampleGui"));
        ElementUtil.setBaseParams(sampler, this.getParent(), config, this.getId(), this.getIndex());
        sampler.addTestElement(configCenter(this.getConfigCenter()));
        sampler.addTestElement(registryCenter(this.getRegistryCenter()));
        sampler.addTestElement(consumerAndService(this.getConsumerAndService()));

        Constants.setRpcProtocol(this.getProtocol(), sampler);
        Constants.setInterfaceName(this.get_interface(), sampler);
        Constants.setMethod(this.getMethod(), sampler);

        if (CollectionUtils.isNotEmpty(this.getArgs())) {
            List<MethodArgument> methodArgs = this.getArgs().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable)
                    .map(keyValue -> new MethodArgument(keyValue.getName(), keyValue.getValue())).collect(Collectors.toList());
            Constants.setMethodArgs(methodArgs, sampler);
        }
        if (CollectionUtils.isNotEmpty(this.getAttachmentArgs())) {
            List<MethodArgument> attachmentArgs = this.getAttachmentArgs().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable)
                    .map(keyValue -> new MethodArgument(keyValue.getName(), keyValue.getValue())).collect(Collectors.toList());
            Constants.setAttachmentArgs(attachmentArgs, sampler);
        }
        return sampler;
    }

    private ConfigTestElement configCenter(MsConfigCenter configCenter) {
        ConfigTestElement configTestElement = new ConfigTestElement();
        if (configCenter != null && StringUtils.isNotEmpty(configCenter.getAddress()) && StringUtils.isNotEmpty(configCenter.getProtocol()) && StringUtils.isNotEmpty(configCenter.getUsername()) && StringUtils.isNotEmpty(configCenter.getPassword())) {
            Constants.setConfigCenterProtocol(configCenter.getProtocol(), configTestElement);
            Constants.setConfigCenterGroup(configCenter.getGroup(), configTestElement);
            Constants.setConfigCenterNamespace(configCenter.getNamespace(), configTestElement);
            Constants.setConfigCenterUserName(configCenter.getUsername(), configTestElement);
            Constants.setConfigCenterPassword(configCenter.getPassword(), configTestElement);
            Constants.setConfigCenterAddress(configCenter.getAddress(), configTestElement);
            Constants.setConfigCenterTimeout(configCenter.getTimeout(), configTestElement);
        }
        return configTestElement;
    }

    private ConfigTestElement registryCenter(MsRegistryCenter registryCenter) {
        ConfigTestElement configTestElement = new ConfigTestElement();
        if (registryCenter != null) {
            Constants.setRegistryProtocol(registryCenter.getProtocol(), configTestElement);
            Constants.setRegistryGroup(registryCenter.getGroup(), configTestElement);
            Constants.setRegistryUserName(registryCenter.getUsername(), configTestElement);
            Constants.setRegistryPassword(registryCenter.getPassword(), configTestElement);
            Constants.setRegistryTimeout(registryCenter.getTimeout(), configTestElement);
            Constants.setAddress(registryCenter.getAddress(), configTestElement);
        }
        return configTestElement;
    }

    private ConfigTestElement consumerAndService(MsConsumerAndService consumerAndService) {
        ConfigTestElement configTestElement = new ConfigTestElement();
        if (consumerAndService != null) {
            Constants.setTimeout(consumerAndService.getTimeout(), configTestElement);
            Constants.setVersion(consumerAndService.getVersion(), configTestElement);
            Constants.setGroup(consumerAndService.getGroup(), configTestElement);
            Constants.setConnections(consumerAndService.getConnections(), configTestElement);
            Constants.setLoadbalance(consumerAndService.getLoadBalance(), configTestElement);
            Constants.setAsync(consumerAndService.getAsync(), configTestElement);
            Constants.setCluster(consumerAndService.getCluster(), configTestElement);
        }
        return configTestElement;
    }

}
