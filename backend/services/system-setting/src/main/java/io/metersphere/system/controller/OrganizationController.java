package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.request.OrganizationDeleteRequest;
import io.metersphere.system.request.OrganizationRequest;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @author song-cc-rock
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ)
    public Pager<List<OrganizationDTO>> list(@RequestBody OrganizationRequest organizationRequest) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return new Pager<>();
        }
        Page<Object> page = PageHelper.startPage(organizationRequest.getCurrent(), organizationRequest.getPageSize());
        return PageUtils.setPageInfo(page, organizationService.list(organizationRequest));
    }

    @PostMapping("/list-all")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ)
    public List<OrganizationDTO> listAll(@RequestBody OrganizationRequest organizationRequest) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return Collections.emptyList();
        }
        return organizationService.list(organizationRequest);
    }

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ_ADD)
    public OrganizationDTO add(@Validated({Created.class}) @RequestBody OrganizationDTO organizationDTO) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return new OrganizationDTO();
        }
        organizationDTO.setCreateUser(SessionUtils.getUserId());
        organizationDTO.setUpdateUser(SessionUtils.getUserId());
        return organizationService.add(organizationDTO);
    }

    @PostMapping("/update")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ_UPDATE)
    public void update(@Validated({Updated.class}) @RequestBody OrganizationDTO organizationDTO) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return;
        }
        organizationDTO.setUpdateUser(SessionUtils.getUserId());
        organizationService.update(organizationDTO);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ_DELETE)
    public void delete(@PathVariable String id) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return;
        }
        OrganizationDeleteRequest organizationDeleteRequest = new OrganizationDeleteRequest();
        organizationDeleteRequest.setId(id);
        organizationDeleteRequest.setDeleteUserId(SessionUtils.getUserId());
        organizationService.delete(organizationDeleteRequest);
    }

    @GetMapping("/undelete/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ_DELETE)
    public void undelete(@PathVariable String id) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return;
        }
        organizationService.undelete(id);
    }

    @GetMapping("/enable/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ_UPDATE)
    public void enable(@PathVariable String id) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return;
        }
        organizationService.enable(id);
    }

    @GetMapping("/disable/{id}")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ_UPDATE)
    public void disable(@PathVariable String id) {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return;
        }
        organizationService.disable(id);
    }

    @GetMapping("/getDefault")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_READ)
    public OrganizationDTO getDefault() {
        OrganizationService organizationService = CommonBeanFactory.getBean(OrganizationService.class);
        if (organizationService == null) {
            return new OrganizationDTO();
        }
        return organizationService.getDefault();
    }
}
