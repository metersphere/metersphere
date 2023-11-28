package io.metersphere.sdk.plugin;

import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDescriptor;

/**
 * @author jainxing
 * 由于 DefaultPluginDescriptor 中的 set 方法是 protected 的，JdbcDriverPluginDescriptorFinder 无法访问
 * 这里重写一下，让相同包名下的 JdbcDriverPluginDescriptorFinder 可以访问
 */
public class JdbcDriverPluginDescriptor extends DefaultPluginDescriptor {

    @Override
    protected DefaultPluginDescriptor setPluginId(String pluginId) {
        return super.setPluginId(pluginId);
    }

    @Override
    protected PluginDescriptor setPluginDescription(String pluginDescription) {
        return super.setPluginDescription(pluginDescription);
    }

    @Override
    protected PluginDescriptor setPluginClass(String pluginClassName) {
        return super.setPluginClass(pluginClassName);
    }

    @Override
    protected DefaultPluginDescriptor setPluginVersion(String version) {
        return super.setPluginVersion(version);
    }

    @Override
    protected PluginDescriptor setProvider(String provider) {
        return super.setProvider(provider);
    }

    @Override
    protected PluginDescriptor setDependencies(String dependencies) {
        return super.setDependencies(dependencies);
    }

    @Override
    protected PluginDescriptor setRequires(String requires) {
        return super.setRequires(requires);
    }
}
