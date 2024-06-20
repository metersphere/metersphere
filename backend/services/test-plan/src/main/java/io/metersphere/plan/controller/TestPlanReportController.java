package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugAttachmentService;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.ReportDetailCasePageDTO;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanReportDetailResponse;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.service.*;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/test-plan/report")
@Tag(name = "测试计划-报告")
public class TestPlanReportController {

    @Resource
    private BugAttachmentService bugAttachmentService;
    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestPlanService testPlanService;

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

    @PostMapping("/rename/{id}")
    @Operation(summary = "测试计划-报告-重命名")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_UPDATE)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan_report")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.renameLog(#id, #name)", msClass = TestPlanReportLogService.class)
    public void rename(@PathVariable String id, @RequestBody Object name) {
        testPlanReportService.rename(id, name.toString());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "测试计划-报告-删除")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "test_plan_report")
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = TestPlanReportLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TEST_PLAN_REPORT_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getDto(#id)", targetClass = TestPlanReportNoticeService.class)
    public void delete(@PathVariable String id) {
        testPlanReportService.setReportDelete(id);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "测试计划-报告-批量删除")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_DELETE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchDelete(@Validated @RequestBody TestPlanReportBatchRequest request) {
        testPlanReportService.batchSetReportDelete(request, SessionUtils.getUserId());
    }

    @PostMapping("/gen")
    @Operation(summary = "测试计划-详情-生成报告")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public void genReportByManual(@Validated @RequestBody TestPlanReportGenRequest request) {
        testPlanService.checkTestPlanNotArchived(request.getTestPlanId());
        testPlanReportService.genReportByManual(request, SessionUtils.getUserId());
    }

    // 报告详情开始

    @GetMapping("/get/{reportId}")
    @Operation(summary = "测试计划-报告-详情")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#reportId", resourceType = "test_plan_report")
    public TestPlanReportDetailResponse get(@PathVariable String reportId) {
        return testPlanReportService.getReport(reportId);
    }

    @PostMapping("/upload/md/file")
    @Operation(summary = "测试计划-报告-详情-上传富文本(图片)")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_UPDATE)
    public String upload(@RequestParam("file") MultipartFile file) {
        return bugAttachmentService.uploadMdFile(file);
    }

    @PostMapping("/detail/edit")
    @Operation(summary = "测试计划-报告-详情-报告内容更新")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "test_plan_report")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateDetailLog(#request)", msClass = TestPlanReportLogService.class)
    public TestPlanReportDetailResponse edit(@Validated @RequestBody TestPlanReportDetailEditRequest request) {
        return testPlanReportService.edit(request, SessionUtils.getUserId());
    }

    @PostMapping("/detail/bug/page")
    @Operation(summary = "测试计划-报告-详情-缺陷分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<BugDTO>> pageBug(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tprb.bug_num, tprb.id desc");
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailBugs(request));
    }

    @PostMapping("/detail/functional/case/page")
    @Operation(summary = "测试计划-报告-详情-功能用例分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<ReportDetailCasePageDTO>> pageFunctionalCase(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tprfc.function_case_num, tprfc.id desc");
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailCases(request, AssociateCaseType.FUNCTIONAL));
    }

    @PostMapping("/detail/api/case/page")
    @Operation(summary = "测试计划-报告-详情-接口用例分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<ReportDetailCasePageDTO>> pageApiCase(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tprac.api_case_num, tprac.id desc");
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailCases(request, AssociateCaseType.API_CASE));
    }

    @PostMapping("/detail/scenario/case/page")
    @Operation(summary = "测试计划-报告-详情-场景用例分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<ReportDetailCasePageDTO>> pageScenarioCase(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tpras.api_scenario_num, tpras.id desc");
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailCases(request, AssociateCaseType.API_SCENARIO));
    }

    @PostMapping("/detail/plan/report/page")
    @Operation(summary = "测试计划-报告-集合报告详情")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<TestPlanReportDetailResponse>> planReportPage(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tpr.create_time desc");
        return PageUtils.setPageInfo(page, testPlanReportService.planReportList(request));
    }
}
