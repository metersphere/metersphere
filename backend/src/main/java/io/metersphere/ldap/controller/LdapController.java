package io.metersphere.ldap.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.ldap.service.LdapService;
import io.metersphere.log.annotation.MsAuditLog;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/ldap")
public class LdapController {

    @Resource
    private LdapService ldapService;

    @PostMapping(value = "/signin")
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.LOGIN, title = "LDAP")
    public ResultHolder login(@RequestBody LoginRequest request) {
        return ldapService.login(request);
    }

    @PostMapping("/test/connect")
    public void testConnect() {
        ldapService.testConnect();
    }

    @PostMapping("/test/login")
    @MsAuditLog(module = OperLogModule.SYSTEM_PARAMETER_SETTING, type = OperLogConstants.LOGIN, title = "LDAP")
    public void testLogin(@RequestBody LoginRequest request) {
        ldapService.authenticate(request);
    }

    @GetMapping("/open")
    public boolean isOpen() {
        return ldapService.isOpen();
    }

}
