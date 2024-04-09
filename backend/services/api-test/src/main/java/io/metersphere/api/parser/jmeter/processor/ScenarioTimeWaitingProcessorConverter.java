package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.TimeWaitingProcessor;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;


/**
 *
 * 场景的的等待时间，需要包一层 debugSampler
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class ScenarioTimeWaitingProcessorConverter extends TimeWaitingProcessorConverter {
    @Override
    public void parse(HashTree hashTree, TimeWaitingProcessor processor, ParameterConfig config) {
        DebugSampler constantTimerDebugSampler = getConstantTimerDebugSampler();
        constantTimerDebugSampler.setProperty(ElementProperty.MS_STEP_ID.name(), processor.getStepId());
        hashTree.add(constantTimerDebugSampler, getConstantTimer(processor));
    }

    private DebugSampler getConstantTimerDebugSampler() {
        // 添加 debugSampler 步骤间生效
        DebugSampler debugSampler = new DebugSampler();
        debugSampler.setEnabled(true);
        debugSampler.setName(ElementProperty.SCENARIO_CONSTANT_TIMER.name());
        debugSampler.setProperty(TestElement.TEST_CLASS, DebugSampler.class.getName());
        debugSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.TEST_BEAN_GUI));

        // 下面三行直接Set属性会导致DebugSampler构建时取不到值，可能是JMeter的Bug,需要SetProperty
        debugSampler.setProperty("displayJMeterProperties", false);
        debugSampler.setProperty("displayJMeterVariables", false);
        debugSampler.setProperty("displaySystemProperties", false);

        return debugSampler;
    }
}
