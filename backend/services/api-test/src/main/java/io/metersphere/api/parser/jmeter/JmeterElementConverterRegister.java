package io.metersphere.api.parser.jmeter;

import io.metersphere.api.parser.jmeter.child.MsCsvChildPreConverter;
import io.metersphere.api.parser.jmeter.controller.MsConstantTimerControllerConverter;
import io.metersphere.api.parser.jmeter.controller.MsIfControllerConverter;
import io.metersphere.api.parser.jmeter.controller.MsLoopControllerConverter;
import io.metersphere.api.parser.jmeter.controller.MsOnceOnlyControllerConverter;
import io.metersphere.api.parser.jmeter.interceptor.RetryInterceptor;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.MsTestElement;
import io.metersphere.plugin.sdk.util.PluginLogUtils;
import io.metersphere.sdk.exception.MSException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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
    private static final Map<Class<? extends MsTestElement>, AbstractJmeterElementConverter<? extends MsTestElement>> parserMap = new HashMap<>();

    static {
        // 设置获取转换器的方法
        AbstractJmeterElementConverter.setGetConverterFunc(JmeterElementConverterRegister::getConverter);
        // 注册子步骤前置解析器
        AbstractJmeterElementConverter.registerChildPreConverters(new MsCsvChildPreConverter());
        // 注册默认的转换器
        register(MsHTTPElementConverter.class);
        register(MsCommonElementConverter.class);
        register(MsScriptElementConverter.class);
        register(MsScenarioConverter.class);
        register(MsIfControllerConverter.class);
        register(MsLoopControllerConverter.class);
        register(MsOnceOnlyControllerConverter.class);
        register(MsConstantTimerControllerConverter.class);
        register(MsJMeterComponentConverter.class);

        // 注册转换器拦截器
        AbstractJmeterElementConverter.registerConvertInterceptor(new RetryInterceptor());
    }

    /**
     * 注册 MsTestElement 对应的转换器
     *
     * @param elementConverterClass 转换器的类
     */
    public static void register(Class<? extends AbstractJmeterElementConverter<? extends MsTestElement>> elementConverterClass) {
        try {
            AbstractJmeterElementConverter<? extends MsTestElement> elementConverter = elementConverterClass.getDeclaredConstructor().newInstance();
            // 注册到解析器集合中
            parserMap.put(elementConverter.testElementClass, elementConverter);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            handleRegistrationException(elementConverterClass, e);
        }
    }

    /**
     *  注销 MsTestElement 对应的转换器
     *
     * @param elementConverterClass 转换器的类
     */
    public static void unRegister(Class<? extends AbstractJmeterElementConverter<? extends MsTestElement>> elementConverterClass) {
        parserMap.remove(elementConverterClass);
    }

    /**
     * 获取对应组件的转换器
     *
     * @param msTestElementClass 组件的类
     * @return 转换器
     */
    public static AbstractJmeterElementConverter getConverter(Class<? extends MsTestElement> msTestElementClass) {
        return parserMap.computeIfAbsent(msTestElementClass, cls -> {
            throw new MSException("No corresponding converter found: " + cls);
        });
    }

    /**
     * 处理注册转换器时的异常
     *
     * @param elementConverterClass 转换器的类
     * @param e                     异常
     */
    private static void handleRegistrationException(Class<?> elementConverterClass, Exception e) {
        PluginLogUtils.error("注册转换器失败: " + elementConverterClass, e);
    }
}

