package io.metersphere.api.dto.definition.request;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.commons.constants.ApiTestConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
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
    private String clazzName = MsTestPlan.class.getCanonicalName();
    // 自定义JAR
    private List<String> jarPaths;

    private boolean serializeThreadGroups = false;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
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
        testPlan.setSerialized(serializeThreadGroups);
        testPlan.setTearDownOnShutdown(true);
        if (CollectionUtils.isNotEmpty(jarPaths)) {
            testPlan.setProperty(ApiTestConstants.JAR_PATH, JSON.toJSONString(jarPaths));
        }
        testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

}
