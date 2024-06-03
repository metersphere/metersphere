package io.metersphere.plan.controller;

import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanDetailResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.service.*;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Resource
    private TestPlanStatisticsService testPlanStatisticsService;


    @PostMapping("/page")
    @Operation(summary = "测试计划-表格分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<TestPlanResponse>> page(@Validated @RequestBody TestPlanTableRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.page(request);
    }

    @GetMapping("/list-in-group/{groupId}")
    @Operation(summary = "测试计划-表格分页查询")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#groupId", resourceType = "test_plan")
    public List<TestPlanResponse> listInGroup(@NotBlank @PathVariable String groupId) {
        testPlanManagementService.checkModuleIsOpen(groupId, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanManagementService.selectByGroupId(groupId);
    }

    @PostMapping("/statistics")
    @Operation(summary = "测试计划-获取计划详情统计{通过率, 执行进度}")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @Parameter(name = "ids", description = "计划ID集合", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    public List<TestPlanStatisticsResponse> selectTestPlanMetricById(@RequestBody List<String> ids) {
        return testPlanStatisticsService.calculateRate(ids);
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
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.sendAddNotice(#request)", targetClass = TestPlanSendNoticeService.class)
    public TestPlan add(@Validated @RequestBody TestPlanCreateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.add(request, SessionUtils.getUserId(), "/test-plan/add", HttpMethodConstants.POST.name());
    }

    @PostMapping("/update")
    @Operation(summary = "测试计划-更新测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "test_plan")
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.sendUpdateNotice(#request)", targetClass = TestPlanSendNoticeService.class)
    public TestPlan add(@Validated @RequestBody TestPlanUpdateRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.update(request, SessionUtils.getUserId(), "/test-plan/update", HttpMethodConstants.POST.name());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "测试计划-删除测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan")
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.sendDeleteNotice(#id)", targetClass = TestPlanSendNoticeService.class)
    public void delete(@NotBlank @PathVariable String id) {
        testPlanManagementService.checkModuleIsOpen(id, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.delete(id, SessionUtils.getUserId(), "/test-plan/delete", HttpMethodConstants.GET.name());
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "测试计划-关注/取消关注")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void editFollower(@Validated @RequestBody TestPlanFollowerRequest request) {
        String userId = SessionUtils.getUserId();
        testPlanService.editFollower(request.getTestPlanId(), userId);
    }

    @GetMapping("/archived/{id}")
    @Operation(summary = "测试计划-归档")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan")
    @Log(type = OperationLogType.ARCHIVED, expression = "#msClass.archivedLog(#id)", msClass = TestPlanLogService.class)
    public void archived(@NotBlank @PathVariable String id) {
        String userId = SessionUtils.getUserId();
        testPlanService.archived(id, userId);
    }

    @PostMapping("/copy")
    @Operation(summary = "测试计划-复制测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.COPY, expression = "#msClass.copyLog(#request)", msClass = TestPlanLogService.class)
    public TestPlan copy(@Validated @RequestBody TestPlanCopyRequest request) {
        return testPlanService.copy(request, SessionUtils.getUserId());
    }

    @GetMapping("/{id}")
    @Operation(summary = "测试计划-抽屉详情(单个测试计划获取详情用于编辑)")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan")
    public TestPlanDetailResponse detail(@NotBlank @PathVariable String id) {
        return testPlanService.detail(id, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-delete")
    @Operation(summary = "测试计划-批量删除")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void delete(@Validated @RequestBody TestPlanBatchProcessRequest request) throws Exception {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.batchDelete(request, SessionUtils.getUserId(), "/test-plan/batch-delete", HttpMethodConstants.POST.name());
    }

    @PostMapping("/batch-copy")
    @Operation(summary = "测试计划-批量复制测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchCopy(@Validated @RequestBody TestPlanBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        testPlanService.batchCopy(request, SessionUtils.getUserId(), "/test-plan/batch-copy", HttpMethodConstants.POST.name());
    }

    @PostMapping("/batch-move")
    @Operation(summary = "测试计划-批量移动测试计划")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchMove(@Validated @RequestBody TestPlanBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        testPlanService.batchMove(request, SessionUtils.getUserId(), "/test-plan/batch-move", HttpMethodConstants.POST.name());
    }

    @PostMapping("/batch-archived")
    @Operation(summary = "测试计划-批量归档")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchArchived(@Validated @RequestBody TestPlanBatchRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.batchArchived(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/association")
    @Operation(summary = "测试计划功能用例-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void association(@Validated @RequestBody TestPlanAssociationRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        testPlanService.checkTestPlanNotArchived(request.getTestPlanId());
        testPlanService.association(request, SessionUtils.getUserId());
        testPlanService.refreshTestPlanStatus(request.getTestPlanId());
    }

    @PostMapping("/batch-edit")
    @Operation(summary = "测试计划-批量编辑")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.batchEditLog(#request)", msClass = TestPlanLogService.class)
    public void batchEdit(@Validated @RequestBody TestPlanBatchEditRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getProjectId(), TestPlanResourceConfig.CHECK_TYPE_PROJECT, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        testPlanService.filterArchivedIds(request);
        testPlanService.batchEdit(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划移动（测试计划拖进、拖出到测试计划组、测试计划在测试计划组内的排序")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getMoveId()", resourceType = "test_plan")
    public TestPlanResourceSortResponse sortTestPlan(@Validated @RequestBody PosRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getMoveId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN));
        return testPlanService.sortInGroup(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/move", HttpMethodConstants.POST.name()));
    }
}
