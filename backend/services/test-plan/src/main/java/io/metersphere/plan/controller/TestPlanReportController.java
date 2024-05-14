package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
import io.metersphere.plan.dto.request.TestPlanReportDeleteRequest;
import io.metersphere.plan.dto.request.TestPlanReportEditRequest;
import io.metersphere.plan.dto.request.TestPlanReportPageRequest;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/test-plan/report")
@Tag(name = "测试计划-报告")
public class TestPlanReportController {

	@Resource
	private TestPlanManagementService testPlanManagementService;
	@Resource
	private TestPlanReportService testPlanReportService;

	@PostMapping("/page")
	@Operation(summary = "测试计划-报告-表格分页查询")
	@RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public Pager<List<TestPlanReportPageResponse>> page(@Validated @RequestBody TestPlanReportPageRequest request) {
		testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tpr.create_time desc");
		return PageUtils.setPageInfo(page, testPlanReportService.page(request));
	}

	@PostMapping("/rename")
	@Operation(summary = "测试计划-报告-重命名")
	@RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_UPDATE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public void rename(@Validated @RequestBody TestPlanReportEditRequest request) {
		testPlanReportService.rename(request);
	}

	@PostMapping("/delete")
	@Operation(summary = "测试计划-报告-删除")
	@RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_DELETE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public void delete(@Validated @RequestBody TestPlanReportDeleteRequest request) {
		testPlanReportService.delete(request);
	}

	@PostMapping("/batch-delete")
	@Operation(summary = "测试计划-报告-批量删除")
	@RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_DELETE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public void batchDelete(@Validated @RequestBody TestPlanReportBatchRequest request) {
		testPlanReportService.batchDelete(request);
	}
}
