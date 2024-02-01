package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.project.api.processor.TimeWaitingProcessor;
import io.metersphere.plugin.api.dto.ParameterConfig;
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
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(processor.getEnable());
        constantTimer.setName(processor.getDelay() + " ms");
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(CONSTANT_TIMER_GUI));
        constantTimer.setDelay(processor.getDelay().toString());
        hashTree.add(constantTimer);
    }
}
