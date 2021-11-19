package io.metersphere.service.utils;

import com.github.pagehelper.util.StringUtil;
import io.metersphere.commons.utils.LogUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MsClassLoader {

    public final static ConcurrentHashMap<String, MsURLClassLoader> LOADER_CACHE = new ConcurrentHashMap<>();

    public MsURLClassLoader loadJar(String path) {
        try {
            String jarName = path.substring(path.indexOf("_") + 1);
            if (StringUtil.isNotEmpty(jarName) && jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4);
            }
            MsURLClassLoader urlClassLoader = LOADER_CACHE.get(jarName);
            if (urlClassLoader != null) {
                return urlClassLoader;
            }
            urlClassLoader = new MsURLClassLoader();
            File jarFile = new File(path);
            URL jarUrl = jarFile.toURI().toURL();
            urlClassLoader.addURLFile(jarUrl);
            LOADER_CACHE.put(jarName, urlClassLoader);
            return urlClassLoader;
        } catch (Exception ex) {
            LogUtil.error("加载JAR包失败：" + ex.getMessage());
        }
        return null;
    }

    public Class loadClass(String jarName, String name) throws ClassNotFoundException {
        if (StringUtil.isNotEmpty(jarName) && jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4);
        }
        MsURLClassLoader urlClassLoader = LOADER_CACHE.get(jarName);
        if (urlClassLoader == null) {
            return null;
        }
        return urlClassLoader.loadClass(name);
    }

    public void unloadJarFile(String path) throws MalformedURLException {
        String jarName = path.substring(path.indexOf("_") + 1);
        if (StringUtil.isNotEmpty(jarName) && jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4);
        }
        MsURLClassLoader urlClassLoader = LOADER_CACHE.get(jarName);
        if (urlClassLoader == null) {
            return;
        }
        urlClassLoader.unloadJarFile(path);
        urlClassLoader = null;
        LOADER_CACHE.remove(jarName);
    }
}
