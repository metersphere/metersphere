package io.metersphere.api.parser.ms;

import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class TestPlanConverter extends AbstractMsElementConverter<TestPlan> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, TestPlan element, HashTree hashTree) {
        // 测试计划不做任何处理，直接解析子元素
        parseChild(parent, element, hashTree);
    }
}
