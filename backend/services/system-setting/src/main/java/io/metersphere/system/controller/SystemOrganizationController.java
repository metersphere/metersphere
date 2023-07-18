package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.ProjectDTO;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;
import io.metersphere.system.request.ProjectRequest;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.SystemProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Tag(name = "系统-组织")
@RestController
@RequestMapping("/system/organization")
public class SystemOrganizationController {

    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private OrganizationService organizationService;

    @PostMapping("/list")
    @Operation(summary = "获取组织列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Pager<List<OrganizationDTO>> list(@Validated @RequestBody OrganizationRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, organizationService.list(request));
    }

    @PostMapping("/list-all")
    @Operation(summary = "获取系统所有组织")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<OrganizationDTO> listAll() {
        return organizationService.listAll();
    }

    @PostMapping("/list-member")
    @Operation(summary = "获取组织成员")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, PermissionConstants.SYSTEM_USER_READ})
    public Pager<List<UserExtend>> listMember(@Validated @RequestBody OrganizationRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, organizationService.getMemberListBySystem(request));
    }

    @PostMapping("/add-member")
    @Operation(summary = "添加组织成员")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void addMember(@Validated @RequestBody OrganizationMemberRequest request) {
        organizationService.addMemberBySystem(request, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId);
    }

    @GetMapping("/default")
    @Operation(summary = "获取系统默认组织")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public OrganizationDTO getDefault() {
        return organizationService.getDefault();
    }

    @PostMapping("/list-project")
    @Operation(summary = "获取组织下的项目列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Pager<List<ProjectDTO>> listProject(@Validated @RequestBody ProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(request));
    }
}
