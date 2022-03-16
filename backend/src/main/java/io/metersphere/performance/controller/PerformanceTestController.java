package io.metersphere.performance.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.consul.CacheNode;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.performance.dto.LoadModuleDTO;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.request.*;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.FileService;
import io.metersphere.track.request.testplan.FileOperationRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "performance")
public class PerformanceTestController {
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private FileService fileService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @PostMapping("recent/{count}")
    public List<LoadTestDTO> recentTestPlans(@PathVariable int count, @RequestBody QueryTestPlanRequest request) {
        PageHelper.startPage(1, count, true);
        return performanceTestService.recentTestPlans(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_PERFORMANCE_TEST:READ")
    public Pager<List<LoadTestDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceTestService.list(request));
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions("PROJECT_PERFORMANCE_TEST:READ")
    public List<LoadTest> list(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return performanceTestService.getLoadTestByProjectId(projectId);
    }


    @GetMapping("/state/get/{testId}")
    @RequiresPermissions("PROJECT_PERFORMANCE_TEST:READ")
    public LoadTest listByTestId(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getLoadTestBytestId(testId);
    }

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_CREATE)
    @CacheNode // 把监控节点缓存起来
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, event = NoticeConstants.Event.CREATE,
            mailTemplate = "performance/TestCreate", subject = "性能测试通知")
    public LoadTest save(
            @RequestPart("request") SaveTestPlanRequest request,
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) {
        request.setId(UUID.randomUUID().toString());
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return performanceTestService.save(request, files);
    }

    @PostMapping(value = "/sync/scenario")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_CREATE)
    public void syncScenario(@RequestBody EditTestPlanRequest request) {
        performanceTestService.syncApi(request);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_EDIT)
    @CacheNode // 把监控节点缓存起来
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, event = NoticeConstants.Event.UPDATE, mailTemplate = "performance/TestUpdate", subject = "性能测试通知")
    public LoadTest edit(
            @RequestPart("request") EditTestPlanRequest request,
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) {
        checkPermissionService.checkPerformanceTestOwner(request.getId());
        return performanceTestService.edit(request, files);
    }


    @PostMapping("/edit/order")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        performanceTestService.updateOrder(request);
    }

    @GetMapping("/get/{testId}")
    public LoadTestDTO get(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.get(testId);
    }

    @GetMapping("/get-advanced-config/{testId}")
    public String getAdvancedConfiguration(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getAdvancedConfiguration(testId);
    }

    @GetMapping("/get-load-config/{testId}")
    public String getLoadConfiguration(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getLoadConfiguration(testId);
    }

    @GetMapping("/get-jmx-content/{testId}")
    public List<LoadTestExportJmx> getJmxContent(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getJmxContent(testId);
    }

    @PostMapping("/export/jmx")
    public List<LoadTestExportJmx> exportJmx(@RequestBody List<String> fileIds) {
        return performanceTestService.exportJmx(fileIds);
    }

    @PostMapping("/project/{loadType}/{projectId}/{goPage}/{pageSize}")
    public Pager<List<FileMetadata>> getProjectFiles(@PathVariable String projectId, @PathVariable String loadType,
                                                     @PathVariable int goPage, @PathVariable int pageSize,
                                                     @RequestBody QueryProjectFileRequest request) {
//        checkPermissionService.checkProjectOwner(projectId);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceTestService.getProjectFiles(projectId, loadType, request));
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_DELETE)
    @CacheNode // 把监控节点缓存起来
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, event = NoticeConstants.Event.DELETE,
            target = "#targetClass.get(#request.id)", targetClass = PerformanceTestService.class, mailTemplate = "performance/TestDelete", subject = "性能测试通知")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        checkPermissionService.checkPerformanceTestOwner(request.getId());
        performanceTestService.delete(request);
    }

    @PostMapping("/delete/batch")
    @CacheNode
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.DELETE, beforeEvent = "#msClass.deleteBatchLog(#request)", msClass = PerformanceTestService.class)
    public void deleteBatch(@RequestBody DeletePerformanceRequest request) {
        performanceTestService.deleteBatch(request);
    }

    @PostMapping("/run")
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_RUN)
    public String run(@RequestBody RunTestPlanRequest request) {
        return performanceTestService.run(request);
    }

    @GetMapping("stop/{reportId}/{forceStop}")
    public void stopTest(@PathVariable String reportId, @PathVariable boolean forceStop) {
        performanceTestService.stopTest(reportId, forceStop);
    }

    @GetMapping("/file/metadata/{testId}")
    public List<FileMetadata> getFileMetadata(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getFileMetadataByTestId(testId);
    }

    @GetMapping("/file/getMetadataById/{metadataId}")
    public FileMetadata getMetadataById(@PathVariable String metadataId) {
        return fileService.getFileMetadataById(metadataId);
    }

    @PostMapping("/file/{projectId}/getMetadataByName")
    public List<FileMetadata> getProjectMetadataByName(@PathVariable String projectId, @RequestBody QueryProjectFileRequest request) {
        return fileService.getProjectFiles(projectId, request);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> downloadJmx(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileService.loadFileAsBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getId()+".jmx" + "\"")
                .body(bytes);
    }

    @GetMapping("dashboard/tests")
    public List<DashboardTestDTO> dashboardTests() {
        return performanceTestService.dashboardTests(SessionUtils.getCurrentWorkspaceId());
    }

    @PostMapping(value = "/copy")
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.COPY, content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_COPY)
    @CacheNode // 把监控节点缓存起来
    public void copy(@RequestBody SaveTestPlanRequest request) {
        performanceTestService.copy(request);
    }

    @PostMapping(value = "/schedule/create")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_SCHEDULE)
    public void createSchedule(@RequestBody ScheduleRequest request) {
        performanceTestService.createSchedule(request);
    }

    @PostMapping(value = "/schedule/update")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_SCHEDULE)
    public void updateSchedule(@RequestBody Schedule request) {
        performanceTestService.updateSchedule(request);
    }

    @PostMapping("/list/schedule/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_PERFORMANCE_TEST:READ")
    public List<ScheduleDao> listSchedule(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryScheduleRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return performanceTestService.listSchedule(request);
    }

    @PostMapping("/list/schedule")
    public List<ScheduleDao> listSchedule(@RequestBody QueryScheduleRequest request) {
        return performanceTestService.listSchedule(request);
    }

    @GetMapping("test/report-count/{testId}")
    public Long getReportCount(@PathVariable String testId) {
        return performanceTestService.getReportCountByTestId(testId);
    }

    @GetMapping("test/follow/{testId}")
    public List<String> getFollows(@PathVariable String testId) {
        return performanceTestService.getFollows(testId);
    }

    @PostMapping("test/update/follows/{testId}")
    public void saveFollows(@PathVariable String testId, @RequestBody List<String> follows) {
        performanceTestService.saveFollows(testId, follows);
    }

    @GetMapping("module/list/plan/{planId}")
    public List<LoadModuleDTO> getNodeByPlanId(@PathVariable String planId) {
        return performanceTestService.getNodeByPlanId(planId);
    }

    @GetMapping("versions/{loadTestId}")
    public List<LoadTestDTO> getLoadTestVersions(@PathVariable String loadTestId) {
        return performanceTestService.getLoadTestVersions(loadTestId);
    }

    @GetMapping("get/{version}/{refId}")
    public LoadTestDTO getLoadTestByVersion(@PathVariable String version, @PathVariable String refId) {
        return performanceTestService.getLoadTestByVersion(version,refId);
    }

    @GetMapping("delete/{version}/{refId}")
    public void deleteLoadTestByVersion(@PathVariable String version, @PathVariable String refId) {
        performanceTestService.deleteLoadTestByVersion(version, refId);
    }
}
