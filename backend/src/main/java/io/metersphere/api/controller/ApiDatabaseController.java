package io.metersphere.api.controller;

import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.service.APIDatabaseService;
import io.metersphere.commons.constants.RoleConstants;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/database")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
public class ApiDatabaseController {

    @Resource
    APIDatabaseService apiDatabaseService;

    @PostMapping("/validate")
    public void validate(@RequestBody DatabaseConfig databaseConfig) {
        apiDatabaseService.validate(databaseConfig);
    }

}
