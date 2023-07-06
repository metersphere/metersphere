package io.metersphere.system.controller;


import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.EMailInfoDto;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.SystemParameterService;
import io.metersphere.system.domain.SystemParameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@Tag(name = "系统参数")
@RequestMapping("/system/parameter")
public class SystemParameterController {

    @Resource
    SystemParameterService systemParameterService;


    @PostMapping("/save/base-info")
    @Operation(summary = "保存基本信息")
    @RequiresPermissions(value= {PermissionConstants.SYSTEM_SETTING_READ_UPDATE, PermissionConstants.SYSTEM_SETTING_READ_CREAT}, logical = Logical.OR)
    @Log(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PARAMETER_SETTING, details = "基本配置", sourceId = "#systemParameter.get(0).paramKey")
    public void saveBaseParameter(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.saveBaseInfo(systemParameter);
    }


    @GetMapping("/get/base-info")
    @Operation(summary = "获取基本信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public BaseSystemConfigDTO getBaseInfo() {
        return systemParameterService.getBaseInfo();
    }


    @GetMapping("/get/email-info")
    @Operation(summary = "获取邮件信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public EMailInfoDto getEmailInfo() {
        return systemParameterService.getEmailInfo();
    }


    @PostMapping("/edit/email-info")
    @Operation(summary = "保存邮件信息")
    @RequiresPermissions(value= {PermissionConstants.SYSTEM_SETTING_READ_UPDATE, PermissionConstants.SYSTEM_SETTING_READ_CREAT}, logical = Logical.OR)
    @Log(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PARAMETER_SETTING, details = "邮件配置", sourceId = "#systemParameter.get(0).paramKey")
    public void editEMailInfo(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editEMailInfo(systemParameter);
    }


    @PostMapping("/test/email")
    @Operation(summary = "测试连接")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public void testEmailConnection(@RequestBody HashMap<String, String> hashMap) {
        systemParameterService.testEmailConnection(hashMap);
    }

}
