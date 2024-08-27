package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.constants.ScriptLanguageType;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jorphan.collections.HashTree;

public class BeanShellPostProcessConverter extends AbstractMsElementConverter<BeanShellPostProcessor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, BeanShellPostProcessor element, HashTree hashTree) {
        ScriptProcessor msScriptElement = new ScriptProcessor();
        msScriptElement.setEnable(element.isEnabled());
        msScriptElement.setScriptLanguage(ScriptLanguageType.BEANSHELL.name());
        msScriptElement.setName(element.getPropertyAsString("TestElement.name"));
        msScriptElement.setScript(element.getPropertyAsString("script"));
        ConverterUtils.addPostProcess(parent, msScriptElement);
    }
}
