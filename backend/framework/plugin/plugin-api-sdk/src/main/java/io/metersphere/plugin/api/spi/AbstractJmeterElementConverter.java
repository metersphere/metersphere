package io.metersphere.plugin.api.spi;


import io.metersphere.plugin.api.dto.ParameterConfig;
import lombok.Setter;
import org.apache.jorphan.collections.HashTree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * @author jianxing
 * @createTime 2021-10-30  10:07
 * 将 MsTestElement 具体实现类转换为 HashTree
 */
public abstract class AbstractJmeterElementConverter<T extends MsTestElement> {

    public Class<? extends MsTestElement> testElementClass;

    /**
     * 获取转换器的函数
     * 主应用在实例化转换器的时候会设置
     */
    @Setter
    private Function<Class<? extends MsTestElement>, AbstractJmeterElementConverter> getConverterFunc;

    public AbstractJmeterElementConverter() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            // 获取泛型的具体类型，即 MsTestElement 的具体实现类
            testElementClass = ((Class) parameterizedType.getActualTypeArguments()[0]);
        }
    }

    /**
     * 将 MsTestElement 具体实现类转换为 HashTree
     */
    public abstract void toHashTree(HashTree tree, T element, ParameterConfig config);

    /**
     * 解析 MsTestElement 的子节点
     */
    public void parseChild(HashTree tree, AbstractMsTestElement element, ParameterConfig config) {
        if (element != null && element.getChildren() != null) {
            element.getChildren().forEach(child ->
                    getConverterFunc.apply(child.getClass()).toHashTree(tree, child, config));
        }
    }
}
