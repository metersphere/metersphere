package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample;
import io.github.ningyu.jmeter.plugin.dubbo.sample.MethodArgument;
import io.github.ningyu.jmeter.plugin.util.Constants;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsConfigCenter;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsConsumerAndService;
import io.metersphere.api.dto.definition.request.sampler.dubbo.MsRegistryCenter;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.constants.MsTestElementConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "DubboSampler")
public class MsDubboSampler extends MsTestElement {
    // type 必须放最前面，以便能够转换正确的类
    private String type = "DubboSampler";

    @JSONField(ordinal = 52)
    private final String protocol = "dubbo://";
    @JsonProperty(value = "interface")
    @JSONField(ordinal = 53, name = "interface")
    private String _interface;
    @JSONField(ordinal = 54)
    private String method;

    @JSONField(ordinal = 55)
    private MsConfigCenter configCenter;
    @JSONField(ordinal = 56)
    private MsRegistryCenter registryCenter;
    @JSONField(ordinal = 57)
    private MsConsumerAndService consumerAndService;

    @JSONField(ordinal = 58)
    private List<KeyValue> args;
    @JSONField(ordinal = 59)
    private List<KeyValue> attachmentArgs;

//    @JSONField(ordinal = 60)
//    private Object requestResult;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (this.getReferenced() != null && "Deleted".equals(this.getReferenced())) {
            return;
        }
        if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            this.getRefElement(this);
        }

        final HashTree testPlanTree = tree.add(dubboSample(config));
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(testPlanTree, el.getHashTree(), config);
            });
        }
    }

    private DubboSample dubboSample(ParameterConfig config) {
        DubboSample sampler = new DubboSample();
        sampler.setEnabled(this.isEnable());
        sampler.setName(this.getName());
        String name = this.getParentName(this.getParent(), config);
        if (StringUtils.isNotEmpty(name) && !config.isOperating()) {
            sampler.setName(this.getName() + "<->" + name);
        }
        sampler.setProperty(TestElement.TEST_CLASS, DubboSample.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("DubboSampleGui"));

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
        if (configCenter != null && configCenter.getProtocol() != null && configCenter.getUsername() != null && configCenter.getPassword() != null) {
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
