package io.metersphere.sdk.plugin.loader;

import io.metersphere.sdk.controller.handler.result.CommonResultCode;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.plugin.storage.StorageStrategy;
import io.metersphere.sdk.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author jianxing.chen
 */
public class PluginManager {

    /**
     * 自定义类加载器
     */
    protected Map<String, PluginClassLoader> classLoaderMap = new HashMap<>();

    /**
     * 缓存查找过的类
     * 内层 map
     * key 未接口的类
     * value 为实现类
     */
    protected Map<String, Map<Class, Class>> implClassCache = new HashMap<>();

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
        implClassCache.remove(id);
    }

    /**
     * 从输入流中加载 jar 包
     *
     * @param in
     */
    public PluginManager loadJar(String pluginId, InputStream in, StorageStrategy storageStrategy, boolean isNeedUploadFile) throws Exception {
        PluginClassLoader pluginClassLoader = new PluginClassLoader(storageStrategy, isNeedUploadFile);
        classLoaderMap.put(pluginId, pluginClassLoader);
        pluginClassLoader.loadJar(in);
        return this;
    }

    public PluginManager loadJar(String pluginId, InputStream in, boolean isNeedUploadFile) throws Exception {
        return this.loadJar(pluginId, in, null, isNeedUploadFile);
    }

    /**
     * 获取接口的单一实现类
     */
    public <T> Class<T> getImplClass(String pluginId, Class<T> superClazz) {
        PluginClassLoader classLoader = getPluginClassLoader(pluginId);
        Map<Class, Class> classes = implClassCache.get(pluginId);
        if (classes == null) {
            classes = new HashMap<>();
            implClassCache.put(pluginId, classes);
        }
        if (classes.get(superClazz) != null) {
            return classes.get(superClazz);
        }
        LinkedHashSet<Class<T>> result = new LinkedHashSet<>();
        Set<Class> clazzSet = classLoader.getClazzSet();
        for (Class item : clazzSet) {
            if (isImplClazz(superClazz, item) && !result.contains(item)) {
                classes.put(superClazz, item);
                return item;
            }
        }
        return null;
    }

    private PluginClassLoader getPluginClassLoader(String pluginId) {
        PluginClassLoader classLoader = classLoaderMap.get(pluginId);
        if (classLoader == null) {
            throw new MSException("插件未加载");
        }
        return classLoader;
    }

    /**
     * 获取指定接口最后一次加载的实现类实例
     */
    public <T> T getImplInstance(String pluginId, Class<T> superClazz) {
       return this.getImplInstance(pluginId, superClazz, null);
    }

    public <T> T getImplInstance(String pluginId, Class<T> superClazz, Object param) {
        try {
            Class<T> clazz = getImplClass(pluginId, superClazz);
            if (clazz == null) {
                throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE);
            }
            if (param == null) {
                return clazz.getConstructor().newInstance();
            } else {
                return clazz.getConstructor(param.getClass()).newInstance(param);
            }
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
