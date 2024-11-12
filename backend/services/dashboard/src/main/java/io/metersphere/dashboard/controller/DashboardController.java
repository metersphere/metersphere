package io.metersphere.dashboard.controller;

import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.dashboard.service.DashboardService;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工作台-首页")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

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

}
