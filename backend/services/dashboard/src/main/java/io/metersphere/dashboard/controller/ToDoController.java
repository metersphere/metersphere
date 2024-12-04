package io.metersphere.dashboard.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugService;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.plan.service.TestPlanStatisticsService;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author song-cc-rock
 */

@Tag(name = "工作台-我的待办")
@RestController
@RequestMapping("/dashboard/todo")
public class ToDoController {

	@Resource
	private BugService bugService;
	@Resource
	private CaseReviewService caseReviewService;
	@Resource
	private TestPlanManagementService testPlanManagementService;
	@Resource
	private TestPlanStatisticsService testPlanStatisticsService;

	@PostMapping("/plan/page")
	@Operation(summary = "我的待办-测试计划-列表分页查询")
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<TestPlanResponse>> planPage(@Validated @RequestBody TestPlanTableRequest request) {
		// 默认按照创建时间倒序
		if (MapUtils.isEmpty(request.getSort())) {
			request.setSort(Map.of("createTime", "desc"));
		}
		request.setMyTodo(true);
		request.setMyTodoUserId(SessionUtils.getUserId());
		return testPlanManagementService.page(request);
	}

	@PostMapping("/plan/statistics")
	@Operation(summary = "我的待办-测试计划-获取列表详情统计 {通过率, 执行进度}")
	@Parameter(name = "ids", description = "计划ID集合", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
	public List<TestPlanStatisticsResponse> selectTestPlanMetricById(@RequestBody List<String> ids) {
		return testPlanStatisticsService.calculateRate(ids);
	}

	@PostMapping("/review/page")
	@Operation(summary = "我的待办-用例评审-列表分页查询")
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<CaseReviewDTO>> reviewPage(@Validated @RequestBody CaseReviewPageRequest request) {
		request.setMyTodo(true);
		request.setMyTodoUserId(SessionUtils.getUserId());
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
		return PageUtils.setPageInfo(page, caseReviewService.getCaseReviewPage(request));
	}

	@PostMapping("/bug/page")
	@Operation(summary = "我的待办-缺陷-列表分页查询")
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<BugDTO>> bugPage(@Validated @RequestBody BugPageRequest request) {
		request.setUseTrash(false);
		request.setAssignedToMe(true);
		request.setUnresolved(true);
		request.setTodoParam(bugService.buildBugToDoParam(request, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId()));
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "b.pos desc");
		return PageUtils.setPageInfo(page, bugService.list(request));
	}
}
