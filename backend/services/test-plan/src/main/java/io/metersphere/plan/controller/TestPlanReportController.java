package io.metersphere.plan.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.domain.TestPlanReportComponent;
import io.metersphere.plan.dto.ReportDetailCasePageDTO;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.*;
import io.metersphere.plan.service.*;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

/**
 * @author song-cc-rock
 */
@RestController
@RequestMapping("/test-plan/report")
@Tag(name = "测试计划-报告")
public class TestPlanReportController {
    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private CommonFileService commonFileService;

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

    @PostMapping("/batch-param")
    @Operation(summary = "测试计划-报告-获取批量参数")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public List<String> getBatchParam(@Validated @RequestBody TestPlanReportBatchRequest request) {
        return testPlanReportService.getBatchIds(request);
    }

    @PostMapping("/manual-gen")
    @Operation(summary = "测试计划-详情-手动生成报告")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public String genReportByManual(@Validated @RequestBody TestPlanReportManualRequest request) {
        testPlanService.checkTestPlanNotArchived(request.getTestPlanId());
        return testPlanReportService.genReportByManual(request, SessionUtils.getUserId());
    }

    @PostMapping("/auto-gen")
    @Operation(summary = "测试计划-详情-自动生成报告")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public String genReportByAuto(@Validated @RequestBody TestPlanReportGenRequest request) {
        testPlanService.checkTestPlanNotArchived(request.getTestPlanId());
        return testPlanReportService.genReportByAuto(request, SessionUtils.getUserId());
    }

    // 报告详情开始

    @GetMapping("/get/{reportId}")
    @Operation(summary = "测试计划-报告-详情")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#reportId", resourceType = "test_plan_report")
    public TestPlanReportDetailResponse get(@PathVariable String reportId) {
        return testPlanReportService.getReport(reportId);
    }

    @GetMapping("/get-task/{taskId}")
    @Operation(summary = "测试计划|组-执行历史-执行结果")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#taskId", resourceType = "exec_task")
    public TestPlanTaskReportResponse getTaskDetail(@PathVariable String taskId) {
        return testPlanReportService.getTaskDetail(taskId);
    }

    @GetMapping("/get-result/{taskId}")
    @Operation(summary = "测试计划|组-任务-执行结果")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#reportId", resourceType = "test_plan_report")
    public TestPlanReportDetailResponse getTaskResult(@PathVariable String taskId) {
        return testPlanReportService.getTaskResult(taskId);
    }

    @GetMapping("/get-layout/{reportId}")
    @Operation(summary = "测试计划-报告-组件布局")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#reportId", resourceType = "test_plan_report")
    public List<TestPlanReportComponent> getLayout(@PathVariable String reportId) {
        return testPlanReportService.getLayout(reportId);
    }

