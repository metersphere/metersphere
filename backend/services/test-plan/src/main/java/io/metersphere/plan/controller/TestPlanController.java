package io.metersphere.plan.controller;

import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.request.TestPlanUpdateRequest;
import io.metersphere.plan.dto.response.TestPlanCountResponse;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test-plan")
@Tag(name = "测试计划")
public class TestPlanController {
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestPlanManagementService testPlanManagementService;


    @PostMapping("/page")
    @Operation(summary = "测试计划-表格分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TestPlanResponse>> page(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.page(request);
    }

    @GetMapping("/getCount/{id}")
    @Operation(summary = "测试计划-获取统计数据")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan")
    public TestPlanCountResponse getCount(@PathVariable String id) {
        testPlanManagementService.checkModuleIsOpen(id, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.getCount(id);
    }


    @PostMapping("/module/count")
    @Operation(summary = "测试计划-模块统计")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.moduleCount(request);
    }


    @PostMapping("/add")
    @Operation(summary = "测试计划-创建测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String add(@Validated @RequestBody TestPlanCreateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.add(request, SessionUtils.getUserId(), "/test-plan/add", HttpMethodConstants.POST.name());
    }

    @PostMapping("/update")
    @Operation(summary = "测试计划-创建测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "test_plan")
    public String add(@Validated @RequestBody TestPlanUpdateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.update(request, SessionUtils.getUserId(), "/test-plan/update", HttpMethodConstants.POST.name());
    }


    @GetMapping("/delete/{id}")
    @Operation(summary = "测试计划-删除测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan")
    public void delete(@NotBlank @PathVariable String id) {
        testPlanManagementService.checkModuleIsOpen(id, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.delete(id, SessionUtils.getUserId(), "/test-plan/delete", HttpMethodConstants.GET.name());
    }


    @PostMapping(value = "/batch-delete")
    @Operation(summary = "测试计划-批量删除")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void delete(@Validated @RequestBody TestPlanBatchProcessRequest request) throws Exception {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.batchDelete(request, SessionUtils.getUserId(), "/test-plan/batch-delete", HttpMethodConstants.POST.name());
    }


    //todo 关注测试计划接口

    //todo 取消关注测试计划接口

    /*
    todo 归档测试计划
        ·归档时要确定测试计划是完成状态
        ·归档时所有数据要备份一套新的（包括各种用例的基本信息）。
            ·关于环境：但是在查看归档内容的时候所属环境展示什么？只展示名字吗？
            ·关于用例：那么是要在测试计划里能查看到用例的具体信息吗？ 不归档的测试计划能查看吗？
                ·功能用例里涉及到评审相关的也要备份吗？
                ·功能用例里关联的其他自动化用例信息也要备份吗？
                ·缺陷的整个流转过程也是要备份吗？
            ·关于报告：
                ·备份到什么程度？还需要在报告列表中展示这些报告吗？
                ·每个用例调试出来的报告要备份吗？全部都备份？
                ·执行记录呢也要完全备份吗？
        ·归档的测试计划从哪里看？在测试计划列表中混合着展示吗？还是提供一个单独的按钮/页面/模块单独展示这些归档了的东西？
     */
}
