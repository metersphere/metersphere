package io.metersphere.service.utils;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

public class MsURLClassLoader extends URLClassLoader {
    private JarURLConnection cachedJarFile = null;

    public MsURLClassLoader() {
        super(new URL[]{}, findParentClassLoader());
    }

    /**
     * 将指定的文件url添加到类加载器的classpath中去，并缓存jar connection，方便以后卸载jar
     * 一个可想类加载器的classpath中添加的文件url
     *
     * @param
     */
    public void addURLFile(URL file) {
        try {
            // 打开并缓存文件url连接
            URLConnection uc = file.openConnection();
            if (uc instanceof JarURLConnection) {
                uc.setUseCaches(true);
                ((JarURLConnection) uc).getManifest();
                cachedJarFile = (JarURLConnection) uc;
            }
        } catch (Exception e) {
            System.err.println("Failed to cache plugin JAR file: " + file.toExternalForm());
        }
        addURL(file);
    }


    public void unloadJarFile(String url) {
        JarURLConnection jarURLConnection = cachedJarFile;
        if (jarURLConnection == null) {
            return;
        }
        try {
            System.err.println("Unloading plugin JAR file " + jarURLConnection.getJarFile().getName());
            jarURLConnection.getJarFile().close();
            jarURLConnection = null;
        } catch (Exception e) {
            System.err.println("Failed to unload JAR file\n" + e);
        }
    }


    /**
     * 定位基于当前上下文的父类加载器
     *
     * @return 返回可用的父类加载器.
     */
    private static ClassLoader findParentClassLoader() {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        if (parent == null) {
            parent = MsURLClassLoader.class.getClassLoader();
        }
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        return parent;
    }

}
