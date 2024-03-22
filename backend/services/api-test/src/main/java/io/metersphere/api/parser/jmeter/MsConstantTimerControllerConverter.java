package io.metersphere.api.parser.jmeter;

import io.metersphere.api.dto.request.controller.MsConstantTimerController;
import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

public class MsConstantTimerControllerConverter extends AbstractJmeterElementConverter<MsConstantTimerController> {


    @Override
    public void toHashTree(HashTree tree, MsConstantTimerController element, ParameterConfig config) {
        if (BooleanUtils.isFalse(element.getEnable())) {
            LogUtils.info("MsConstantTimerController is disabled");
            return;
        }
        HashTree groupTree = tree.add(constantTimer(element));
        parseChild(groupTree, element, config);
    }

    private DebugSampler constantTimer(MsConstantTimerController element) {
        // 添加 debugSampler 步骤间生效
        DebugSampler debugSampler = new DebugSampler();
        debugSampler.setEnabled(true);
        debugSampler.setName(ElementProperty.SCENARIO_CONSTANT_TIMER.name());
        debugSampler.setProperty(TestElement.TEST_CLASS, DebugSampler.class.getName());
        debugSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));

        // 上面三行直接Set属性会导致DebugSampler构建时取不到值，可能是JMeter的Bug,需要SetProperty
        debugSampler.setProperty("displayJMeterProperties", false);
        debugSampler.setProperty("displayJMeterVariables", false);
        debugSampler.setProperty("displaySystemProperties", false);

        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(element.getEnable());
        constantTimer.setName(element.getName() + " ms");
        constantTimer.setProperty(TestElement.TEST_CLASS, ConstantTimer.class.getName());
        constantTimer.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ConstantTimerGui"));
        constantTimer.setDelay(element.getDelay());
        debugSampler.addTestElement(constantTimer);
        return debugSampler;
    }

}