    @PostMapping("/upload/md/file")
    @Operation(summary = "测试计划-报告-详情-上传富文本(图片)")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_UPDATE)
    public String upload(@RequestParam("file") MultipartFile file) {
        return commonFileService.uploadTempImgFile(file);
    }

    @PostMapping("/detail/edit")
    @Operation(summary = "测试计划-报告-详情-富文本组件内容更新")
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
        request.setDetailReportIds(testPlanReportService.getActualReportIds(request.getReportId()));
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tprb.bug_num desc");
        if (!request.getStartPager()) {
            page.close();
            page.setOrderBy("tprb.bug_num desc");
        }
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailBugs(request));
    }

    @PostMapping("/detail/functional/case/page")
    @Operation(summary = "测试计划-报告-详情-功能用例分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<ReportDetailCasePageDTO>> pageFunctionalCase(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        request.setDetailReportIds(testPlanReportService.getActualReportIds(request.getReportId()));
        String sort = request.getSortString();
        sort = StringUtils.replace(sort, "request_time", "request_duration");
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(sort) ? sort : "tprfc.pos desc");
        if (!request.getStartPager()) {
            // 不分页仅排序 {测试集升序, 用例位次倒序}
            page.setPageSize(0);
            page.setPageSizeZero(true);
            page.setOrderBy("tpc.pos, tpc.name, tprfc.pos desc");
            page.setOrderByOnly(true);
        }
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailCases(request, null, AssociateCaseType.FUNCTIONAL));
    }

    @GetMapping("/detail/functional/case/step/{reportId}")
    @Operation(summary = "测试计划-报告-详情-功能用例-执行步骤结果")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    public TestPlanCaseExecHistoryResponse getFunctionalExecuteResult(@PathVariable String reportId) {
        return testPlanReportService.getFunctionalExecuteResult(reportId);
    }

    @PostMapping("/detail/api/case/page")
    @Operation(summary = "测试计划-报告-详情-接口用例分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<ReportDetailCasePageDTO>> pageApiCase(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        request.setDetailReportIds(testPlanReportService.getActualReportIds(request.getReportId()));
        String sort = request.getSortString();
        sort = StringUtils.replace(sort, "request_time", "request_duration");
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(sort) ? sort : "tprac.pos desc");
        if (!request.getStartPager()) {
            // 不分页仅排序 {测试集升序, 用例位次倒序}
            page.setPageSize(0);
            page.setPageSizeZero(true);
            page.setOrderBy("tpc.pos, tpc.name, tprac.pos desc");
            page.setOrderByOnly(true);
        }
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailCases(request, null, AssociateCaseType.API_CASE));
    }

    @PostMapping("/detail/scenario/case/page")
    @Operation(summary = "测试计划-报告-详情-场景用例分页查询")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<ReportDetailCasePageDTO>> pageScenarioCase(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        request.setDetailReportIds(testPlanReportService.getActualReportIds(request.getReportId()));
        String sort = request.getSortString();
        sort = StringUtils.replace(sort, "request_time", "request_duration");
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(sort) ? sort : "tpras.pos desc");
        if (!request.getStartPager()) {
            // 不分页仅排序 {测试集升序, 用例位次倒序}
            page.setPageSize(0);
            page.setPageSizeZero(true);
            page.setOrderBy("tpc.pos, tpc.name, tpras.pos desc");
            page.setOrderByOnly(true);
        }
        return PageUtils.setPageInfo(page, testPlanReportService.listReportDetailCases(request, null, AssociateCaseType.API_SCENARIO));
    }

    @PostMapping("/detail/plan/report/page")
    @Operation(summary = "测试计划-报告-集合报告详情")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    public Pager<List<TestPlanReportDetailResponse>> planReportPage(@Validated @RequestBody TestPlanReportDetailPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "tpr.create_time desc");
        if (!request.getStartPager()) {
            page.close();
        }
        return PageUtils.setPageInfo(page, testPlanReportService.planReportList(request));
    }

    @GetMapping(value = "/preview/md/{projectId}/{fileId}/{compressed}")
    @Operation(summary = "缺陷管理-富文本缩略图-预览")
    public ResponseEntity<byte[]> previewMd(@PathVariable String projectId, @PathVariable String fileId, @PathVariable("compressed") boolean compressed) {
        return testPlanReportService.previewMd(projectId, fileId, compressed);
    }

    @PostMapping("/export/{reportId}")
    @Operation(summary = "测试计划-报告-导出日志")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_EXPORT)
    public void exportLog(@PathVariable String reportId) {
        testPlanReportService.exportLog(reportId, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/batch-export")
    @Operation(summary = "测试计划-报告-批量导出日志")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_REPORT_READ_EXPORT)
    public void batchExportLog(@Validated @RequestBody TestPlanReportBatchRequest request) {
        testPlanReportService.batchExportLog(request, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

    @PostMapping("/detail/{type}/collection/page")
    @Operation(summary = "测试计划-报告-详情-测试集分页查询(不同用例类型)")
    @RequiresPermissions(value = {PermissionConstants.TEST_PLAN_REPORT_READ, PermissionConstants.TEST_PLAN_READ_EXECUTE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReportId()", resourceType = "test_plan_report")
    @Parameter(name = "type", description = "用例类型", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED), example = "functional, api, scenario")
    public Pager<List<TestPlanReportDetailCollectionResponse>> collectionPage(@PathVariable String type, @Validated @RequestBody TestPlanReportDetailPageRequest request) {
        request.setDetailReportIds(testPlanReportService.getActualReportIds(request.getReportId()));
        // 默认按照测试集的位序升序
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), "tpc.pos asc");
        return PageUtils.setPageInfo(page, testPlanReportService.listReportCollection(request, type));
    }
}
