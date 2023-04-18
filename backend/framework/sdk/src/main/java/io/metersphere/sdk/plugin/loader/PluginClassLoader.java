package io.metersphere.sdk.plugin.loader;

import io.metersphere.sdk.plugin.storage.StorageStrategy;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * @author jianxing.chen
 */
public class PluginClassLoader extends ClassLoader {

    /**
     * 记录加载的类
     */
    protected final Set<Class> clazzSet = new HashSet<>();
    /**
     * 加载重试次数
     */
    protected final static int CLASS_RELOAD_TIME = 20;
    /**
     * 保存加载失败的类，之后重试
     */
    protected Map<String, byteArrayWrapper> loadErrorMap = new HashMap<>();

    private class byteArrayWrapper {
        private byte[] values;

        public byteArrayWrapper(byte[] values) {
            this.values = values;
        }

        public byte[] getValues() {
            return values;
        }
    }

    public Set<Class> getClazzSet() {
        return clazzSet;
    }

    /**
     * jar包的静态资源的存储策略
     * 可以扩展为对象存储
     */
    protected StorageStrategy storageStrategy;

    public PluginClassLoader() {
        // 将父加载器设置成当前的类加载器，目的是由父加载器加载接口，实现类由该加载器加载
        super(PluginClassLoader.class.getClassLoader());
    }

    public PluginClassLoader(StorageStrategy storageStrategy) {
        this();
        this.storageStrategy = storageStrategy;
    }

    public StorageStrategy getStorageStrategy() {
        return storageStrategy;
    }

    /**
     * 扫描目录或 jar 包
     * 加载 clazz
     *
     * @param file
     */
    protected void scanJarFile(File file) throws IOException {
        if (file.exists()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    readJar(new JarFile(file));
                } catch (IOException e) {
                    throw e;
                }
            } else if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    scanJarFile(f);
                }
            }
        }
    }

    /**
     * 加载对应目录下的 jar 包
     *
     * @param jarfileDir
     */
    public void loadJar(String jarfileDir) throws IOException {
        if (StringUtils.isBlank(jarfileDir)) {
            throw new IllegalArgumentException("basePath can not be empty!");
        }
        File dir = new File(jarfileDir);
        if (!dir.exists()) {
            throw new IllegalArgumentException("basePath not exists:" + jarfileDir);
        }
        scanJarFile(new File(jarfileDir));
    }

    /**
     * 从输入流中加载 jar 包
     *
     * @param in
     */
    public void loadJar(InputStream in) throws IOException {
        if (in != null) {
            try (JarInputStream jis = new JarInputStream(in)) {
                JarEntry je;
                while ((je = jis.getNextJarEntry()) != null) {
                    loadJar(jis, je);
                }
                reloadErrorClazz();
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * 读取 jar 包中的 clazz
     *
     * @param jar
     * @throws IOException
     */
    protected void readJar(JarFile jar) {
        Enumeration<JarEntry> en = jar.entries();
        while (en.hasMoreElements()) {
            JarEntry je = en.nextElement();
            try (InputStream in = jar.getInputStream(je)) {
                loadJar(in, je);
            } catch (IOException e) {
//                LogUtils.error(e);
            }
        }
        reloadErrorClazz();
    }

    /**
     * 加载 jar 包的 class，并存储静态资源
     *
     * @param in
     * @param je
     * @throws IOException
     */
    protected void loadJar(InputStream in, JarEntry je) throws IOException {
        je.getName();
        String name = je.getName();
        if (name.endsWith(".class")) {
            String className = name.replace("\\", ".")
                    .replace("/", ".")
                    .replace(".class", "");
            BufferedInputStream bis;
            byte[] bytes = null;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                int line;
                bytes = new byte[1024];
                bis = new BufferedInputStream(in);
                while ((line = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, line);
                }
                bos.flush();
                bytes = bos.toByteArray();
                Class<?> clazz = defineClass(className, bytes, 0, bytes.length);
                clazzSet.add(clazz);
            } catch (NoClassDefFoundError e) {
                loadErrorMap.put(className, new byteArrayWrapper(bytes));
            } catch (Throwable e) {
//                LogUtils.error(e);
            }
        } else if (!name.endsWith("/")) {
            // 非目录即静态资源
            if (storageStrategy != null) {
                storageStrategy.store(name, in);
            }
        }
    }

    /**
     * 由于 loadJar 中是按照条目加载的
     * 加载顺序不确定，如果父类没有先加载会加载失败
     * 这里针对加载失败的类，再次加载
     */
    private synchronized void reloadErrorClazz() {
        for (int i = 0; i < CLASS_RELOAD_TIME; i++) {
            Iterator<String> iterator = loadErrorMap.keySet().iterator();
            while (iterator.hasNext()) {
                String className = iterator.next();
                try {
//                    LogUtils.info("reload class: " + className);
                    byte[] bytes = loadErrorMap.get(className).getValues();
                    Class<?> clazz = defineClass(className, bytes, 0, bytes.length);
                    clazzSet.add(clazz);
                    iterator.remove();
                } catch (Throwable e) {
//                    LogUtils.error(e);
                }
            }
        }
    }

    /**
     * 从存储策略中加载静态资源
     * @param name
     * @return
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        if (null != storageStrategy) {
            try {
                return storageStrategy.get(name);
            } catch (IOException e) {
//                LogUtils.error(e, logger);
                return null;
            }
        }
        return super.getResourceAsStream(name);
    }
}
