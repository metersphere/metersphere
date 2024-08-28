package io.metersphere.api.parser.ms;

import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  13:57
 * 作为通用的解析器，如果遇到不支持的组件，会使用这个解析器
 */
public class JmeterGeneralElementConverter extends AbstractMsElementConverter<TestElement> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, TestElement element, HashTree hashTree) {
        // 不做任何处理，直接解析子元素
        parseChild(parent, element, hashTree);
    }
}
