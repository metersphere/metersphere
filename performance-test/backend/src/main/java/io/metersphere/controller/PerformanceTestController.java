package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.service.ApiPerformanceService;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.consul.CacheNode;
import io.metersphere.consul.ConsulService;
import io.metersphere.dto.*;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.request.*;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.PerformanceTestService;
import io.metersphere.task.dto.TaskRequestDTO;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "performance")
public class PerformanceTestController {
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private ConsulService consulService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private ApiPerformanceService apiPerformanceService;

    @PostMapping("recent/{count}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadTestDTO> recentTestPlans(@PathVariable int count, @RequestBody QueryTestPlanRequest request) {
        PageHelper.startPage(1, count, true);
        return performanceTestService.recentTestPlans(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public Pager<List<LoadTestDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceTestService.list(request));
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadTest> list(@PathVariable String projectId) {
        // checkPermissionService.checkProjectOwner(projectId);
        return performanceTestService.getLoadTestByProjectId(projectId);
    }

    @PostMapping("/list/batch")
    public List<LoadTestDTO> listBatch(@RequestBody LoadTestBatchRequest request) {
        return performanceTestService.listBatch(request);
    }

    @PostMapping("/file/list")
    public List<LoadTestFileDTO> selectLoadTestFileByRequest(@RequestBody LoadTestBatchRequest request) {
        return performanceTestService.selectLoadTestFileByRequest(request);
    }

    @GetMapping("/state/get/{testId}")
    public LoadTest listByTestId(@PathVariable String testId) {
        // // checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getLoadTestBytestId(testId);
    }

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_CREATE)
    @CacheNode // 把监控节点缓存起来
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, event = NoticeConstants.Event.CREATE,
            subject = "性能测试通知")
    public LoadTest save(
            @RequestPart("request") SaveTestPlanRequest request,
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) {
        request.setId(UUID.randomUUID().toString());
        LoadTest loadTest = performanceTestService.save(request, files);

        List<ApiLoadTest> apiList = request.getApiList();
        apiPerformanceService.add(apiList, loadTest.getId());
        //检查并发送审核脚本的通知
        performanceTestService.checkAndSendReviewMessage(new ArrayList<>(request.getUpdatedFileList()), files, loadTest);
        return loadTest;
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_EDIT)
    @CacheNode // 把监控节点缓存起来
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, event = NoticeConstants.Event.UPDATE, subject = "性能测试通知")
    public LoadTest edit(
            @RequestPart("request") EditTestPlanRequest request,
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) {
        LoadTest loadTest = performanceTestService.edit(request, files);
        //检查并发送审核脚本的通知
        performanceTestService.checkAndSendReviewMessage(new ArrayList<>(request.getUpdatedFileList()), files, loadTest);
        return loadTest;
    }


    @PostMapping("/edit/order")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_EDIT)
    @MsRequestLog(module = OperLogModule.PERFORMANCE_TEST)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        performanceTestService.updateOrder(request);
    }

    @GetMapping("/get/{testId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public LoadTestDTO get(@PathVariable String testId) {
        // // checkPermissionService.checkPerformanceTestOwner(testId);
        LoadTestDTO loadTestDTO = performanceTestService.get(testId);
        loadTestDTO.setIsNeedUpdate(apiPerformanceService.isNeedUpdate(loadTestDTO.getId()));
        return loadTestDTO;
    }

    @GetMapping("/get-advanced-config/{testId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public String getAdvancedConfiguration(@PathVariable String testId) {
        // // checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getAdvancedConfiguration(testId);
    }

    @GetMapping("/get-load-config/{testId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public String getLoadConfiguration(@PathVariable String testId) {
        // // checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getLoadConfiguration(testId);
    }

    @GetMapping("/get-jmx-content/{testId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadTestExportJmx> getJmxContent(@PathVariable String testId) {
        // // checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getJmxContent(testId);
    }

    @PostMapping("/export/jmx")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadTestExportJmx> exportJmx(@RequestBody List<String> fileIds) {
        return performanceTestService.exportJmx(fileIds);
    }

    @PostMapping("/project/{loadType}/{projectId}/{goPage}/{pageSize}")
    public Pager<List<FileMetadata>> getProjectFiles(@PathVariable String projectId, @PathVariable String loadType,
                                                     @PathVariable int goPage, @PathVariable int pageSize,
                                                     @RequestBody QueryProjectFileRequest request) {
        //        // checkPermissionService.checkProjectOwner(projectId);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, performanceTestService.getProjectFiles(projectId, loadType, request));
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_DELETE)
    @CacheNode // 把监控节点缓存起来
    @SendNotice(taskType = NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, event = NoticeConstants.Event.DELETE,
            target = "#targetClass.get(#request.id)", targetClass = PerformanceTestService.class, subject = "性能测试通知")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        // // checkPermissionService.checkPerformanceTestOwner(request.getId());
        performanceTestService.delete(request);
    }

    @PostMapping("/delete/batch")
    @CacheNode
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.DELETE, beforeEvent = "#msClass.deleteBatchLog(#request)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_DELETE)
    public void deleteBatch(@RequestBody DeletePerformanceRequest request) {
        performanceTestService.deleteBatch(request);
    }

    @PostMapping("/run")
    @MsAuditLog(module = OperLogModule.PERFORMANCE_TEST, type = OperLogConstants.EXECUTE, beforeEvent = "#msClass.getRunLogDetails(#request.id)", msClass = PerformanceTestService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_RUN)
    public String run(@RequestBody RunTestPlanRequest request) {
        return performanceTestService.run(request);
    }

    @GetMapping("stop/{reportId}/{forceStop}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_RUN)
    public void stopTest(@PathVariable String reportId, @PathVariable boolean forceStop) {
        performanceTestService.stopTest(reportId, forceStop);
    }

    @GetMapping("/file/metadata/{testId}")
    public List<FileMetadata> getFileMetadata(@PathVariable String testId) {
        // // checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getFileMetadataByTestId(testId);
    }

    @GetMapping("/file/getMetadataById/{metadataId}")
    public FileMetadata getMetadataById(@PathVariable String metadataId) {
        return fileMetadataService.getFileMetadataById(metadataId);
    }

    @PostMapping("/file/{projectId}/getMetadataByName")
    public List<FileMetadataWithBLOBs> getProjectMetadataByName(@PathVariable String projectId, @RequestBody QueryProjectFileRequest request) {
        return fileMetadataService.getProjectFiles(projectId, request);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> downloadJmx(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileMetadataService.loadFileAsBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getId() + ".jmx" + "\"")
                .body(bytes);
    }

    @GetMapping("dashboard/tests")
    public List<DashboardTestDTO> dashboardTests() {
        return performanceTestService.dashboardTests(SessionUtils.getCurrentProjectId());
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
    @MsRequestLog(module = OperLogModule.PERFORMANCE_TEST)
    public void createSchedule(@RequestBody ScheduleRequest request) {
        performanceTestService.createSchedule(request);
    }

    @PostMapping(value = "/schedule/update")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_SCHEDULE)
    public void updateSchedule(@RequestBody Schedule request) {
        performanceTestService.updateSchedule(request);
    }

    @PostMapping("/list/schedule/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_SCHEDULE)
    public List<ScheduleDao> listSchedule(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryScheduleRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return performanceTestService.listSchedule(request);
    }

    @PostMapping("/list/schedule")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_SCHEDULE)
    public List<ScheduleDao> listSchedule(@RequestBody QueryScheduleRequest request) {
        return performanceTestService.listSchedule(request);
    }

    @GetMapping("test/report-count/{testId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public Long getReportCount(@PathVariable String testId) {
        return performanceTestService.getReportCountByTestId(testId);
    }

    @GetMapping("test/follow/{testId}")
    public List<String> getFollows(@PathVariable String testId) {
        return performanceTestService.getFollows(testId);
    }

    @PostMapping("test/update/follows/{testId}")
    @MsRequestLog(module = OperLogModule.PERFORMANCE_TEST)
    public void saveFollows(@PathVariable String testId, @RequestBody List<String> follows) {
        performanceTestService.saveFollows(testId, follows);
    }

    @GetMapping("versions/{loadTestId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadTestDTO> getLoadTestVersions(@PathVariable String loadTestId) {
        return performanceTestService.getLoadTestVersions(loadTestId);
    }

    @GetMapping("get/{version}/{refId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public LoadTestDTO getLoadTestByVersion(@PathVariable String version, @PathVariable String refId) {
        return performanceTestService.getLoadTestByVersion(version, refId);
    }

    @GetMapping("check-file-is-related/{fileId}")
    public void checkFileIsRelated(@PathVariable String fileId) {
        performanceTestService.checkFileIsRelated(fileId);
    }

    @GetMapping("delete/{version}/{refId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_DELETE)
    public void deleteLoadTestByVersion(@PathVariable String version, @PathVariable String refId) {
        performanceTestService.deleteLoadTestByVersion(version, refId);
    }

    /**
     * 使用某资源池的所有性能测试
     *
     * @param poolId 资源池ID
     * @return 性能测试列表
     */
    @GetMapping("/get/using/pool/{poolId}")
    public List<LoadTest> getLoadTestByPoolId(@PathVariable String poolId) {
        return performanceTestService.getLoadTestByPoolId(poolId);
    }

    @GetMapping("/update/cache")
    public void updateNodeCache() {
        consulService.updateCache();
    }

    @PostMapping("/stop/batch")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_RUN)
    @MsRequestLog(module = OperLogModule.PERFORMANCE_TEST)
    public void stopBatch(@RequestBody TaskRequestDTO taskRequestDTO) {
        performanceTestService.stopBatch(taskRequestDTO);
    }

    /**
     * 统计性能用例
     * By.jianguo：
     * 项目报告服务需要统计性能用例
     */
    @PostMapping("/count")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadCaseCountChartResult> countScenarioCaseByRequest(@RequestBody LoadCaseCountRequest request) {
        return performanceTestService.countByRequest(request);
    }

    @PostMapping("/getLoadCaseByIds")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<LoadTest> getLoadCaseByIds(@RequestBody List<String> ids) {
        return performanceTestService.getLoadCaseByIds(ids);
    }

    @GetMapping("/get-base-case/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public List<BaseCase> getBaseCaseByProjectId(@PathVariable String projectId) {
        return performanceTestService.getBaseCaseByProjectId(projectId);
    }
}
