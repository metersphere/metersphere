package io.metersphere.api.parser.ms.http.pre;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.ScriptProcessor;
import jodd.util.StringUtil;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jorphan.collections.HashTree;

public class JSR223PreProcessConverter extends AbstractMsElementConverter<JSR223PreProcessor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, JSR223PreProcessor element, HashTree hashTree) {

        ScriptProcessor msScriptElement = new ScriptProcessor();
        msScriptElement.setScriptLanguage(element.getScriptLanguage());
        if (StringUtil.isBlank(msScriptElement.getScriptLanguage())) {
            msScriptElement.setScriptLanguage(element.getPropertyAsString("scriptLanguage"));
        }
        msScriptElement.setEnable(element.isEnabled());
        msScriptElement.setName(element.getPropertyAsString("TestElement.name"));
        msScriptElement.setScript(element.getPropertyAsString("script"));

        ConverterUtils.addPreProcess(parent, msScriptElement);
    }
}
