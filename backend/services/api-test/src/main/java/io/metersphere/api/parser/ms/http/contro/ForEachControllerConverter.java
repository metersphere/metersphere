package io.metersphere.api.parser.ms.http.contro;

import io.metersphere.api.dto.request.controller.LoopType;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.request.controller.loop.MsCountController;
import io.metersphere.api.dto.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.request.controller.loop.MsWhileController;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.control.ForeachController;
import org.apache.jorphan.collections.HashTree;

public class ForEachControllerConverter extends AbstractMsElementConverter<ForeachController> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, ForeachController element, HashTree hashTree) {
        MsLoopController msLoopController = new MsLoopController();
        msLoopController.setLoopType(LoopType.FOREACH.name());
        MsForEachController controller = new MsForEachController();
        controller.setValue(element.getInputValString());

        msLoopController.setForEachController(controller);
        msLoopController.setMsCountController(new MsCountController());
        msLoopController.setWhileController(new MsWhileController());

        parent.getChildren().add(msLoopController);

        parseChild(msLoopController, element, hashTree);
    }
}
