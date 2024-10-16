package io.metersphere.api.parser.ms;

import io.metersphere.api.dto.request.MsJMeterComponent;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsProtocolTestElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayOutputStream;

public class JmeterGeneralElementConverter extends AbstractMsElementConverter<TestElement> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, TestElement element, HashTree hashTree) {
        if (parent instanceof AbstractMsProtocolTestElement) {
            //  在请求类型的组件下，如果存在未知子组件，暂时不解析
            return;
        }
        MsJMeterComponent msJMeterComponent = new MsJMeterComponent();
        msJMeterComponent.setName(element.getName());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SaveService.saveElement(element, baos);
            msJMeterComponent.setTestElementContent(baos.toString());
        } catch (Exception ignore) {
        }
        parent.getChildren().add(msJMeterComponent);
        parseChild(msJMeterComponent, element, hashTree);
    }
}
