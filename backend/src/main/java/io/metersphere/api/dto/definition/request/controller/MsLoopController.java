package io.metersphere.api.dto.definition.request.controller;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.controller.loop.CountController;
import io.metersphere.api.dto.definition.request.controller.loop.MsForEachController;
import io.metersphere.api.dto.definition.request.controller.loop.MsWhileController;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "LoopController")
public class MsLoopController extends MsTestElement {
    private String type = "LoopController";
    @JSONField(ordinal = 20)
    private String loopType;

    @JSONField(ordinal = 21)
    private CountController countController;

    @JSONField(ordinal = 22)
    private MsForEachController forEachController;

    @JSONField(ordinal = 23)
    private MsWhileController whileController;

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        GenericController controller = controller();
        if (controller == null)
            return;

        final HashTree groupTree = tree.add(controller);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(groupTree, el.getHashTree(), config);
            });
        }
    }

    private LoopController loopController() {
        LoopController loopController = new LoopController();
        loopController.setEnabled(true);
        loopController.setName(this.getLabel());
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("LoopControlPanel"));
        loopController.setContinueForever(countController.isProceed());
        loopController.setLoops(countController.getLoops());
        return loopController;
    }

    public String getCondition() {
        String variable = "\"" + this.whileController.getVariable() + "\"";
        String operator = this.whileController.getOperator();
        String value = "\"" + this.whileController.getValue() + "\"";

        if (StringUtils.contains(operator, "~")) {
            value = "\".*" + this.whileController.getValue() + ".*\"";
        }

        if (StringUtils.equals(operator, "is empty")) {
            variable = "empty(" + variable + ")";
            operator = "";
            value = "";
        }

        if (StringUtils.equals(operator, "is not empty")) {
            variable = "!empty(" + variable + ")";
            operator = "";
            value = "";
        }
        return "${__jexl3(" + variable + operator + value + ")}";
    }

    private WhileController whileController() {
        WhileController controller = new WhileController();
        controller.setEnabled(true);
        controller.setName(this.getLabel());
        controller.setProperty(TestElement.TEST_CLASS, WhileController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("WhileControllerGui"));
        controller.setCondition(getCondition());
        return controller;
    }

    private ForeachController foreachController() {
        ForeachController controller = new ForeachController();
        controller.setEnabled(true);
        controller.setName(this.getLabel());
        controller.setProperty(TestElement.TEST_CLASS, ForeachController.class.getName());
        controller.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ForeachControlPanel"));
        controller.setInputVal(this.forEachController.getInputVal());
        controller.setReturnVal(this.forEachController.getReturnVal());
        controller.setUseSeparator(true);
        return controller;
    }

    private GenericController controller() {
        if (StringUtils.equals(this.loopType, "WHILE") && this.whileController != null) {
            return whileController();
        }
        if (StringUtils.equals(this.loopType, "FOREACH") && this.forEachController != null) {
            return foreachController();
        }
        if (StringUtils.equals(this.loopType, "LOOP_COUNT") && this.countController != null) {
            return loopController();
        }
        return null;
    }

}
