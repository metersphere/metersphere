package io.metersphere.system.controller;

import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.dto.request.PlatformOptionRequest;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.file.annotation.FileLimit;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.PluginLogService;
import io.metersphere.system.service.PluginService;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author : jianxing
 * @date : 2023-7-13
 */
@RestController
@RequestMapping("/plugin")
@Tag(name = "系统设置-系统-插件管理")
public class PluginController {

    @Resource
    private PluginService pluginService;

    @GetMapping("/list")
    @Operation(summary = "系统设置-系统-插件管理-获取插件列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_READ)
    public List<PluginDTO> list() {
        return pluginService.list();
    }

    @FileLimit
    @PostMapping("/add")
    @Operation(summary = "系统设置-系统-插件管理-创建插件")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_ADD)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addLog(#request)", msClass = PluginLogService.class)
    public Plugin add(@Validated({Created.class}) @RequestPart(value = "request") PluginUpdateRequest request,
                      @RequestPart(value = "file") MultipartFile file) {
        request.setCreateUser(SessionUtils.getUserId());
        return pluginService.add(request, file);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-插件管理-更新插件")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.updateLog(#request)", msClass = PluginLogService.class)
    public Plugin update(@Validated({Updated.class}) @RequestBody PluginUpdateRequest request) {
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        return pluginService.update(request);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-插件管理-删除插件")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = PluginLogService.class)
    public void delete(@PathVariable String id) {
        pluginService.delete(id);
    }

    @GetMapping("/script/get/{pluginId}/{scriptId}")
    @Operation(summary = "系统设置-系统-插件管理-获取插件对应表单的脚本内容")
    @RequiresPermissions(PermissionConstants.SYSTEM_PLUGIN_READ)
    public String getScript(@PathVariable String pluginId, @PathVariable String scriptId) {
        return pluginService.getScript(pluginId, scriptId);
    }

    @GetMapping("/image/{pluginId}")
    @Operation(summary = "系统设置-系统-插件管理-获取插件的图片资源")
    public void getPluginImg(
            @Schema(description =  "插件ID", requiredMode = Schema.RequiredMode.REQUIRED)
            @PathVariable("pluginId")
            String pluginId,
            @Schema(description =  "图片路径", requiredMode = Schema.RequiredMode.REQUIRED)
            @RequestParam("imagePath")
            String imagePath,
            HttpServletResponse response) throws IOException {
        pluginService.getPluginImg(pluginId, imagePath, response);
    }

    @PostMapping("/options")
    @Operation(summary = "获取插件下拉选项")
    public List<SelectOption> getPluginOptions(@RequestBody PlatformOptionRequest request) {
        return pluginService.getPluginOptions(request);
    }


}