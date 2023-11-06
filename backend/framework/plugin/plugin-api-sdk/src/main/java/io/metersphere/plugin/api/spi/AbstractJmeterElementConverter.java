package io.metersphere.plugin.api.spi;


import org.apache.jorphan.collections.HashTree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jianxing
 * @createTime 2021-10-30  10:07
 * 将 MsTestElement 具体实现类转换为 HashTree
 */
public abstract class AbstractJmeterElementConverter<T extends MsTestElement> {

    /**
     * 解析器集合
     * key 为 MsTestElement 实现类的 Class
     * value 为对应的转换器
     */
    private static final Map<Class<? extends MsTestElement>, AbstractJmeterElementConverter<?>> parserMap = new HashMap<>();

    public AbstractJmeterElementConverter() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType type) {
            // 获取泛型的具体类型，即 MsTestElement 的具体实现类
            Class<? extends MsTestElement> msTestElementClass = (Class<? extends MsTestElement>) type.getActualTypeArguments()[0];
            // 注册到解析器集合中
            parserMap.put(msTestElementClass, this);
        }
    }


    /**
     * 将 MsTestElement 具体实现类转换为 HashTree
     *
     */
    public abstract void toHashTree(HashTree tree, AbstractMsTestElement element, MsParameter config);

    /**
     * 解析 MsTestElement 的子节点
     */
    public void parseChild(HashTree tree, AbstractMsTestElement element, MsParameter config) {
        if (element != null && element.getChildren() != null) {
            for (MsTestElement testElement : element.getChildren()) {
                AbstractJmeterElementConverter<?> converter = getConverter(testElement);
                converter.toHashTree(tree, element, config);
            }
        }
    }

    /**
     * 获取对应组件的转换器
     */
    public static AbstractJmeterElementConverter<?> getConverter(MsTestElement element) {
        return parserMap.get(element.getClass());
    }

}
