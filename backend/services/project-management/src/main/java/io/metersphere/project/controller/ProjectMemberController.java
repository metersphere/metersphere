package io.metersphere.project.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.request.ProjectMemberAddRequest;
import io.metersphere.project.request.ProjectMemberBatchDeleteRequest;
import io.metersphere.project.request.ProjectMemberEditRequest;
import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.project.service.ProjectMemberService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.dto.UserExtend;
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
@Tag(name = "项目管理-成员")
@RestController
@RequestMapping("/project/member")
public class ProjectMemberController {

    @Resource
    private ProjectMemberService projectMemberService;

    @PostMapping("/list")
    @Operation(summary = "项目管理-成员-列表查询")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_READ)
    public Pager<List<ProjectUserDTO>> listMember(@Validated @RequestBody ProjectMemberRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, projectMemberService.listMember(request));
    }

    @GetMapping("/get-member/option/{projectId}")
    @Operation(summary = "项目管理-成员-获取成员下拉选项")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_ADD)
    public List<UserExtend> getMemberOption(@PathVariable String projectId) {
        return projectMemberService.getMemberOption(projectId);
    }

    @GetMapping("/get-role/option/{projectId}")
    @Operation(summary = "项目管理-成员-获取用户组下拉选项")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_ADD)
    public List<OptionDTO> getRoleOption(@PathVariable String projectId) {
        return projectMemberService.getRoleOption(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-成员-添加成员")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_ADD)
    public void addMember(@RequestBody ProjectMemberAddRequest request) {
        projectMemberService.addMember(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-成员-编辑成员")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_UPDATE)
    public void updateMember(@RequestBody ProjectMemberEditRequest request) {
        projectMemberService.updateMember(request, SessionUtils.getUserId());
    }

    @GetMapping("/remove/{projectId}/{userId}")
    @Operation(summary = "项目管理-成员-移除成员")
    @Parameters({
            @Parameter(name = "projectId", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)),
            @Parameter(name = "userId", description = "成员ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    })
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_DELETE)
    public void removeMember(@PathVariable String projectId, @PathVariable String userId) {
        projectMemberService.removeMember(projectId, userId);
    }

    @PostMapping("/add-role")
    @Operation(summary = "项目管理-成员-批量添加至用户组")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_UPDATE)
    public void addMemberRole(@RequestBody ProjectMemberAddRequest request) {
        projectMemberService.addMemberRole(request, SessionUtils.getUserId());
    }

    @PostMapping("/batch/remove")
    @Operation(summary = "项目管理-成员-批量从项目移除")
    @RequiresPermissions(PermissionConstants.PROJECT_MEMBER_DELETE)
    public void batchRemove(@RequestBody ProjectMemberBatchDeleteRequest request) {
        projectMemberService.batchRemove(request);
    }
}
