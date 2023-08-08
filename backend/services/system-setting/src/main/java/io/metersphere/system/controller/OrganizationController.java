package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.IdNameStructureDTO;
import io.metersphere.system.dto.OrgUserExtend;
import io.metersphere.system.request.*;
import io.metersphere.system.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Tag(name = "组织")
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;

    @PostMapping("/member/list")
    @Operation(summary = "组织级别获取组织成员")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_MEMBER_READ, PermissionConstants.SYSTEM_USER_READ})
    public Pager<List<OrgUserExtend>> getMemberList(@Validated @RequestBody OrganizationRequest organizationRequest) {
        Page<Object> page = PageHelper.startPage(organizationRequest.getCurrent(), organizationRequest.getPageSize());
        return PageUtils.setPageInfo(page, organizationService.getMemberListByOrg(organizationRequest));
    }

    @PostMapping("/add-member")
    @Operation(summary = "添加组织成员")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public void addMemberByList(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberByOrg(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/role/update-member")
    @Operation(summary = "添加组织成员至用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberRole(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberRole(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/update-member")
    @Operation(summary = "更新用户")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_MEMBER_UPDATE, PermissionConstants.PROJECT_USER_READ_ADD,  PermissionConstants.PROJECT_USER_READ_DELETE})
    public void updateMember(@Validated @RequestBody OrganizationMemberUpdateRequest organizationMemberExtendRequest) {
        organizationService.updateMember(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/project/add-member")
    @Operation(summary = "添加组织成员至项目")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_MEMBER_UPDATE, PermissionConstants.PROJECT_USER_READ_ADD})
    public void addMemberToProject(@Validated @RequestBody OrgMemberExtendProjectRequest orgMemberExtendProjectRequest) {
        organizationService.addMemberToProject(orgMemberExtendProjectRequest, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDelLog(#organizationId, #userId)", msClass = OrganizationService.class)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId);
    }

    @GetMapping("/project/list/{organizationId}")
    @Operation(summary = "获取当前组织下的所有项目")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public List<IdNameStructureDTO> getProjectList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getProjectList(organizationId);
    }

    @GetMapping("/user/role/list/{organizationId}")
    @Operation(summary = "获取当前组织下的所有自定义用户组以及组织级别的用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public List<IdNameStructureDTO> getUserRoleList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getUserRoleList(organizationId);
    }

    @GetMapping("/not-exist/user/list/{organizationId}")
    @Operation(summary = "获取不在当前组织的所有用户")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public List<IdNameStructureDTO> getUserList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getUserList(organizationId);
    }

}
