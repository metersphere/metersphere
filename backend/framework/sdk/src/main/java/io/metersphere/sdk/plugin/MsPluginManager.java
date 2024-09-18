package io.metersphere.sdk.plugin;

import org.pf4j.*;

/**
 * @author jianxing
 * 为了支持加载使用 SPI 机制加载的 jdbc 驱动
 * 这里加入自定义的 MsServiceProviderExtensionFinder 和 JdbcDriverPluginDescriptorFinder
 */
public class MsPluginManager extends DefaultPluginManager {
    @Override
    protected ExtensionFinder createExtensionFinder() {
        DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
        // 添加 SPI 发现机制，只发现指定实现类
        extensionFinder.add(new MsServiceProviderExtensionFinder(this));
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
