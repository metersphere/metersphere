package io.metersphere.api.parser.jmeter.controller;

import io.metersphere.api.dto.request.controller.MsConstantTimerController;
import io.metersphere.api.parser.jmeter.processor.ScenarioTimeWaitingProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.api.processor.TimeWaitingProcessor;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jorphan.collections.HashTree;

public class MsConstantTimerControllerConverter extends AbstractJmeterElementConverter<MsConstantTimerController> {

    @Override
    public void toHashTree(HashTree tree, MsConstantTimerController element, ParameterConfig config) {
        if (BooleanUtils.isFalse(element.getEnable())) {
            LogUtils.info("MsConstantTimerController is disabled");
            return;
        }
        TimeWaitingProcessor timeWaitingProcessor = BeanUtils.copyBean(new TimeWaitingProcessor(), element);
        new ScenarioTimeWaitingProcessorConverter().parse(tree, timeWaitingProcessor, config);
    }
}
