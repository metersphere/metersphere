package io.metersphere.ldap.controller;

import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.domain.Person;
import io.metersphere.ldap.service.LdapService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/ldap")
public class LdapController {

    @Resource
    private UserService userService;
    @Resource
    private LdapService ldapService;
    @Resource
    private SystemParameterService systemParameterService;

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {

        String isOpen = systemParameterService.getValue(ParamConstants.LDAP.OPEN.getValue());
        if (StringUtils.isBlank(isOpen) || StringUtils.equals(Boolean.FALSE.toString(), isOpen)) {
            MSException.throwException(Translator.get("ldap_authentication_not_enabled"));
        }

        Person person = ldapService.authenticate(request);

        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LDAP.name());

        String username = request.getUsername();

        String email = person.getEmail();

        if (StringUtils.isBlank(email)) {
            MSException.throwException(Translator.get("login_fail_email_null"));
        }

        User u = userService.selectUser(request.getUsername());
        if (u == null) {
            User user = new User();
            user.setId(username);
            user.setName(username);
            user.setEmail(email);
            user.setSource(UserSource.LDAP.name());
            userService.addLdapUser(user);
        }

        return userService.login(request);
    }

    @PostMapping("/test/connect")
    public void testConnect() {
        ldapService.testConnect();
    }

    @PostMapping("/test/login")
    public void testLogin(@RequestBody LoginRequest request) {
        ldapService.authenticate(request);
    }

}
