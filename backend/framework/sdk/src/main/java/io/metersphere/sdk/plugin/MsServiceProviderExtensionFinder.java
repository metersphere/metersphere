package io.metersphere.sdk.plugin;


import io.metersphere.sdk.util.LogUtils;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.ServiceProviderExtensionFinder;
import org.pf4j.processor.ExtensionStorage;
import org.pf4j.processor.ServiceProviderExtensionStorage;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author jianxing
 * 支持加载 jdbc 驱动和 xstream 的 Converter
 * pf4j 中 ServiceProviderExtensionFinder 本身是支持 SPI
 * 默认会读取 META-INF/services 下的文件
 * 但是遍历 JarEntry 发现 jdbc 资源中没有 META-INF/services 只有 META-INF/services/java.sql.Driver
 * 所以使用默认的 ServiceProviderExtensionFinder 会无法加载，这里重写后只修改了 EXTENSIONS_RESOURCE
 */
public class MsServiceProviderExtensionFinder extends ServiceProviderExtensionFinder {

    public static final String EXTENSIONS_RESOURCE = ServiceProviderExtensionStorage.EXTENSIONS_RESOURCE;

    /**
     * 需要支持的 SPI 文件
     */
    public static final List<String> SPI_FILES = List.of("java.sql.Driver", "com.thoughtworks.xstream.converters.Converter");

    public MsServiceProviderExtensionFinder(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public Map<String, Set<String>> readClasspathStorages() {
        LogUtils.debug("Reading extensions storages from classpath");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        final Set<String> bucket = new HashSet<>();
        try {
            for (String spiFile : SPI_FILES) {
                String spiResource = EXTENSIONS_RESOURCE + "/" + spiFile;
                Enumeration<URL> urls = getClass().getClassLoader().getResources(spiResource);
                if (urls.hasMoreElements()) {
                    jdbcCollectExtensions(urls, bucket);
                } else {
                    LogUtils.debug("Cannot find '{}'", spiResource);
                }
            }

            debugExtensions(bucket);

            result.put(null, bucket);
        } catch (IOException | URISyntaxException e) {
            LogUtils.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, Set<String>> readPluginsStorages() {
        LogUtils.debug("Reading extensions storages from plugins");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            LogUtils.debug("Reading extensions storages for plugin '{}'", pluginId);
            final Set<String> bucket = new HashSet<>();
            try {
                for (String spiFile : SPI_FILES) {
                    String spiResource = EXTENSIONS_RESOURCE + "/" + spiFile;
                    Enumeration<URL> urls = ((PluginClassLoader) plugin.getPluginClassLoader()).findResources(spiResource);
                    if (urls.hasMoreElements()) {
                        jdbcCollectExtensions(urls, bucket);
                    } else {
                        LogUtils.debug("Cannot find '{}'", spiResource);
                    }
                }

                debugExtensions(bucket);

                result.put(pluginId, bucket);
            } catch (IOException | URISyntaxException e) {
                LogUtils.error(e.getMessage(), e);
            }
        }

        return result;
    }

    private void jdbcCollectExtensions(Enumeration<URL> urls, Set<String> bucket) throws URISyntaxException, IOException {
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            LogUtils.debug("Read '{}'", url.getFile());
            jdbcCollectExtensions(url, bucket);
        }
    }

    private void jdbcCollectExtensions(URL url, Set<String> bucket) throws URISyntaxException, IOException {
        Path extensionPath;

        if (url.toURI().getScheme().equals("jar")) {
            extensionPath = FileUtils.getPath(url.toURI(), EXTENSIONS_RESOURCE + "/" + url.getPath().substring(url.getPath().lastIndexOf("/") + 1));
        } else {
            extensionPath = Paths.get(url.toURI());
        }

        try {
            bucket.addAll(jdbcReadExtensions(extensionPath));
        } finally {
            FileUtils.closePath(extensionPath);
        }
    }

    private Set<String> jdbcReadExtensions(Path extensionPath) throws IOException {
        final Set<String> result = new HashSet<>();
        Files.walkFileTree(extensionPath, Collections.emptySet(), 1, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LogUtils.debug("Read '{}'", file);
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    ExtensionStorage.read(reader, result);
                }

                return FileVisitResult.CONTINUE;
            }

        });

        return result;
    }

}
