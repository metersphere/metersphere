package io.metersphere.api.parser.ms.http.contro;

import io.metersphere.api.dto.request.controller.MsIfController;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.MsAssertionCondition;
import org.apache.jmeter.control.IfController;
import org.apache.jorphan.collections.HashTree;

public class IfControllerConverter extends AbstractMsElementConverter<IfController> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, IfController element, HashTree hashTree) {
        MsIfController msIfController = new MsIfController();
        msIfController.setCondition(MsAssertionCondition.EMPTY.name());
        msIfController.setVariable(element.getCondition());
        parent.getChildren().add(msIfController);

        parseChild(msIfController, element, hashTree);
    }
}
