package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Organization;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
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

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<Organization>> getOrganizationList(@PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, organizationService.getOrganizationList());
    }

    @GetMapping("/delete/{organizationId}")
    public void deleteOrganization(@PathVariable(value = "organizationId") String organizationId) { organizationService.deleteOrganization(organizationId); }

    @PostMapping("/update")
    public void updateOrganization(@RequestBody Organization organization) { organizationService.updateOrganization(organization); }

    @GetMapping("/list/userorg/{userId}")
    public List<Organization> getOrganizationListByUserId(@PathVariable String userId) {
        return organizationService.getOrganizationListByUserId(userId);
    }
}
