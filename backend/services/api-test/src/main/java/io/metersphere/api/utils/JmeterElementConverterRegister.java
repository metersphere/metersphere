package io.metersphere.api.utils;

import io.metersphere.api.parser.jmeter.MsCommonElementConverter;
import io.metersphere.api.parser.jmeter.MsHTTPElementConverter;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.MsTestElement;
import io.metersphere.plugin.sdk.util.PluginLogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-03  17:14
 */
public class JmeterElementConverterRegister {

    /**
     * 解析器集合
     * key 为 MsTestElement 实现类的 Class
     * value 为对应的转换器
     */
    private static final Map<Class<? extends MsTestElement>, AbstractJmeterElementConverter<?>> parserMap = new HashMap<>();

    static {
        // 注册默认的转换器 todo 注册插件的转换器
        JmeterElementConverterRegister.register(MsHTTPElementConverter.class);
        JmeterElementConverterRegister.register(MsCommonElementConverter.class);
    }

    /**
     * 注册 MsTestElement 对应的转换器
     * @param elementConverterClass
     */
    public static void register(Class<? extends AbstractJmeterElementConverter> elementConverterClass) {
        try {
            AbstractJmeterElementConverter elementConverter = elementConverterClass.getConstructor().newInstance();
            // 设置获取转换器的方法
            Function<Class<? extends MsTestElement>, AbstractJmeterElementConverter> getConverterFunc = JmeterElementConverterRegister::getConverter;
            elementConverter.setGetConverterFunc(getConverterFunc);
            // 注册到解析器集合中
            parserMap.put(elementConverter.testElementClass, elementConverter);
        } catch (Exception e) {
            PluginLogUtils.error("注册转换器失败: " + elementConverterClass);
            PluginLogUtils.error(e);
        }
    }

    /**
     * 获取对应组件的转换器
     */
    public static AbstractJmeterElementConverter getConverter(Class<? extends MsTestElement> msTestElementClass) {
        return parserMap.get(msTestElementClass);
    }
}
