package io.metersphere.ldap.controller;

import io.metersphere.base.domain.UserRole;
import io.metersphere.controller.ResultHolder;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.LdapService;
import io.metersphere.ldap.PersonRepoImpl;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

@RestController
@RequestMapping("/ldap")
public class LdapController {

    @Resource
    private PersonRepoImpl personRepo;
    @Resource
    private UserService userService;
    @Resource
    private LdapService ldapService;

    @GetMapping("/test")
    public List<String> test() {
        return personRepo.getAllPersonNames();
    }

    @GetMapping("/find/{name}")
    public List test(@PathVariable String name) {
        return personRepo.findByName(name);
    }

    @GetMapping("/testUser")
    public void testUser() {
        // TODO LDAP 认证
//        personRepo.authenticate("Administrator@fit2cloud.com", "Calong@2015");
    }

    @PostMapping(value = "/signin")
    public ResultHolder login(@RequestBody LoginRequest request) {
        // TODO 1. LDAP 认证  2. 认证之后， 重新登录系统， 3. 执行其它
        ldapService.authenticate(request);

        return userService.login(request);
    }





}
