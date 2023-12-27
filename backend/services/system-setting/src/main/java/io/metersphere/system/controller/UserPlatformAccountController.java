package io.metersphere.system.controller;

import io.metersphere.system.service.*;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/platform")
@Tag(name = "系统设置-个人中心-三方平台账号")
public class UserPlatformAccountController {
    @Resource
    private UserPlatformAccountService userPlatformAccountService;

    @GetMapping("/account/info")
    @Operation(summary = "系统设置-个人中心-获取三方平台账号信息(插件信息)")
    public Map<String, Object> getAccountInfoList() {
        return userPlatformAccountService.getAccountInfoList();
    }

    @PostMapping("/validate/{pluginId}")
    @Operation(summary = "系统设置-个人中心-校验服务集成信息")
    public void validate(@PathVariable String pluginId,
                         @Validated({Updated.class})
                         @RequestBody
                         @NotEmpty
                         @Schema(description = "配置的表单键值对", requiredMode = Schema.RequiredMode.REQUIRED)
                         HashMap<String, String> serviceIntegrationInfo) {
        userPlatformAccountService.validate(pluginId, serviceIntegrationInfo);
    }

    @PostMapping("/save")
    @Operation(summary = "系统设置-个人中心-保存三方平台账号(这里的应该是插件信息加账号值)")
    public void save(@RequestBody Map<String, Object> platformInfo) {
        userPlatformAccountService.save(platformInfo, SessionUtils.getUserId());
    }

    @GetMapping("/get")
    @Operation(summary = "系统设置-个人中心-获取个人三方平台账号")
    public Map<String, Object> get() {
        return userPlatformAccountService.get(SessionUtils.getUserId());
    }


}
