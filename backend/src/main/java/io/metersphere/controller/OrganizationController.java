package io.metersphere.controller;

import io.metersphere.base.domain.Organization;
import io.metersphere.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("organization")
@RestController
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;

    @PostMapping("/add")
    public Organization addOrganization(@RequestBody Organization organization) { return organizationService.addOrganization(organization); }

    @GetMapping("/list")
    public List<Organization> getOrganizationList() { return organizationService.getOrganizationList(); }

    @GetMapping("/delete/{organizationId}")
    public void deleteOrganization(@PathVariable(value = "organizationId") String organizationId) { organizationService.deleteOrganization(organizationId); }

    @PostMapping("/update")
    public void updateOrganization(@RequestBody Organization organization) { organizationService.updateOrganization(organization); }
}
