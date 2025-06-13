package io.metersphere.dashboard.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.definition.ApiDefinitionUpdateDTO;
import io.metersphere.bug.dto.response.BugColumnsOptionDTO;
import io.metersphere.bug.service.BugCommonService;
import io.metersphere.bug.service.BugService;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.CascadeChildrenDTO;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.dashboard.service.DashboardService;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.system.service.PermissionCheckService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.metersphere.dashboard.result.DashboardResultCode.NO_PROJECT_PERMISSION;

@Tag(name = "工作台-首页")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;
    @Resource
    private BugService bugService;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private PermissionCheckService permissionCheckService;

    @PostMapping("/layout/edit/{organizationId}")
    @Operation(summary = "编辑用户布局")
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<LayoutDTO> editLayout(@PathVariable String organizationId, @RequestBody List<LayoutDTO> layoutDTO ) {
       return dashboardService.editLayout(organizationId, SessionUtils.getUserId(), layoutDTO);
    }

    @GetMapping("/layout/get/{organizationId}")
    @Operation(summary = "获取用户布局")
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<LayoutDTO> getLayout(@PathVariable String organizationId) {
        return dashboardService.getLayout(organizationId, SessionUtils.getUserId());
    }

    @PostMapping("/create_by_me")
    @Operation(summary = "我创建的")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public OverViewCountDTO createByMeCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.createByMeCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/project_view")
    @Operation(summary = "概览")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public OverViewCountDTO projectViewCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectViewCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/project_member_view")
    @Operation(summary = "人员概览")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public OverViewCountDTO projectMemberViewCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectMemberViewCount(request);
    }

    @PostMapping("/plan_view")
    @Operation(summary = "测试计划概览")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public OverViewCountDTO projectPlanViewCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectPlanViewCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/bug_handle_user")
    @Operation(summary = "缺陷处理人统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public OverViewCountDTO projectBugHandleUser(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectBugHandleUser(request, SessionUtils.getUserId());
    }

    @GetMapping("/bug_handle_user/list/{projectId}")
    @Operation(summary = "获取缺陷处理人列表")
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<SelectOption> getBugHandleUserList(@PathVariable String projectId) {
        return dashboardService.getBugHandleUserList(projectId);
    }

    @GetMapping("/member/get-project-member/option/{projectId}")
    @Operation(summary = "获取项目成员列表")
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<UserExtendDTO> getMemberOption(@PathVariable String projectId,
                                               @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                               @RequestParam(value = "keyword", required = false) String keyword) {
        return dashboardService.getMemberOption(projectId, keyword);
    }

    @PostMapping("/case_count")
    @Operation(summary = "用例数")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectCaseCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/associate_case_count")
    @Operation(summary = "关联用例统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectAssociateCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectAssociateCaseCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/review_case_count")
    @Operation(summary = "用例评审数")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectReviewCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectReviewCaseCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/api_count")
    @Operation(summary = "接口数量统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectApiCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectApiCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/api_case_count")
    @Operation(summary = "接口用例数量统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectApiCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectApiCaseCount(request, SessionUtils.getUserId());
    }

    @PostMapping("/scenario_count")
    @Operation(summary = "场景数量统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectApiScenarioCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectApiScenarioCount(request, SessionUtils.getUserId());
    }


    @PostMapping("/bug_count")
    @Operation(summary = "缺陷数量统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectBugCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectBugCount(request, SessionUtils.getUserId(), null);
    }

    @PostMapping("/create_bug_by_me")
    @Operation(summary = "我创建的缺陷")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectBugCountCreateByMe(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectBugCountCreateByMe(request, SessionUtils.getUserId(), null);
    }

    @PostMapping("/handle_bug_by_me")
    @Operation(summary = "待我处理的缺陷")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectBugCountHandleByMe(@Validated @RequestBody DashboardFrontPageRequest request) {
        String platformHandlerUser = bugCommonService.getPlatformHandlerUser(request.getProjectIds().getFirst(), SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId());
        return dashboardService.projectBugCountHandleByMe(request, SessionUtils.getUserId(), platformHandlerUser);
    }

    @PostMapping("/plan_legacy_bug")
    @Operation(summary = "计划遗留bug统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectPlanLegacyBug(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectPlanLegacyBug(request, SessionUtils.getUserId());
    }


    @PostMapping("/reviewing_by_me")
    @Operation(summary = "待我评审")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Pager<List<CaseReviewDTO>> getFunctionalCasePage(@Validated @RequestBody DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, "caseManagement", SessionUtils.getUserId(), PermissionConstants.FUNCTIONAL_CASE_READ))) {
            throw new MSException(NO_PROJECT_PERMISSION);
        }
        return dashboardService.getFunctionalCasePage(request);
    }

    @PostMapping("/api_change")
    @Operation(summary = "接口变更")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Pager<List<ApiDefinitionUpdateDTO>> getApiUpdatePage(@Validated @RequestBody DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, "apiTest", SessionUtils.getUserId(), PermissionConstants.PROJECT_API_DEFINITION_READ))) {
            throw new MSException(NO_PROJECT_PERMISSION);
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, dashboardService.getApiUpdatePage(request));
    }

    @GetMapping("/header/custom-field/{projectId}")
    @Operation(summary = "缺陷列表-获取表头自定义字段集合")
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<TemplateCustomFieldDTO> getHeaderFields(@PathVariable String projectId) {
        return bugService.getHeaderCustomFields(projectId);
    }

    @GetMapping("/header/columns-option/{projectId}")
    @Operation(summary = "缺陷列表-获取表头状态选项")
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public BugColumnsOptionDTO getHeaderOption(@PathVariable String projectId) {
        return bugService.getHeaderOption(projectId);
    }

    @GetMapping("/plan/option/{projectId}")
    @Operation(summary = "获取测试计划列表")
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<CascadeChildrenDTO> getPlanOption(@PathVariable String projectId) {
        return dashboardService.getPlanOption(projectId);
    }

}
