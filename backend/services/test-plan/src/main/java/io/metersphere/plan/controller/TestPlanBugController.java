package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.dto.request.TestPlanBugPageRequest;
import io.metersphere.plan.dto.response.TestPlanBugPageResponse;
import io.metersphere.plan.service.TestPlanBugService;
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

import java.util.List;

@Tag(name = "测试计划-详情-缺陷列表")
@RestController
@RequestMapping("/test-plan/bug")
public class TestPlanBugController {

	@Resource
	private TestPlanBugService testPlanBugService;

	@PostMapping("/page")
	@Operation(summary = "缺陷列表-分页查询")
	@RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
	@CheckOwner(resourceId = "#request.getPlanId()", resourceType = "test_plan")
	public Pager<List<TestPlanBugPageResponse>> page(@Validated @RequestBody TestPlanBugPageRequest request) {
		Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
				StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "b.create_time desc");
		return PageUtils.setPageInfo(page, testPlanBugService.page(request));
	}
}
