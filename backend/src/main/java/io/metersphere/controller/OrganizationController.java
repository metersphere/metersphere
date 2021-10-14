package io.metersphere.controller;

import io.metersphere.dto.OrganizationResource;
import io.metersphere.service.OrganizationService;
import io.metersphere.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("organization")
@RestController
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;
    @Resource
    private UserService userService;


    @GetMapping("/list/resource/{groupId}/{type}")
    public OrganizationResource listResource(@PathVariable String groupId, @PathVariable String type) {
        return organizationService.listResource(groupId, type);
    }
}
