package io.metersphere.plugin.api.spi;


import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.pf4j.ExtensionPoint;

/**
 * @author jianxing
 * @createTime 2021-10-30  10:07
 * 将 MsTestElement 具体实现类转换为 HashTree
 */
public interface MsElementConverter<T extends TestElement> extends ExtensionPoint {

    /**
     * 将 MsTestElement 具体实现类转换为 HashTree
     */
    void toMsElement(AbstractMsTestElement parent, T element, HashTree hashTree);
}
