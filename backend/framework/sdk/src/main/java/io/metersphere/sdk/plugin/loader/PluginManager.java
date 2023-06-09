package io.metersphere.sdk.plugin.loader;

import io.metersphere.sdk.controller.handler.result.CommonResultCode;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.plugin.storage.StorageStrategy;
import io.metersphere.sdk.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jianxing.chen
 */
public class PluginManager {

    /**
     * 自定义类加载器
     */
    protected Map<String, PluginClassLoader> classLoaderMap = new HashMap<>();

    public PluginClassLoader getClassLoader(String pluginId) {
        return classLoaderMap.get(pluginId);
    }

    /**
     * 加载对应目录下的 jar 包
     */
    public PluginManager loadJar(String pluginId, String jarfileDir, StorageStrategy storageStrategy) throws IOException {
        PluginClassLoader pluginClassLoader = new PluginClassLoader(storageStrategy);
        classLoaderMap.put(pluginId, pluginClassLoader);
        pluginClassLoader.loadJar(jarfileDir);
        return this;
    }

    public PluginManager loadJar(String pluginId, String jarfileDir) throws IOException {
        return this.loadJar(pluginId, jarfileDir, null);
    }

    public Map<String, PluginClassLoader> getClassLoaderMap() {
        return classLoaderMap;
    }

    public void deletePlugin(String id) {
        classLoaderMap.remove(id);
    }

    /**
     * 从输入流中加载 jar 包
     *
     * @param in
     */
    public PluginManager loadJar(String pluginId, InputStream in, StorageStrategy storageStrategy) throws IOException {
        PluginClassLoader pluginClassLoader = new PluginClassLoader(storageStrategy);
        classLoaderMap.put(pluginId, pluginClassLoader);
        pluginClassLoader.loadJar(in);
        return this;
    }

    public PluginManager loadJar(String pluginId, InputStream in) throws IOException {
        return this.loadJar(pluginId, in, null);
    }

    /**
     * 获取接口的单一实现类
     */
    public <T> Class<T> getImplClass(String pluginId, Class<T> superClazz) {
        PluginClassLoader classLoader = classLoaderMap.get(pluginId);
        LinkedHashSet<Class<T>> result = new LinkedHashSet<>();
        Set<Class> clazzSet = classLoader.getClazzSet();
        for (Class item : clazzSet) {
            if (isImplClazz(superClazz, item) && !result.contains(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取指定接口最后一次加载的实现类实例
     */
    public <T> T getImplInstance(String pluginId, Class<T> superClazz) {
        try {
            Class<T> clazz = getImplClass(pluginId, superClazz);
            return clazz.getConstructor().newInstance();
        } catch (InvocationTargetException e) {
            LogUtils.error(e);
            throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE, e.getTargetException().getMessage());
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE, e.getMessage());
        }
    }

    public <T> T getImplInstance(String pluginId, Class<T> superClazz, Object param) {
        try {
            Class<T> clazz = getImplClass(pluginId, superClazz);
            return clazz.getConstructor(param.getClass()).newInstance(param);
        } catch (InvocationTargetException e) {
            LogUtils.error(e.getTargetException());
            throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE, e.getTargetException().getMessage());
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE, e.getMessage());
        }
    }


    /**
     * 判断 impClazz 是否是 superClazz 的实现类
     *
     * @param impClazz
     * @return
     */
    private boolean isImplClazz(Class superClazz, Class impClazz) {
        if (impClazz == superClazz) {
            return true;
        }
        Type[] interfaces = impClazz.getGenericInterfaces();

        if (interfaces != null && interfaces.length > 0) {
            for (Type genericInterface : interfaces) {
                if (genericInterface instanceof Class && isImplClazz(superClazz, (Class) genericInterface)) {
                    return true;
                }
            }
        }
        Type superclass = impClazz.getGenericSuperclass();
        if (superclass != null
                && superclass instanceof Class
                && isImplClazz(superClazz, (Class) superclass)) {
            return true;

        }
        return false;
    }
}
