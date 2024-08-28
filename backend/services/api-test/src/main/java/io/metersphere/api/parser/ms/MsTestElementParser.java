package io.metersphere.api.parser.ms;

import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 将 hashTree 转换为 MsTestElement
 */
public class MsTestElementParser {

    public AbstractMsTestElement parse(HashTree hashTree) {
        MsScenario msScenario = new MsScenario();
        for (Object key : hashTree.keySet()) {
            if (key instanceof TestElement testElement) {
                MsElementConverterRegister.getConverter(testElement.getClass()).toMsElement(msScenario, testElement, hashTree);
            }
        }
        return msScenario;
    }
}
