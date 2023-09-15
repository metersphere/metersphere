package io.metersphere.system.plugin;

import org.pf4j.*;

/**
 * @author jianxing
 * 为了支持加载使用 SPI 机制加载的 jdbc 驱动
 * 这里加入自定义的 JdbcDriverServiceProviderExtensionFinder 和 JdbcDriverPluginDescriptorFinder
 */
public class MsPluginManager extends DefaultPluginManager {
    @Override
    protected ExtensionFinder createExtensionFinder() {
        DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
        extensionFinder.addServiceProviderExtensionFinder();
        extensionFinder.add(new JdbcDriverServiceProviderExtensionFinder(this));
        return extensionFinder;
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        // 需要保证 JdbcDriverPluginDescriptorFinder 在 ManifestPluginDescriptorFinder 之前解析
        return (new CompoundPluginDescriptorFinder())
                .add(new PropertiesPluginDescriptorFinder())
                .add(new JdbcDriverPluginDescriptorFinder())
                .add(new ManifestPluginDescriptorFinder());
    }
}
