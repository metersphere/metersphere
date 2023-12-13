package io.metersphere.sdk.util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

@Component
public class CommonBeanFactory implements ApplicationContextAware {
    private static ApplicationContext context;

    public void setApplicationContext(@NotNull ApplicationContext ctx) throws BeansException {
        context = ctx;
    }

    public static Object getBean(String beanName) {
        try {
            return context != null && !StringUtils.isBlank(beanName) ? context.getBean(beanName) : null;
        } catch (BeansException e) {
            return null;
        }
    }

    public static <T> T getBean(Class<T> className) {
        try {
            return context != null && className != null ? context.getBean(className) : null;
        } catch (BeansException e) {
            return null;
        }
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> className) {
        return context.getBeansOfType(className);
    }

    public static Object invoke(String beanName, Function<Class<?>, Method> methodFunction, Object... args) {
        try {
            Object bean = getBean(beanName);
            if (ObjectUtils.isNotEmpty(bean)) {
                Class<?> clazz = bean.getClass();
                return methodFunction.apply(clazz).invoke(bean, args);
            }
        } catch (Exception e) {
            LogUtils.error(e);
        }
        return null;
    }
}

