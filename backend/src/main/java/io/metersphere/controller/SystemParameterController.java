package io.metersphere.controller;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.domain.UserHeader;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.controller.request.HeaderRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.notice.domain.MailInfo;
import io.metersphere.service.SystemParameterService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/system")
public class SystemParameterController {
    @Resource
    private SystemParameterService SystemParameterService;

    @PostMapping("/edit/email")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void editMail(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.editMail(systemParameter);
    }

    @PostMapping("/testConnection")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void testConnection(@RequestBody HashMap<String, String> hashMap) {
        SystemParameterService.testConnection(hashMap);
    }

    @GetMapping("/version")
    public String getVersion() {
        return SystemParameterService.getVersion();
    }

    @GetMapping("/mail/info")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public MailInfo mailInfo() {
        return SystemParameterService.mailInfo(ParamConstants.Classify.MAIL.getValue());
    }

    @GetMapping("/base/info")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public BaseSystemConfigDTO getBaseInfo () {
        return SystemParameterService.getBaseInfo();
    }

    @PostMapping("/save/base")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void saveBaseInfo (@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.saveBaseInfo(systemParameter);
    }

    @PostMapping("/save/ldap")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public void saveLdap(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.saveLdap(systemParameter);
    }

    @GetMapping("/ldap/info")
    @RequiresRoles(value = {RoleConstants.ADMIN})
    public LdapInfo getLdapInfo() {
        return SystemParameterService.getLdapInfo(ParamConstants.Classify.LDAP.getValue());
    }

    @PostMapping("save/header")
    public void saveHeader(@RequestBody UserHeader userHeader) {
        SystemParameterService.saveHeader(userHeader);
    }

    @PostMapping("/header/info")
    public UserHeader getHeaderInfo(@RequestBody HeaderRequest headerRequest) {
        return SystemParameterService.queryUserHeader(headerRequest);
    }
}
