package io.metersphere.api.dto.definition.request.controller;

import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.CriticalSectionController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

public class MsCriticalSectionController extends MsTestElement {

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {

    }

    public static HashTree createHashTree(HashTree tree, String name) {
        CriticalSectionController criticalSectionController = new CriticalSectionController();
        criticalSectionController.setName(StringUtils.isNotEmpty(name) ? "Csc_" + name : "Scenario Critical Section Controller");
        criticalSectionController.setLockName("global_lock");
        criticalSectionController.setEnabled(true);
        criticalSectionController.setProperty(TestElement.TEST_CLASS, CriticalSectionController.class.getName());
        criticalSectionController.setProperty(TestElement.GUI_CLASS, "CriticalSectionControllerGui");
        return tree.add(criticalSectionController);
    }
}
