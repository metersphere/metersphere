package io.metersphere.plugin.api.spi;


import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import lombok.Setter;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author jianxing
 * @createTime 2021-10-30  10:07
 * 将 MsTestElement 具体实现类转换为 HashTree
 */
public abstract class AbstractJmeterElementConverter<T extends MsTestElement> implements JmeterElementConverter<T> {

    public Class<? extends MsTestElement> testElementClass;

    /**
     * 获取转换器的函数
     */
    @Setter
    private static Function<Class<? extends MsTestElement>, AbstractJmeterElementConverter> getConverterFunc;
    /**
     * 解析子步骤前的前置处理函数
     */
    private static List<AbstractJmeterElementConverter> childPreConverters = new ArrayList<>();
    /**
     * 解析子步骤前的前置处理函数
     */
    private static List<AbstractJmeterElementConverter> childPostConverters = new ArrayList<>();
    /**
     * 解析子步骤前的前置处理函数
     */
    private static List<JmeterElementConvertInterceptor> convertInterceptors = new ArrayList<>();

    public static void registerChildPreConverters(AbstractJmeterElementConverter converter) {
        childPreConverters.add(converter);
    }

    public static void registerChildPostConverters(AbstractJmeterElementConverter converter) {
        childPostConverters.add(converter);
    }

    public static void registerConvertInterceptor(JmeterElementConvertInterceptor interceptor) {
        convertInterceptors.add(interceptor);
    }

    public AbstractJmeterElementConverter() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            // 获取泛型的具体类型，即 MsTestElement 的具体实现类
            testElementClass = ((Class) parameterizedType.getActualTypeArguments()[0]);
        }
    }

    /**
     * 解析 MsTestElement 的子节点
     */
    public void parseChild(HashTree tree, AbstractMsTestElement element, ParameterConfig config) {
        if (element != null && element.getChildren() != null) {
            // 解析子步骤前的前置处理函数
            childPreConverters.forEach(processor -> processor.toHashTree(tree, element, config));
            for (AbstractMsTestElement child : element.getChildren()) {
                child.setParent(element);
                // 拦截器拦截
                HashTree wrapperTree = intercept(config, child, tree);
                getConverterFunc.apply(child.getClass()).toHashTree(wrapperTree, child, config);
            }
            // 解析子步骤后的后置处理函数
            childPostConverters.forEach(processor -> processor.toHashTree(tree, element, config));
        }
    }

    public static HashTree intercept(ParameterConfig config, AbstractMsTestElement testElement, HashTree hashTree) {
        for (JmeterElementConvertInterceptor convertInterceptor : convertInterceptors) {
            return convertInterceptor.intercept(hashTree, testElement, config);
        }
        return hashTree;
    }

    /**
     * 设置步骤标识
     * 当前步骤唯一标识，结果和步骤匹配的关键
     *
     * @param msHTTPElement
     * @param config
     * @param sampler
     */
    public void setStepIdentification(AbstractMsTestElement msHTTPElement, ParameterConfig config, TestElement sampler) {
        sampler.setProperty(ElementProperty.MS_RESOURCE_ID.name(), msHTTPElement.getResourceId());
        sampler.setProperty(ElementProperty.MS_STEP_ID.name(), msHTTPElement.getStepId());
        sampler.setProperty(ElementProperty.PROJECT_ID.name(), msHTTPElement.getProjectId());
    }
}
