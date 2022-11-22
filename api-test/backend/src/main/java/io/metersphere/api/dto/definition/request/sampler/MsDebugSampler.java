package io.metersphere.api.dto.definition.request.sampler;

import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class MsDebugSampler extends MsTestElement {
    private String clazzName = MsDebugSampler.class.getCanonicalName();


    private String type = ElementConstants.DEBUG_SAMPLER;

    private boolean displayJMeterProperties = false;

    private boolean displayJMeterVariables = true;

    private boolean displaySystemProperties = false;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        final HashTree groupTree = tree.add(debugSampler());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
    }


    private DebugSampler debugSampler() {
        DebugSampler debugSampler = new DebugSampler();
        debugSampler.setEnabled(this.isEnable());
        if (StringUtils.isEmpty(this.getName())) {
            this.setName(ElementConstants.DEBUG_SAMPLER);
        }
        debugSampler.setName(this.getName());
        debugSampler.setProperty(TestElement.TEST_CLASS, DebugSampler.class.getName());
        debugSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));

        debugSampler.setDisplaySystemProperties(this.displaySystemProperties);
        debugSampler.setDisplayJMeterVariables(this.displayJMeterVariables);
        debugSampler.setDisplayJMeterProperties(this.displayJMeterProperties);

        //上面三行直接Set属性会导致DebugSampler构建时取不到值，可能是JMeter的Bug,需要SetProperty
        debugSampler.setProperty("displayJMeterProperties", this.displayJMeterProperties);
        debugSampler.setProperty("displayJMeterVariables", this.displayJMeterVariables);
        debugSampler.setProperty("displaySystemProperties", this.displaySystemProperties);
        return debugSampler;
    }

}
