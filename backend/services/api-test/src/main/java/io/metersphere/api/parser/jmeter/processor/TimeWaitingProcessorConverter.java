package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.TimeWaitingProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.CONSTANT_TIMER_GUI;


/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class TimeWaitingProcessorConverter extends MsProcessorConverter<TimeWaitingProcessor> {
    @Override
    public void parse(HashTree hashTree, TimeWaitingProcessor processor, ParameterConfig config) {
        hashTree.add(getConstantTimerElement(),getConstantTimer(processor));
    }

    private DebugSampler getConstantTimerElement() {
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

    public static ConstantTimer getConstantTimer(TimeWaitingProcessor processor) {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(processor.getEnable());
        constantTimer.setName(StringUtils.isBlank(processor.getName()) ? (processor.getDelay() + " ms") : processor.getName());
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(CONSTANT_TIMER_GUI));
        constantTimer.setDelay(processor.getDelay().toString());
        return constantTimer;
    }
}
