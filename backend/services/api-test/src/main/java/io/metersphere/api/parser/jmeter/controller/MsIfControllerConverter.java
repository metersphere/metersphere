package io.metersphere.api.parser.jmeter.controller;

import io.metersphere.api.dto.request.controller.MsIfController;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

public class MsIfControllerConverter extends AbstractJmeterElementConverter<MsIfController> {


    @Override
    public void toHashTree(HashTree tree, MsIfController element, ParameterConfig config) {

        if (BooleanUtils.isFalse(element.getEnable())) {
            LogUtils.info("MsIfController is disabled");
            return;
        }

        HashTree groupTree = tree.add(ifController(element));
        parseChild(groupTree, element, config);
    }

    private IfController ifController(MsIfController element) {
        IfController ifController = new IfController();
        ifController.setEnabled(element.getEnable());
        ifController.setName(StringUtils.isNotBlank(element.getName()) ? element.getName() : "If Controller");
        ifController.setProperty(TestElement.TEST_CLASS, IfController.class.getName());
        ifController.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("IfControllerPanel"));
        ifController.setCondition(element.getConditionValue());
        ifController.setEvaluateAll(false);
        ifController.setUseExpression(true);
        return ifController;
    }
}
