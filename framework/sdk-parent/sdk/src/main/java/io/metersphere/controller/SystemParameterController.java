package io.metersphere.controller;

import io.metersphere.base.domain.SystemHeader;
import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.UserHeader;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.request.HeaderRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.domain.MailInfo;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.SystemParameterService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/system")
public class SystemParameterController {
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private Environment env;
    @Resource
    private BaseUserService baseUserService;


    @PostMapping("/edit/email")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.UPDATE, title = "邮件设置", beforeEvent = "#msClass.getMailLogDetails()", content = "#msClass.getMailLogDetails()", msClass = SystemParameterService.class)
    public void editMail(@RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.editMail(systemParameter);
    }

    @PostMapping("/testConnection")
    public void testConnection(@RequestBody HashMap<String, String> hashMap) {
        systemParameterService.testConnection(hashMap);
    }

    @GetMapping("/version")
    public String getVersion() {
        return systemParameterService.getVersion();
    }

    @GetMapping("/theme")
    public String getTheme() {
        return systemParameterService.getValue("ui.theme");
    }

    @GetMapping("timeout")
    public long getTimeout() {
        return env.getProperty("spring.session.timeout", Duration.class, Duration.ofHours(12)).getSeconds(); // 默认43200s, 12个小时
    }

    @GetMapping("/mail/info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public MailInfo mailInfo() {
        return systemParameterService.mailInfo(ParamConstants.Classify.MAIL.getValue());
    }

    @GetMapping("/base/info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public BaseSystemConfigDTO getBaseInfo() {
        return systemParameterService.getBaseInfo();
    }

    @PostMapping("/system/header")
    public SystemHeader getHeader(@RequestBody SystemHeader systemHeader) {
        return systemParameterService.getHeader(systemHeader.getType());
    }

    @PostMapping("/save/base")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getBaseLogDetails()", content = "#msClass.getBaseLogDetails()", msClass = SystemParameterService.class)
    public void saveBaseInfo(@RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.saveBaseInfo(systemParameter);
    }

    @GetMapping("/save/baseurl")
    public void saveBaseurl(@RequestParam("baseurl") String baseurl) {
        systemParameterService.saveBaseurl(baseurl);
    }

    @PostMapping("/save/ldap")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails()", content = "#msClass.getLogDetails()", msClass = SystemParameterService.class)
    public void saveLdap(@RequestBody List<SystemParameter> systemParameter) {
        systemParameterService.saveLdap(systemParameter);
    }

    @GetMapping("/ldap/info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ)
    public LdapInfo getLdapInfo() {
        return systemParameterService.getLdapInfo(ParamConstants.Classify.LDAP.getValue());
    }

    @PostMapping("save/header")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_EDIT)
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.UPDATE, title = "显示设置")
    public void saveHeader(@RequestBody UserHeader userHeader) {
        systemParameterService.saveHeader(userHeader);
    }

    @PostMapping("/header/info")
    public UserHeader getHeaderInfo(@RequestBody HeaderRequest headerRequest) {
        return systemParameterService.queryUserHeader(headerRequest);
    }

    @GetMapping("/user/size")
    public long getSystemUserCount() {
        return baseUserService.getUserSize();
    }

    @GetMapping("/get/info/{key}")
    public SystemParameter getInfo(@PathVariable String key) {
        return systemParameterService.getInfo(key);
    }

    @PostMapping("/edit/info")
    @RequiresPermissions(PermissionConstants.SYSTEM_SETTING_READ_EDIT)
    public SystemParameter editInfo(@RequestBody SystemParameter systemParameter) {
        systemParameterService.editInfo(systemParameter);
        return systemParameter;
    }


}
