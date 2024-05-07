package io.metersphere.sdk.plugin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginRuntimeException;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author jianxing
 * 支持解析 jdbc 驱动中的 MANIFEST.MF
 * 如果使用 ManifestPluginDescriptorFinder 解析，由于 jdbc 驱动没有按照 pf4j 的规范打包
 * 无法解析出 PluginDescriptor ，这里重写后，按照自定义的规则解析
 */
public class JdbcDriverPluginDescriptorFinder extends ManifestPluginDescriptorFinder {

    private String driverClass;

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath, new LinkOption[0]) && (Files.isDirectory(pluginPath, new LinkOption[0]) || FileUtils.isZipOrJarFile(pluginPath))
                && hasDriverFile(pluginPath); // 这里前判断时候包含 META-INF/services/java.sql.Driver 文件，不包含则走 ManifestPluginDescriptorFinder
    }

    /**
     * 判断是否有SPI的驱动实现类
     * @param jarPath
     * @return
     */
    protected boolean hasDriverFile(Path jarPath) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry jarEntry = jar.getJarEntry("META-INF/services/java.sql.Driver");
            if (jarEntry == null) {
                return false;
            }
            try (InputStream inputStream = jar.getInputStream(jarEntry)) {
                // 获取SPI中定义的类名
                driverClass = IOUtils.toString(inputStream);
            }
            return true;
        } catch (IOException e) {
            throw new PluginRuntimeException(e, "Cannot read META-INF/services/java.sql.Driver from {}", jarPath);
        }
    }

    @Override
    protected PluginDescriptor createPluginDescriptor(Manifest manifest) {
        JdbcDriverPluginDescriptor pluginDescriptor = this.createJdbcDriverPluginDescriptorInstance();
        Attributes attributes = manifest.getMainAttributes();
        // 将类名作为ID
        String id = driverClass;
        pluginDescriptor.setPluginId(id.split("\n")[0]);
        if (StringUtils.isBlank(id)) {
            return null;
        }
        String description = attributes.getValue("Plugin-Description");
        if (StringUtils.isBlank(description)) {
            pluginDescriptor.setPluginDescription("");
        } else {
            pluginDescriptor.setPluginDescription(description);
        }
        String version = attributes.getValue("Implementation-Version");
        if (StringUtils.isNotBlank(version)) {
            pluginDescriptor.setPluginVersion(version);
        } else {
            // 如果没有 Implementation-Version 属性，就查找是否有版本相关的属性，设置为版本
            for (Object key : attributes.keySet()) {
                Object var = attributes.get(key);
                if (key != null && var != null && StringUtils.containsIgnoreCase(key.toString(), "Version")) {
                    pluginDescriptor.setPluginVersion(var.toString());
                    break;
                }
            }
        }

        if (StringUtils.isBlank(version)) {
            // 没有版本相关的属性，就用id兜底
            pluginDescriptor.setPluginVersion(id);
        }

        String provider = attributes.getValue("Implementation-Vendor");
        pluginDescriptor.setProvider(provider);
        return pluginDescriptor;
    }

    protected JdbcDriverPluginDescriptor createJdbcDriverPluginDescriptorInstance() {
        return new JdbcDriverPluginDescriptor();
    }
}
