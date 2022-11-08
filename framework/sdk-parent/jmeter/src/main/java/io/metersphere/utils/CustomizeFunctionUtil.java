package io.metersphere.utils;

import groovy.lang.GroovyClassLoader;
import io.metersphere.jmeter.MsClassLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizeFunctionUtil {
    public static final String CUSTOMIZE_FUNCTION = "CUSTOMIZE_FUNCTION";
    private static final String CONTAIN = "classfinder.functions.contain";
    private static final String NOT_CONTAIN = "classfinder.functions.notContain";
    public static final String JAR_PATH = "JAR_PATH";
    public static final String MS_CLASS_LOADER = "MS_CLASS_LOADER";

    public static void initCustomizeClass(TestPlan testPlan) {
        try {
            Map<String, Class<? extends Function>> functions = new HashMap<>();
            String pathStr = testPlan.getPropertyAsString(JAR_PATH);
            JMeterContext context = JMeterContextService.getContext();
            if (StringUtils.isNotEmpty(pathStr) && context != null) {
                List<String> jarPaths = JsonUtils.parseObject(pathStr, List.class);
                if (CollectionUtils.isNotEmpty(jarPaths)) {
                    // 初始化类加载器
                    GroovyClassLoader loader = MsClassLoader.getDynamic(jarPaths);
                    if (loader == null) {
                        return;
                    }
                    // 所有自定义jar的类加载器
                    context.getVariables().putObject(MS_CLASS_LOADER, loader);
                    Thread.currentThread().setContextClassLoader(loader);
                    // 获取所有自定义函数class
                    List<String> classes = ClassFinder.findClassesThatExtend(
                            jarPaths.toArray(String[]::new),
                            new Class[]{Function.class}, true,
                            JMeterUtils.getProperty(CONTAIN),
                            JMeterUtils.getProperty(NOT_CONTAIN)
                    );

                    for (String clazzName : classes) {
                        Function tempFunc = loader.loadClass(clazzName)
                                .asSubclass(Function.class)
                                .getDeclaredConstructor().newInstance();
                        String referenceKey = tempFunc.getReferenceKey();
                        if (StringUtils.isNotEmpty(referenceKey)) { // ignore self
                            functions.put(referenceKey, tempFunc.getClass());
                        }
                    }
                }
            }
            if (MapUtils.isNotEmpty(functions)) {
                context.getVariables().putObject(CUSTOMIZE_FUNCTION, functions);
            }
        } catch (Exception e) {
            LoggerUtil.error(e);
        }
    }

    public static Map<String, Class<? extends Function>> getFunctions() {
        try {
            JMeterContext context = JMeterContextService.getContext();
            if (context != null && ObjectUtils.isNotEmpty(context.getVariables())) {
                Object function = context.getVariables().getObject(CUSTOMIZE_FUNCTION);
                if (ObjectUtils.isNotEmpty(function)) {
                    return (Map<String, Class<? extends Function>>) function;
                }
            }
            return new HashMap<>();
        } catch (Exception e) {
            LoggerUtil.error(e);
            return new HashMap<>();
        }
    }
}
