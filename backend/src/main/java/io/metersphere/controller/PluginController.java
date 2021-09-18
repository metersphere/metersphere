package io.metersphere.controller;

import io.metersphere.base.domain.Plugin;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.PluginDTO;
import io.metersphere.controller.request.PluginRequest;
import io.metersphere.service.PluginService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/plugin")
public class PluginController {

    @Resource
    private PluginService pluginService;

    @PostMapping("/add")
    public String create(@RequestPart(value = "file", required = false) MultipartFile file) {
        if (file == null) {
            MSException.throwException("上传文件/执行入口为空");
        }
        return pluginService.editPlugin(file);
    }

    @GetMapping("/list")
    public List<PluginDTO> list(String name) {
        return pluginService.list(name);
    }

    @GetMapping("/get/{id}")
    public Plugin get(@PathVariable String id) {
        return pluginService.get(id);
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        return pluginService.delete(id);
    }

    @PostMapping(value = "/customMethod")
    public Object customMethod(@RequestBody PluginRequest request) {
        return pluginService.customMethod(request);
    }

}
