package io.metersphere.api.parser.ms;

import io.metersphere.api.parser.ms.http.HeaderManagerConverter;
import io.metersphere.api.parser.ms.http.contro.ForEachControllerConverter;
import io.metersphere.api.parser.ms.http.contro.IfControllerConverter;
import io.metersphere.api.parser.ms.http.contro.LoopControllerConverter;
import io.metersphere.api.parser.ms.http.contro.WhileControllerConverter;
import io.metersphere.api.parser.ms.http.post.*;
import io.metersphere.api.parser.ms.http.pre.BeanShellPreProcessConverter;
import io.metersphere.api.parser.ms.http.pre.JDBCPreProcessConverter;
import io.metersphere.api.parser.ms.http.pre.JSR223PreProcessConverter;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.sdk.util.PluginLogUtils;
import org.apache.jmeter.testelement.TestElement;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-03  17:14
 */
public class MsElementConverterRegister {

    /**
     * 解析器集合
     * key 为 TestElement 实现类的 Class
     * value 为对应的转换器
     */
    private static final Map<Class<? extends TestElement>, AbstractMsElementConverter<? extends TestElement>> parserMap = new HashMap<>();
    private static final JmeterGeneralElementConverter generalElementConverter = new JmeterGeneralElementConverter();

    static {
        // 设置获取转换器的方法
        AbstractMsElementConverter.setGetConverterFunc(MsElementConverterRegister::getConverter);

        // 注册默认的转换器
        register(TestPlanConverter.class);
        register(HTTPSamplerConverter.class);
        register(HeaderManagerConverter.class);

        register(BeanShellPostProcessConverter.class);
        register(ConstantTimerConverter.class);
        register(JDBCPostProcessConverter.class);
        register(JSONPostProcessorConverter.class);
        register(JSR223PostProcessConverter.class);
        register(RegexExtractorConverter.class);
        register(XPath2ExtractorConverter.class);
        register(XPathExtractorConverter.class);
        register(JSONPathAssertionConverter.class);
        register(XPathAssertionConverter.class);
        register(ThreadGroupConverter.class);

        register(BeanShellPreProcessConverter.class);
        register(JDBCPreProcessConverter.class);
        register(JSR223PreProcessConverter.class);

        register(ResultCollectorConverter.class);
        register(DebugSampleConverter.class);

        register(LoopControllerConverter.class);
        register(ForEachControllerConverter.class);
        register(WhileControllerConverter.class);
        register(IfControllerConverter.class);
    }

    /**
     * 注册 TestElement 对应的转换器
     *
     * @param elementConverterClass 转换器的类
     */
    public static void register(Class<? extends AbstractMsElementConverter<? extends TestElement>> elementConverterClass) {
        try {
            AbstractMsElementConverter<? extends TestElement> elementConverter = elementConverterClass.getDeclaredConstructor().newInstance();
            // 注册到解析器集合中
            parserMap.put(elementConverter.testElementClass, elementConverter);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            handleRegistrationException(elementConverterClass, e);
        }
    }

    /**
     *  注销 TestElement 对应的转换器
     *
     * @param elementConverterClass 转换器的类
     */
    public static void unRegister(Class<? extends AbstractMsElementConverter<? extends TestElement>> elementConverterClass) {
        parserMap.remove(elementConverterClass);
    }

    /**
     * 获取对应组件的转换器
     *
     * @param TestElementClass 组件的类
     * @return 转换器
     */
    public static AbstractMsElementConverter getConverter(Class<? extends TestElement> TestElementClass) {
        AbstractMsElementConverter<? extends TestElement> converter = parserMap.get(TestElementClass);
        // 如果没有对应的转换器，则使用通用的转换器
        return Objects.requireNonNullElse(converter, generalElementConverter);
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

