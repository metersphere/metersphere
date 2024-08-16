package io.metersphere.system.controller;


import io.metersphere.system.domain.UserLocalConfig;
import io.metersphere.system.dto.UserLocalConfigAddRequest;
import io.metersphere.system.dto.UserLocalConfigUpdateRequest;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.UserLocalConfigLogService;
import io.metersphere.system.service.UserLocalConfigService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user/local/config")
@Tag(name = "系统设置-个人中心-我的设置-本地执行")
public class UserLocalConfigController {
    @Value("${spring.messages.default-locale}")
    private String defaultLocale;

    @Resource
    private UserLocalConfigService userLocalConfigService;

    @PostMapping("/add")
    @Operation(summary = "系统设置-个人中心-我的设置-本地执行-添加本地执行")
    public UserLocalConfig add(@Validated @RequestBody UserLocalConfigAddRequest request) {
        return userLocalConfigService.add(request, SessionUtils.getUserId());
    }

    @GetMapping("/get")
    @Operation(summary = "系统设置-个人中心-我的设置-本地执行-获取本地执行")
    public List<UserLocalConfig> get() {
        return userLocalConfigService.get(SessionUtils.getUserId());
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-个人中心-我的设置-本地执行-启用本地执行")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.enableLog(#id)", msClass = UserLocalConfigLogService.class)
    public void enable(@PathVariable String id) {
        userLocalConfigService.enable(id);
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-个人中心-我的设置-本地执行-禁用本地执行")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.disableLog(#id)", msClass = UserLocalConfigLogService.class)
    public void disable(@PathVariable String id) {
        userLocalConfigService.disable(id);
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-个人中心-我的设置-本地执行-更新本地执行")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = UserLocalConfigLogService.class)
    public void update(@Validated @RequestBody UserLocalConfigUpdateRequest request) {
        userLocalConfigService.update(request);
    }

    @GetMapping(value = "/default-locale")
    public String defaultLocale() {
        SessionUser user = SessionUtils.getUser();
        String language = Optional.ofNullable(user)
                .map(SessionUser::getLanguage)
                .filter(StringUtils::isNotBlank)
                .orElse(defaultLocale);

        return language.replace("_", "-");
    }
}
