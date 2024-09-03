package io.metersphere.system.controller;


import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.dto.sdk.BaseCleanConfigDTO;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.EMailInfoDto;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.SystemParameterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@Tag(name = "系统设置-系统-系统参数-基础设置")
@RequestMapping("/system/parameter")
public class SystemParameterController {

    @Resource
    SystemParameterService systemParameterService;


    @PostMapping("/save/base-info")
    @Operation(summary = "系统设置-系统-系统参数-基础设置-基本信息-保存")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateBaseLog(#systemParameter)", msClass = SystemParameterService.class)
    public void saveBaseParameter(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.saveBaseInfo(systemParameter);
    }


    @GetMapping("/get/base-info")
    @Operation(summary = "系统设置-系统-系统参数-基本设置-基本信息-获取")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public BaseSystemConfigDTO getBaseInfo() {
        return systemParameterService.getBaseInfo();
    }


    @GetMapping("/get/email-info")
    @Operation(summary = "系统设置-系统-系统参数-基本设置-邮件设置-获取邮件信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public EMailInfoDto getEmailInfo() {
        return systemParameterService.getEmailInfo();
    }


    @PostMapping("/edit/email-info")
    @Operation(summary = "系统设置-系统-系统参数-基本设置-邮件设置-保存邮件信息")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#systemParameter)", msClass = SystemParameterService.class)
    public void editEMailInfo(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editEMailInfo(systemParameter);
    }


    @PostMapping("/test/email")
    @Operation(summary = "系统设置-系统-系统参数-基本设置-邮件设置-测试连接")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public void testEmailConnection(@RequestBody HashMap<String, String> hashMap) {
        systemParameterService.testEmailConnection(hashMap);
    }


    @GetMapping("/save/base-url")
    @Operation(summary = "系统设置-系统-系统参数-默认站点替换接口")
    public void saveBaseurl(@RequestParam("baseUrl") String baseUrl) {
        systemParameterService.saveBaseUrl(baseUrl);
    }


    @PostMapping("/edit/clean-config")
    @Operation(summary = "系统设置-系统-系统参数-内存清理-保存")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.cleanOperationConfigLog(#systemParameter)", msClass = SystemParameterService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN_READ_UPDATE)
    public void editLogConfig(@RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editLogConfig(systemParameter);
    }


    @GetMapping("/get/clean-config")
    @Operation(summary = "系统设置-系统-系统参数-基本设置-内存清理-获取")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_MEMORY_CLEAN_READ)
    public BaseCleanConfigDTO getLogConfigInfo() {
        return systemParameterService.getLogConfigInfo();
    }


    @GetMapping("/get/api-concurrent-config")
    @Operation(summary = "系统设置-系统-系统参数-单接口任务并发数-获取")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ)
    public String getApiConcurrentConfig() {
        return systemParameterService.getApiConcurrentConfig();
    }

    @PostMapping("/edit/upload-config")
    @Operation(summary = "系统设置-系统-系统参数-基本设置-文件限制-保存")
    @RequiresPermissions(PermissionConstants.SYSTEM_PARAMETER_SETTING_BASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#systemParameter)", msClass = SystemParameterService.class)
    public void editUploadConfigInfo(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editUploadConfigInfo(systemParameter);
    }
}
