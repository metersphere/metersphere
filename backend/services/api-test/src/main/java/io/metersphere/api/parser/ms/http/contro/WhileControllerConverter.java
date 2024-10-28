package io.metersphere.api.parser.ms.http.contro;

import io.metersphere.api.dto.request.controller.LoopType;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.request.controller.loop.*;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.MsAssertionCondition;
import org.apache.jmeter.control.WhileController;
import org.apache.jorphan.collections.HashTree;

public class WhileControllerConverter extends AbstractMsElementConverter<WhileController> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, WhileController element, HashTree hashTree) {
        MsLoopController msLoopController = new MsLoopController();
        msLoopController.setLoopType(LoopType.WHILE.name());

        MsWhileController controller = new MsWhileController();
        controller.setMsWhileVariable(new MsWhileVariable());
        controller.setMsWhileScript(new MsWhileScript());
        controller.getMsWhileVariable().setVariable(element.getCondition());
        controller.getMsWhileVariable().setCondition(MsAssertionCondition.EMPTY.name());

        msLoopController.setWhileController(controller);
        msLoopController.setMsCountController(new MsCountController());
        msLoopController.setForEachController(new MsForEachController());
        parent.getChildren().add(msLoopController);

        parseChild(msLoopController, element, hashTree);
    }
}
