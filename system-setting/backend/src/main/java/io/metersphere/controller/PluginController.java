package io.metersphere.controller;

import io.metersphere.base.domain.Plugin;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.PluginDTO;
import io.metersphere.request.PluginRequest;
import io.metersphere.service.PluginService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping(value = "/plugin")
public class PluginController {

    @Resource
    private PluginService pluginService;

    @PostMapping("/add/{scenario}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_UPLOAD)
    @MsAuditLog(module = OperLogModule.PLUGIN_MANAGE, type = OperLogConstants.CREATE, title = "#file.getOriginalFilename()", msClass = PluginService.class)
    public void create(@RequestPart(value = "file", required = false) MultipartFile file, @PathVariable String scenario) {
        if (file == null) {
            MSException.throwException("上传文件/执行入口为空");
        }
        pluginService.addPlugin(file, scenario);
    }

    @GetMapping("/list")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_READ)
    public List<PluginDTO> list(String name) {
        return pluginService.list(name);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_READ)
    public Plugin get(@PathVariable String id) {
        return pluginService.get(id);
    }

    @GetMapping("/delete/{scenario}/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_DEL)
    @MsAuditLog(module = OperLogModule.PLUGIN_MANAGE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = PluginService.class)
    public void delete(@PathVariable String scenario, @PathVariable String id) {
        pluginService.delete(scenario, id);
    }

    @PostMapping("/custom/method")
    public Object customMethod(@RequestBody PluginRequest request) {
        return pluginService.customMethod(request);
    }
}
