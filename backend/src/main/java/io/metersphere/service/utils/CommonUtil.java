package io.metersphere.service.utils;

import com.github.pagehelper.util.StringUtil;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.api.UiScriptApi;
import org.reflections8.Reflections;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.net.URL;
import java.util.*;

public class CommonUtil {
    //获取某个类的实现类
    public static List<Class<?>> getAllAssignedClass(Class<?> cls) throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Class<?> c : getClasses(cls)) {
            if (cls.isAssignableFrom(c) && !cls.equals(c)) {
                classes.add(c);
            }
        }
        return classes;
    }

    public static List<Class<?>> getClasses(Class<?> cls) throws Exception {
        String pk = cls.getPackage().getName();
        String path = pk.replace('.', '/');
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(path);
        return getClasses(new File(url.getFile()), pk);
    }

    //根据路径获取
    public static List<Class<?>> getClasses(File dir, String pk) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!dir.exists()) {
            return classes;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                classes.addAll(getClasses(f, pk + "." + f.getName()));
            }
            String name = f.getName();
            if (name.endsWith(".class")) {
                classes.add(Class.forName(pk + "." + name.substring(0, name.length() - 6)));
            }
        }
        return classes;
    }

    public static List<Class<?>> getSubClass(String fileName) {
        List<Class<?>> classes = new LinkedList<>();
        try {
            if (StringUtil.isNotEmpty(fileName) && fileName.endsWith(".jar")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            LogUtil.info("获取到文件路径：" + fileName);
            Resource resource = new ClassPathResource(fileName);
            Properties inPro = PropertiesLoaderUtils.loadProperties(resource);
            if (inPro != null) {
                LogUtil.info("开始读取文件内容进行反射处理");
                Set<String> entryObj = inPro.stringPropertyNames();
                if (entryObj != null) {
                    for (String entry : entryObj) {

                        Class<?> clazz = Class.forName(entry);
                        classes.add(clazz);

                    }
                }
            }
        } catch (Exception e) {
            MSException.throwException("解析插件失败，未找到入口配置");
        }
        return classes;
    }

    public static List<Class<?>> getSubClazz(String pac) {
        List<Class<?>> classes = new LinkedList<>();
        try {
            Reflections reflections = new Reflections(pac);
            Set<Class<? extends UiScriptApi>> subTypes = reflections.getSubTypesOf(UiScriptApi.class);
            subTypes.forEach(x -> classes.add(x));
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
            ex.printStackTrace();
        }
        return classes;
    }
}
