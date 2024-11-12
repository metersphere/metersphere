package io.metersphere.dashboard.controller;

import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.dashboard.service.DashboardService;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Tag(name = "工作台-首页")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;
    @Resource
    private CaseReviewService caseReviewService;

    @PostMapping("/layout/edit/{organizationId}")
    @Operation(summary = "编辑用户布局")
    @CheckOwner(resourceId = "#organizationId", resourceType = "organization")
    public List<LayoutDTO> editLayout(@PathVariable String organizationId, @Validated @RequestBody List<LayoutDTO> layoutDTO) {
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

    @PostMapping("/bug_handle_user")
    @Operation(summary = "缺陷处理人统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public OverViewCountDTO projectBugHandleUser(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectBugHandleUser(request);
    }

    @PostMapping("/case_count")
    @Operation(summary = "用例数")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectCaseCount(request);
    }

    @PostMapping("/associate_case_count")
    @Operation(summary = "关联用例统计")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectAssociateCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectAssociateCaseCount(request);
    }

    @PostMapping("/review_case_count")
    @Operation(summary = "用例评审数")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public StatisticsDTO projectReviewCaseCount(@Validated @RequestBody DashboardFrontPageRequest request) {
        return dashboardService.projectReviewCaseCount(request);
    }

    @PostMapping("/reviewing_by_me")
    @Operation(summary = "待我评审")
    @CheckOwner(resourceId = "#request.getOrganizationId()", resourceType = "organization")
    public Pager<List<CaseReviewDTO>> getFunctionalCasePage(@Validated @RequestBody DashboardFrontPageRequest request) {
        CaseReviewPageRequest reviewRequest = getCaseReviewPageRequest(request);
        Page<Object> page = PageHelper.startPage(reviewRequest.getCurrent(), reviewRequest.getPageSize(),
                StringUtils.isNotBlank(reviewRequest.getSortString()) ? reviewRequest.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, caseReviewService.getCaseReviewPage(reviewRequest));
    }

    @NotNull
    private static CaseReviewPageRequest getCaseReviewPageRequest(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        CaseReviewPageRequest reviewRequest = new CaseReviewPageRequest();
        reviewRequest.setProjectId(projectId);
        reviewRequest.setPageSize(request.getPageSize());
        reviewRequest.setCurrent(request.getCurrent());
        reviewRequest.setSort(request.getSort());
        CombineSearch combineSearch = getCombineSearch(request);
        reviewRequest.setCombineSearch(combineSearch);
        return reviewRequest;
    }

    @NotNull
    private static CombineSearch getCombineSearch(DashboardFrontPageRequest request) {
        CombineSearch combineSearch = new CombineSearch();
        combineSearch.setSearchMode(CombineSearch.SearchMode.AND.name());
        List<CombineCondition> conditions = new ArrayList<>();
        CombineCondition userCombineCondition = getCombineCondition(List.of(Objects.requireNonNull(SessionUtils.getUserId())), "reviewers",CombineCondition.CombineConditionOperator.IN.toString());
        conditions.add(userCombineCondition);
        CombineCondition statusCombineCondition = getCombineCondition(List.of(CaseReviewStatus.PREPARED.toString(), CaseReviewStatus.UNDERWAY.toString()), "status",CombineCondition.CombineConditionOperator.IN.toString());
        conditions.add(statusCombineCondition);
        CombineCondition createTimeCombineCondition = getCombineCondition(List.of(request.getToStartTime(), request.getToEndTime()), "createTime",CombineCondition.CombineConditionOperator.BETWEEN.toString());
        conditions.add(createTimeCombineCondition);
        combineSearch.setConditions(conditions);
        return combineSearch;
    }

    @NotNull
    private static CombineCondition getCombineCondition(List<Object> value, String reviewers, String operator) {
        CombineCondition userCombineCondition = new CombineCondition();
        userCombineCondition.setValue(value);
        userCombineCondition.setName(reviewers);
        userCombineCondition.setOperator(operator);
        userCombineCondition.setCustomField(false);
        userCombineCondition.setCustomFieldType("");
        return userCombineCondition;
    }

}
