package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanShareInfo;
import io.metersphere.plan.dto.request.TestPlanReportShareRequest;
import io.metersphere.plan.dto.response.TestPlanShareResponse;
import io.metersphere.plan.service.TestPlanReportShareService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-plan/report/share")
@Tag(name = "测试计划-分享")
public class TestPlanReportShareController {

	@Resource
	private TestPlanReportShareService testPlanReportShareService;

	@PostMapping("/gen")
	@Operation(summary = "测试计划-报告-分享")
	@RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_SHARE)
	@CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
	public TestPlanShareInfo genReportShareInfo(@RequestBody TestPlanReportShareRequest request) {
		return testPlanReportShareService.gen(request, SessionUtils.getUserId());
	}

	@GetMapping("/get/{id}")
	@Operation(summary = "接口测试-接口报告-获取分享链接")
	public TestPlanShareResponse get(@PathVariable String id) {
		return testPlanReportShareService.get(id);
	}

	@GetMapping("/get-share-time/{id}")
	@Operation(summary = "接口测试-接口报告-获取分享链接的有效时间")
	public String getShareTime(@PathVariable String id) {
		return testPlanReportShareService.getShareTime(id);
	}
}
