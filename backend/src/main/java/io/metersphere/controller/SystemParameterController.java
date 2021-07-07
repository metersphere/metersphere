package io.metersphere.controller;

import io.metersphere.base.domain.SystemHeader;
import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.UserHeader;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.controller.request.HeaderRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.domain.MailInfo;
import io.metersphere.service.SystemParameterService;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/system")
public class SystemParameterController {
    @Resource
    private SystemParameterService SystemParameterService;
    @Resource
    private Environment env;

    @PostMapping("/edit/email")
    @MsAuditLog(module = "system_parameter_setting", type = OperLogConstants.UPDATE, title = "邮件设置", beforeEvent = "#msClass.getMailLogDetails()", content = "#msClass.getMailLogDetails()", msClass = SystemParameterService.class)
    public void editMail(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.editMail(systemParameter);
    }

    @PostMapping("/testConnection")
    public void testConnection(@RequestBody HashMap<String, String> hashMap) {
        SystemParameterService.testConnection(hashMap);
    }

    @GetMapping("/version")
    public String getVersion() {
        return SystemParameterService.getVersion();
    }

    @GetMapping("/theme")
    public String getTheme() {
        return SystemParameterService.getValue("ui.theme");
    }

    @GetMapping("timeout")
    public long getTimeout() {
        return env.getProperty("session.timeout", Long.class, 43200L); // 默认43200s, 12个小时
    }

    @GetMapping("/mail/info")
    public MailInfo mailInfo() {
        return SystemParameterService.mailInfo(ParamConstants.Classify.MAIL.getValue());
    }

    @GetMapping("/base/info")
    public BaseSystemConfigDTO getBaseInfo() {
        return SystemParameterService.getBaseInfo();
    }

    @PostMapping("/system/header")
    public SystemHeader getHeader(@RequestBody SystemHeader systemHeader) {
        return SystemParameterService.getHeader(systemHeader.getType());
    }

    @PostMapping("/save/base")
    @MsAuditLog(module = "system_parameter_setting", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getBaseLogDetails()", content = "#msClass.getBaseLogDetails()", msClass = SystemParameterService.class)
    public void saveBaseInfo(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.saveBaseInfo(systemParameter);
    }

    @GetMapping("/save/baseurl")
    public void saveBaseurl(@RequestParam("baseurl") String baseurl) {
        SystemParameterService.saveBaseurl(baseurl);
    }

    @PostMapping("/save/ldap")
    @MsAuditLog(module = "system_parameter_setting", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails()", content = "#msClass.getLogDetails()", msClass = SystemParameterService.class)
    public void saveLdap(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.saveLdap(systemParameter);
    }

    @GetMapping("/ldap/info")
    public LdapInfo getLdapInfo() {
        return SystemParameterService.getLdapInfo(ParamConstants.Classify.LDAP.getValue());
    }

    @PostMapping("save/header")
    @MsAuditLog(module = "system_parameter_setting", type = OperLogConstants.UPDATE, title = "显示设置")
    public void saveHeader(@RequestBody UserHeader userHeader) {
        SystemParameterService.saveHeader(userHeader);
    }

    @PostMapping("/header/info")
    public UserHeader getHeaderInfo(@RequestBody HeaderRequest headerRequest) {
        return SystemParameterService.queryUserHeader(headerRequest);
    }
}
