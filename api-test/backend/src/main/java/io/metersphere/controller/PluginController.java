package io.metersphere.controller;

import io.metersphere.api.dto.plugin.PluginDTO;
import io.metersphere.api.dto.plugin.PluginRequest;
import io.metersphere.service.PluginService;
import io.metersphere.base.domain.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/plugin")
public class PluginController {
    @Resource
    private PluginService pluginService;

    @GetMapping("/list")
    public List<PluginDTO> list(String name) {
        return pluginService.list(name);
    }

    @GetMapping("/get/{id}")
    public Plugin get(@PathVariable String id) {
        return pluginService.get(id);
    }

    @PostMapping(value = "/customMethod")
    public Object customMethod(@RequestBody PluginRequest request) {
        return pluginService.customMethod(request);
    }

}
