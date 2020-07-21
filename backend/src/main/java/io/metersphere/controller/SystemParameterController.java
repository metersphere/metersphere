package io.metersphere.controller;

import io.metersphere.base.domain.SystemParameter;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.service.SystemParameterService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping(value = "/system")
@RequiresRoles(RoleConstants.ADMIN)
public class SystemParameterController {
    @Resource
    private SystemParameterService SystemParameterService;

    @PostMapping("/edit/email")
    public void editMail(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.editMail(systemParameter);
    }

    @PostMapping("/testConnection")
    public void testConnection(@RequestBody HashMap<String, String> hashMap) {
        SystemParameterService.testConnection(hashMap);
    }

    @GetMapping("/version")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
    public String getVersion() {
        return SystemParameterService.getVersion();
    }

    @GetMapping("/mail/info")
    public Object mailInfo() {
        return SystemParameterService.mailInfo(ParamConstants.Classify.MAIL.getValue());
    }

    @PostMapping("/save/ldap")
    public void saveLdap(@RequestBody List<SystemParameter> systemParameter) {
        SystemParameterService.saveLdap(systemParameter);
    }

    @GetMapping("/ldap/info")
    public LdapInfo getLdapInfo() {
        return SystemParameterService.getLdapInfo(ParamConstants.Classify.LDAP.getValue());
    }

}
