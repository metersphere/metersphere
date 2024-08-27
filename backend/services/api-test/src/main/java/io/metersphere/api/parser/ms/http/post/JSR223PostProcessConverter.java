package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.ScriptProcessor;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jorphan.collections.HashTree;

public class JSR223PostProcessConverter extends AbstractMsElementConverter<JSR223PostProcessor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, JSR223PostProcessor element, HashTree hashTree) {
        ScriptProcessor msScriptElement = new ScriptProcessor();
        msScriptElement.setScriptLanguage(element.getScriptLanguage());
        msScriptElement.setEnable(element.isEnabled());
        msScriptElement.setName(element.getPropertyAsString("TestElement.name"));
        msScriptElement.setScript(element.getPropertyAsString("script"));
        ConverterUtils.addPostProcess(parent, msScriptElement);
    }
}
