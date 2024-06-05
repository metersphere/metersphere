package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.dto.request.TestPlanApiCaseRequest;
import io.metersphere.plan.dto.response.TestPlanApiCasePageResponse;
import io.metersphere.plan.service.TestPlanApiCaseService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "测试计划接口用例")
@RestController
@RequestMapping("/test-plan/api/case")
public class TestPlanApiCaseController {

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;


    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联接口用例列表分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanApiCasePageResponse>> page(@Validated @RequestBody TestPlanApiCaseRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString("id")) ? request.getSortString("id") : "create_time desc");
        return PageUtils.setPageInfo(page, testPlanApiCaseService.HasRelateApiCaseList(request, false));
    }


    @PostMapping("/module/count")
    @Operation(summary = "测试计划-已关联接口用例模块数量")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanApiCaseRequest request) {
        return testPlanApiCaseService.moduleCount(request);
    }

    @GetMapping("/tree/{testPlanId}")
    @Operation(summary = "测试计划-已关联接口用例列表模块树")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#testPlanId", resourceType = "test_plan")
    public List<BaseTreeNode> getTree(@PathVariable String testPlanId) {
        return testPlanApiCaseService.getTree(testPlanId);
    }


}
