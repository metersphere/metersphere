package io.metersphere.system.controller;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.UserPlatformAccountService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/platform")
@Tag(name = "系统设置-个人中心-三方平台账号")
public class UserPlatformAccountController {

    @Resource
    private OrganizationService organizationService;
    @Resource
    private UserPlatformAccountService userPlatformAccountService;

    @GetMapping("/account/info")
    @Operation(summary = "系统设置-个人中心-获取三方平台账号信息(插件信息)")
    public Map<String, Object> getAccountInfoList() {
        return userPlatformAccountService.getAccountInfoList();
    }

    @PostMapping("/validate/{pluginId}/{orgId}")
    @Operation(summary = "系统设置-个人中心-校验用户集成信息")
    @Parameters({
            @Parameter(name = "pluginId", description = "插件ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "orgId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    public void validate(@PathVariable String pluginId, @PathVariable String orgId,
                         @RequestBody
                         @NotEmpty
                         @Schema(description = "用户配置集成信息", requiredMode = Schema.RequiredMode.REQUIRED)
                         Map<String, String> userPlatformConfig) {
        userPlatformAccountService.validate(pluginId, orgId, userPlatformConfig);
    }

    @PostMapping("/save")
    @Operation(summary = "系统设置-个人中心-保存三方平台账号(这里的应该是插件信息加账号值)")
    public void save(@RequestBody Map<String, Object> platformInfo) {
        userPlatformAccountService.save(platformInfo, SessionUtils.getUserId());
    }

    @GetMapping("/get/{orgId}")
    @Parameter(name = "orgId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Operation(summary = "系统设置-个人中心-获取个人三方平台账号")
    public Map<String, Object> get(@PathVariable String orgId) {
        return userPlatformAccountService.get(SessionUtils.getUserId(), orgId);
    }

    @GetMapping("/switch-option")
    @Operation(summary = "个人中心-三方平台-组织下拉选项")
    public List<OptionDTO> getSwitchOption() {
        return organizationService.getSwitchOption(SessionUtils.getUserId());
    }
}
