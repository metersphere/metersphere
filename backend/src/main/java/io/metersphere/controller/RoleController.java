package io.metersphere.controller;

import io.metersphere.base.domain.Role;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.service.RoleService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("role")
@RestController
public class RoleController {

    @Resource
    private RoleService roleService;

    @GetMapping("/list/{sign}")
    public List<Role> getRoleList(@PathVariable String sign) {
        return roleService.getRoleList(sign);
    }

    @GetMapping("/all")
    public List<Role> getAllRole() {
        return roleService.getAllRole();
    }

}
