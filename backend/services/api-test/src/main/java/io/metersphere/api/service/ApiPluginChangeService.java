package io.metersphere.api.service;

import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.JmeterElementConverterRegister;
import io.metersphere.plugin.api.spi.AbstractApiPlugin;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.JmeterElementConverter;
import io.metersphere.plugin.api.spi.MsTestElement;
import io.metersphere.sdk.plugin.MsPluginManager;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.PluginChangeService;
import io.metersphere.system.service.PluginLoadService;
import org.pf4j.Plugin;
import org.springframework.stereotype.Service;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:55
 */
@Service
public class ApiPluginChangeService implements PluginChangeService {
    @Override
    public void handlePluginLoad(String pluginId) {
        MsPluginManager msPluginManager = CommonBeanFactory.getBean(PluginLoadService.class).getMsPluginManager();
        Plugin plugin = msPluginManager.getPlugin(pluginId).getPlugin();
        try {
            if (plugin instanceof AbstractApiPlugin) {
                // 注册序列化类
                msPluginManager.getExtensionClasses(MsTestElement.class, pluginId)
                        .forEach(ApiDataUtils::setResolver);

                // 注册插件元素解析器
                msPluginManager.getExtensionClasses(JmeterElementConverter.class, pluginId)
                        .forEach(item -> JmeterElementConverterRegister.register((Class<? extends AbstractJmeterElementConverter<? extends MsTestElement>>) item));
            }
        } catch (Exception e) {
            LogUtils.error("注册接口插件实现类失败：", e);
        }
    }
}
