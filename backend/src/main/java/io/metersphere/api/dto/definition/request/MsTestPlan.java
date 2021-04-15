package io.metersphere.api.dto.definition.request;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "TestPlan")
public class MsTestPlan extends MsTestElement {
    private String type = "TestPlan";
    private boolean serializeThreadgroups = false;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        final HashTree testPlanTree = tree.add(getPlan());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(testPlanTree, el.getHashTree(), config);
            });
        }
    }

    public TestPlan getPlan() {
        TestPlan testPlan = new TestPlan(StringUtils.isEmpty(this.getName()) ? "TestPlan" : this.getName());
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestPlanGui"));
        testPlan.setEnabled(true);
        testPlan.setFunctionalMode(false);
        testPlan.setSerialized(serializeThreadgroups);
        testPlan.setTearDownOnShutdown(true);
        testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

}
