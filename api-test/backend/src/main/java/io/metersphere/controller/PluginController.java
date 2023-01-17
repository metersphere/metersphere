package io.metersphere.controller;

import io.metersphere.api.dto.plugin.PluginDTO;
import io.metersphere.api.dto.plugin.PluginRequest;
import io.metersphere.base.domain.Plugin;
import io.metersphere.service.PluginService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/plugin")
public class PluginController {
    @Resource
    private PluginService pluginService;

    @GetMapping("/list")
    public List<PluginDTO> list() {
        return pluginService.getPluginList();
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
