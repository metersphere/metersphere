package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.TimeWaitingProcessor;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;

public class ConstantTimerConverter extends AbstractMsElementConverter<ConstantTimer> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, ConstantTimer element, HashTree hashTree) {
        TimeWaitingProcessor msProcessor = new TimeWaitingProcessor();
        msProcessor.setDelay(Long.parseLong(element.getDelay()));
        msProcessor.setEnable(element.isEnabled());
        msProcessor.setName(element.getName());
        ConverterUtils.addPreProcess(parent, msProcessor);
    }
}
