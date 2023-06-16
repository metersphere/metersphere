package io.metersphere.system.controller;


import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.EMailInfoDto;
import io.metersphere.sdk.log.annotation.RequestLog;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.SystemParameterService;
import io.metersphere.system.domain.SystemParameter;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/system/parameter")
public class SystemParameterController {

    @Resource
    SystemParameterService systemParameterService;


    /**
     * 基本配置
     *
     * @param systemParameter
     */
    @PostMapping("/save/base-info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_UPDATE)
    @RequestLog(type = OperationLogType.ADD, module = OperationLogModule.SYSTEM_PARAMETER_SETTING, details = "基本配置", sourceId = "#systemParameter.get(0).paramKey")
    public void saveBaseParameter(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.saveBaseInfo(systemParameter);
    }

    @GetMapping("/get/base-info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public BaseSystemConfigDTO getBaseInfo() {
        return systemParameterService.getBaseInfo();
    }


    /**
     * 邮件设置
     *
     * @return
     */
    @GetMapping("/get/email-info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public EMailInfoDto getEmailInfo() {
        return systemParameterService.getEmailInfo();
    }


    @PostMapping("/edit/email-info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_UPDATE)
    @RequestLog(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_PARAMETER_SETTING, details = "邮件配置", sourceId = "#systemParameter.get(0).paramKey")
    public void editEMailInfo(@Validated @RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editEMailInfo(systemParameter);
    }


    /**
     * 邮件测试连接
     *
     * @param hashMap
     */
    @PostMapping("/test/email")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public void testEmailConnection(@RequestBody HashMap<String, String> hashMap) {
        systemParameterService.testEmailConnection(hashMap);
    }

}
