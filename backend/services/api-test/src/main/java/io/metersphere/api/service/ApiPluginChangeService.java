package io.metersphere.api.service;

import com.thoughtworks.xstream.converters.Converter;
import io.metersphere.api.parser.jmeter.xstream.MsSaveService;
import io.metersphere.api.parser.ms.MsElementConverterRegister;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.parser.jmeter.JmeterElementConverterRegister;
import io.metersphere.plugin.api.spi.*;
import io.metersphere.sdk.plugin.MsPluginManager;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.PluginChangeService;
import io.metersphere.system.service.PluginLoadService;
import org.apache.jmeter.testelement.TestElement;
import org.pf4j.Plugin;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                // 注册序列化类，获取最新的实现类，重新注册
                List<Class<? extends MsTestElement>> extensionClasses = msPluginManager.getExtensionClasses(MsTestElement.class);
                List<Class<?>> clazzList = new ArrayList<>(extensionClasses.size());
                extensionClasses.forEach(clazzList::add);
                ApiDataUtils.setResolvers(clazzList);

                // 注册插件 JmeterElementConverter 解析器
                msPluginManager.getExtensionClasses(JmeterElementConverter.class, pluginId)
                        .forEach(item -> JmeterElementConverterRegister.register((Class<? extends AbstractJmeterElementConverter<? extends MsTestElement>>) item));

                // 注册插件 MsElementConverter 解析器
                msPluginManager.getExtensionClasses(MsElementConverter.class, pluginId)
                        .forEach(item -> MsElementConverterRegister.register((Class<? extends AbstractMsElementConverter<? extends TestElement>>) item));

                // 注册插件 xstream xml 序列化解析器
                msPluginManager.getExtensionClasses(Converter.class, pluginId)
                                .forEach(MsSaveService::registerConverter);

                // 加载插件 jmeter 元素别名
                MsSaveService.loadPluginAliasProperties(msPluginManager.getPluginClassLoader(pluginId));
            }
        } catch (Exception e) {
            LogUtils.error("注册接口插件实现类失败：{}", e);
        }
    }

    @Override
    public void handlePluginUnLoad(String pluginId) {
        MsPluginManager msPluginManager = CommonBeanFactory.getBean(PluginLoadService.class).getMsPluginManager();
        Plugin plugin = msPluginManager.getPlugin(pluginId).getPlugin();
        try {
            if (plugin instanceof AbstractApiPlugin) {
                // 注销插件 JmeterElementConverter 解析器
                msPluginManager.getExtensionClasses(JmeterElementConverter.class, pluginId)
                        .forEach(item -> JmeterElementConverterRegister.unRegister((Class<? extends AbstractJmeterElementConverter<? extends MsTestElement>>) item));

                // 注销插件 MsElementConverter 解析器
                msPluginManager.getExtensionClasses(MsElementConverter.class, pluginId)
                        .forEach(item -> MsElementConverterRegister.unRegister((Class<? extends AbstractMsElementConverter<? extends TestElement>>) item));

                // 注销插件 xstream xml 序列化解析器
                msPluginManager.getExtensionClasses(Converter.class, pluginId)
                        .forEach(MsSaveService::unRegisterConverter);
            }
        } catch (Exception e) {
            LogUtils.error("注销接口插件实现类失败：{}", e);
        }
    }
}
