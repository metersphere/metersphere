package io.metersphere.api.controller;

import io.metersphere.api.dto.automation.ApiApiRequest;
import io.metersphere.api.dto.automation.SaveApiTagRequest;
import io.metersphere.api.service.ApiTagService;
import io.metersphere.base.domain.ApiTag;
import io.metersphere.commons.constants.RoleConstants;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/tag")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
public class ApiTagController {

    @Resource
    ApiTagService apiTagService;

    @PostMapping("/list")
    public List<ApiTag> list(@RequestBody ApiApiRequest request) {
        return apiTagService.list(request);
    }

    @PostMapping(value = "/create")
    public void create(@RequestBody SaveApiTagRequest request) {
        apiTagService.create(request);
    }

    @PostMapping(value = "/update")
    public void update(@RequestBody SaveApiTagRequest request) {
        apiTagService.update(request);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        apiTagService.delete(id);
    }

}

