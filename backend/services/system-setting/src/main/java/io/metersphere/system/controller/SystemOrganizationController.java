package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.SimpleUserService;
import io.metersphere.system.service.SystemOrganizationLogService;
import io.metersphere.system.service.SystemProjectService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author song-cc-rock
 */
@Tag(name = "系统设置-系统-组织与项目-组织")
@RestController
@RequestMapping("/system/organization")
public class SystemOrganizationController {

    @Resource
    private SimpleUserService simpleUserService;
    @Resource
    private SystemProjectService systemProjectService;
    @Resource
    private OrganizationService organizationService;

    @PostMapping("/list")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Pager<List<OrganizationDTO>> list(@Validated @RequestBody OrganizationRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        return PageUtils.setPageInfo(page, organizationService.list(request, SessionUtils.getUserId()));
    }

    @PostMapping("/update")
    @Operation(summary = "系统设置-系统-组织与项目-组织-修改组织")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#organizationEditRequest)", msClass = SystemOrganizationLogService.class)
    public void update(@Validated({Updated.class}) @RequestBody OrganizationEditRequest organizationEditRequest) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyBean(organizationDTO, organizationEditRequest);
        organizationDTO.setUpdateUser(SessionUtils.getUserId());
        organizationService.update(organizationDTO);
    }

    @PostMapping("/rename")
    @Operation(summary = "系统设置-系统-组织与项目-组织-修改组织名称")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateNameLog(#organizationEditRequest)", msClass = SystemOrganizationLogService.class)
    public void rename(@Validated({Updated.class}) @RequestBody OrganizationNameEditRequest organizationEditRequest) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        BeanUtils.copyBean(organizationDTO, organizationEditRequest);
        organizationDTO.setUpdateUser(SessionUtils.getUserId());
        organizationService.updateName(organizationDTO);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-删除组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = SystemOrganizationLogService.class)
    public void delete(@PathVariable String id) {
        OrganizationDeleteRequest organizationDeleteRequest = new OrganizationDeleteRequest();
        organizationDeleteRequest.setOrganizationId(id);
        organizationDeleteRequest.setDeleteUserId(SessionUtils.getUserId());
        organizationService.delete(organizationDeleteRequest);
    }

    @GetMapping("/recover/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-恢复组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_RECOVER)
    @Log(type = OperationLogType.RECOVER, expression = "#msClass.recoverLog(#id)", msClass = SystemOrganizationLogService.class)
    public void recover(@PathVariable String id) {
        organizationService.recover(id);
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-启用组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        organizationService.enable(id);
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-结束组织")
    @Parameter(name = "id", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void disable(@PathVariable String id) {
        organizationService.disable(id);
    }

    @PostMapping("/option/all")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取系统所有组织下拉选项")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, PermissionConstants.ORGANIZATION_PROJECT_READ, PermissionConstants.PROJECT_BASE_INFO_READ}, logical = Logical.OR)
    public List<OptionDTO> listAll() {
        return organizationService.listAll();
    }

    @PostMapping("/list-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织成员列表")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ, PermissionConstants.SYSTEM_USER_READ}, logical = Logical.OR)
    public Pager<List<UserExtendDTO>> listMember(@Validated @RequestBody OrganizationRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, organizationService.getMemberListBySystem(request));
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-添加组织成员")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    public void addMember(@Validated @RequestBody OrganizationMemberRequest request) {
        organizationService.addMemberBySystem(request, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "系统设置-系统-组织与项目-组织-删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/default")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取系统默认组织")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public OrganizationDTO getDefault() {
        return organizationService.getDefault();
    }

    @PostMapping("/list-project")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织下的项目列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Pager<List<ProjectDTO>> listProject(@Validated @RequestBody OrganizationProjectRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "create_time desc");
        ProjectRequest projectRequest = new ProjectRequest();
        BeanUtils.copyBean(projectRequest, request);
        return PageUtils.setPageInfo(page, systemProjectService.getProjectList(projectRequest));
    }

    @GetMapping("/total")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织和项目总数")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Map<String, Long> getTotal(@RequestParam(value = "organizationId", required = false) String organizationId) {
        return organizationService.getTotal(organizationId);
    }

    @GetMapping("/get-option/{sourceId}")
    @Operation(summary = "系统设置-系统-组织与项目-获取成员下拉选项")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ})
    @Parameter(name = "sourceId", description = "组织ID或项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<UserExtendDTO> getMemberOption(@PathVariable String sourceId,
                                               @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return simpleUserService.getMemberOption(sourceId, keyword);
    }


    @PostMapping("/member-list")
    @Operation(summary = "系统设置-系统-组织与项目-获取添加成员列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Pager<List<UserExtendDTO>> getMemberOptionList(@Validated @RequestBody MemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, simpleUserService.getMemberList(request));
    }

    @PostMapping("/update-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-成员-更新成员用户组")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_UPDATE)
    public void updateMember(@Validated @RequestBody OrganizationMemberUpdateRequest organizationMemberExtendRequest) {
        organizationService.updateMember(organizationMemberExtendRequest, SessionUtils.getUserId(), "/system/organization/update-member", OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }
}
