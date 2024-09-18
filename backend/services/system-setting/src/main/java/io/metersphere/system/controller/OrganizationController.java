package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.EmailInviteSource;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.OptionDisabledDTO;
import io.metersphere.system.dto.OrgUserExtend;
import io.metersphere.system.dto.request.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.response.UserInviteResponse;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.OrganizationService;
import io.metersphere.system.service.SimpleUserService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author guoyuqi
 */
@Tag(name = "系统设置-组织-成员")
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;
    @Resource
    private SimpleUserService simpleUserService;

    @PostMapping("/member/list")
    @Operation(summary = "系统设置-组织-成员-获取组织成员列表")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_READ)
    public Pager<List<OrgUserExtend>> getMemberList(@Validated @RequestBody OrganizationRequest organizationRequest) {
        Page<Object> page = PageHelper.startPage(organizationRequest.getCurrent(), organizationRequest.getPageSize());
        return PageUtils.setPageInfo(page, organizationService.getMemberListByOrg(organizationRequest));
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public void addMemberByList(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberByOrg(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/user/invite")
    @Operation(summary = "系统设置-组织-成员-邀请用户注册")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_INVITE)
    public UserInviteResponse invite(@Validated @RequestBody UserInviteRequest request) {
        return simpleUserService.saveInviteRecord(request, EmailInviteSource.ORGANIZATION.name(), SessionUtils.getUser());
    }

    @PostMapping("/role/update-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员至用户组")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberRole(@Validated @RequestBody OrganizationMemberExtendRequest organizationMemberExtendRequest) {
        organizationService.addMemberRole(organizationMemberExtendRequest, SessionUtils.getUserId());
    }

    @PostMapping("/update-member")
    @Operation(summary = "系统设置-组织-成员-更新用户")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_MEMBER_UPDATE, PermissionConstants.PROJECT_USER_READ_ADD, PermissionConstants.PROJECT_USER_READ_DELETE}, logical = Logical.OR)
    public void updateMember(@Validated @RequestBody OrganizationMemberUpdateRequest organizationMemberExtendRequest) {
        organizationService.updateMember(organizationMemberExtendRequest, SessionUtils.getUserId(), "/organization/update-member", OperationLogModule.SETTING_ORGANIZATION_MEMBER);
    }

    @PostMapping("/project/add-member")
    @Operation(summary = "系统设置-组织-成员-添加组织成员至项目")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_UPDATE)
    public void addMemberToProject(@Validated @RequestBody OrgMemberExtendProjectRequest orgMemberExtendProjectRequest) {
        organizationService.addMemberToProject(orgMemberExtendProjectRequest, SessionUtils.getUserId());
    }

    @GetMapping("/remove-member/{organizationId}/{userId}")
    @Operation(summary = "系统设置-组织-成员-删除组织成员")
    @Parameters({
            @Parameter(name = "organizationId", description = "组织ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.batchDelLog(#organizationId, #userId)", msClass = OrganizationService.class)
    public void removeMember(@PathVariable String organizationId, @PathVariable String userId) {
        organizationService.removeMember(organizationId, userId, SessionUtils.getUserId());
    }

    @GetMapping("/project/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有项目")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_READ)
    public List<OptionDTO> getProjectList(@PathVariable(value = "organizationId") String organizationId, @Schema(description = "查询关键字，根据项目名查询", requiredMode = Schema.RequiredMode.REQUIRED) @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationService.getProjectList(organizationId, keyword);
    }

    @GetMapping("/user/role/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有自定义用户组以及组织级别的用户组")
    //@RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_READ)
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_MEMBER_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ}, logical = Logical.OR)
    public List<OptionDTO> getUserRoleList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getUserRoleList(organizationId);
    }

    @GetMapping("/not-exist/user/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取不在当前组织的所有用户")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_MEMBER_ADD)
    public List<OptionDisabledDTO> getUserList(@PathVariable(value = "organizationId") String organizationId, @Schema(description = "查询关键字，根据用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
    @RequestParam(value = "keyword", required = false) String keyword) {
        return organizationService.getUserList(organizationId, keyword);
    }

}
