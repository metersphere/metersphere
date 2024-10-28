package io.metersphere.api.parser.ms.http.contro;

import io.metersphere.api.dto.request.controller.LoopType;
import io.metersphere.api.dto.request.controller.MsLoopController;
import io.metersphere.api.dto.request.controller.loop.MsCountController;
import io.metersphere.api.dto.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.request.controller.loop.MsWhileController;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.control.LoopController;
import org.apache.jorphan.collections.HashTree;

public class LoopControllerConverter extends AbstractMsElementConverter<LoopController> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, LoopController element, HashTree hashTree) {
        MsLoopController msLoopController = new MsLoopController();
        msLoopController.setLoopType(LoopType.LOOP_COUNT.name());

        MsCountController controller = new MsCountController();
        controller.setLoops(String.valueOf(element.getLoops()));
        msLoopController.setMsCountController(controller);
        msLoopController.setForEachController(new MsForEachController());
        msLoopController.setWhileController(new MsWhileController());
        parent.getChildren().add(msLoopController);

        parseChild(msLoopController, element, hashTree);
    }
}
