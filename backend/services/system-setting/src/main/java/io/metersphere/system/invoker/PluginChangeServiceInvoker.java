package io.metersphere.system.invoker;

import io.metersphere.system.service.PluginChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:48
 */
@Component
public class PluginChangeServiceInvoker implements PluginChangeService {
    private final List<PluginChangeService> pluginChangeServices;


    @Autowired
    public PluginChangeServiceInvoker(List<PluginChangeService> services) {
        this.pluginChangeServices = services;
    }

    @Override
    public void handlePluginLoad(String pluginId) {
        this.pluginChangeServices.forEach(service -> service.handlePluginLoad(pluginId));
    }

    @Override
    public void handlePluginUnLoad(String pluginId) {
        this.pluginChangeServices.forEach(service -> service.handlePluginUnLoad(pluginId));
    }
}
