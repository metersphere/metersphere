package io.metersphere.system.controller;


import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.dto.EMailInfoDto;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.SystemParameterService;
import io.metersphere.system.domain.SystemParameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
    @Operation(summary = "系统参数-基础设置-基本信息-保存")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateBaseLog(#systemParameter)", msClass = SystemParameterService.class)
    public void saveBaseParameter(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.saveBaseInfo(systemParameter);
    }


    @GetMapping("/get/base-info")
    @Operation(summary = "系统参数-基本设置-基本信息-获取")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public BaseSystemConfigDTO getBaseInfo() {
        return systemParameterService.getBaseInfo();
    }


    @GetMapping("/get/email-info")
    @Operation(summary = "系统参数-基本设置-邮件设置-获取邮件信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public EMailInfoDto getEmailInfo() {
        return systemParameterService.getEmailInfo();
    }


    @PostMapping("/edit/email-info")
    @Operation(summary = "系统参数-基本设置-邮件设置-保存邮件信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#systemParameter)", msClass = SystemParameterService.class)
    public void editEMailInfo(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editEMailInfo(systemParameter);
    }


    @PostMapping("/test/email")
    @Operation(summary = "系统参数-基本设置-邮件设置-测试连接")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public void testEmailConnection(@RequestBody HashMap<String, String> hashMap) {
        systemParameterService.testEmailConnection(hashMap);
    }


    @GetMapping("/save/base-url")
    @Operation(summary = "系统参数-默认站点替换接口")
    public void saveBaseurl(@RequestParam("baseUrl") String baseUrl) {
        systemParameterService.saveBaseUrl(baseUrl);
    }

}
