package io.metersphere.api.parser.jmeter;

import io.metersphere.api.dto.request.controller.MsConstantTimerController;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverter;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverterFactory;
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
        MsProcessorConverter timeWaitingConverter = MsProcessorConverterFactory.getPostConverter(TimeWaitingProcessor.class);
        TimeWaitingProcessor timeWaitingProcessor = BeanUtils.copyBean(new TimeWaitingProcessor(), element);
        timeWaitingConverter.parse(tree, timeWaitingProcessor, config);
    }
}
