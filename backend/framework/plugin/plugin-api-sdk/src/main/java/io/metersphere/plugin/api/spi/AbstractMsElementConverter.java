package io.metersphere.plugin.api.spi;


import lombok.Setter;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * @author jianxing
 * @createTime 2021-10-30  10:07
 * 将 Jmeter 的 TestElement 转换成 MsTestElement
 */
public abstract class AbstractMsElementConverter<T extends TestElement> implements MsElementConverter<T> {

    public Class<? extends TestElement> testElementClass;

    /**
     * 获取转换器的函数
     */
    @Setter
    private static Function<Class<? extends TestElement>, AbstractMsElementConverter> getConverterFunc;

    public AbstractMsElementConverter() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            // 获取泛型的具体类型，即 MsTestElement 的具体实现类
            testElementClass = ((Class) parameterizedType.getActualTypeArguments()[0]);
        }
    }

    /**
     * 解析 MsTestElement 的子节点
     */
    public void parseChild(AbstractMsTestElement parentMsElement, TestElement parentElement, HashTree hashTree) {
        HashTree currentHashtree = hashTree.get(parentElement);
        if (currentHashtree == null) {
            return;
        }
        for (Object key : currentHashtree.list()) {
            if (key instanceof TestElement testElement) {
                getConverterFunc.apply(testElement.getClass()).toMsElement(parentMsElement, testElement, currentHashtree);
            }
        }
    }
}
