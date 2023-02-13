package io.metersphere.api.dto.definition.request;

import com.alibaba.excel.util.StringUtils;
import io.metersphere.commons.constants.ApiTestConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.enums.JmxFileMetadataColumns;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)

public class MsTestPlan extends MsTestElement {
    private String type = ElementConstants.TEST_PLAN;
    private String clazzName = MsTestPlan.class.getCanonicalName();
    // 自定义JAR项目id
    private List<String> projectJarIds;

    private boolean serializeThreadGroups = false;

    // 资源池调用的时候需要的jar配置
    private Map<String, List<ProjectJarConfig>> poolJarsMap;

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
        TestPlan testPlan = new TestPlan(StringUtils.isEmpty(this.getName()) ? ElementConstants.TEST_PLAN : this.getName());
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestPlanGui"));
        testPlan.setEnabled(true);
        testPlan.setFunctionalMode(false);
        testPlan.setSerialized(serializeThreadGroups);
        testPlan.setTearDownOnShutdown(true);
        if (CollectionUtils.isNotEmpty(projectJarIds)) {
            testPlan.setProperty(ApiTestConstants.JAR_PATH, JSON.toJSONString(projectJarIds));
        }
        if (MapUtils.isNotEmpty(poolJarsMap)) {
            testPlan.setProperty(JmxFileMetadataColumns.JAR_PATH_CONFIG.name(), JSON.toJSONString(poolJarsMap));
        }
        testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

}
