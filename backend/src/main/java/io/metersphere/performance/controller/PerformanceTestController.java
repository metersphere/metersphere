package io.metersphere.performance.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.FileService;
import io.metersphere.track.request.testplan.*;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "performance")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class PerformanceTestController {
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private FileService fileService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("recent/{count}")
    public List<LoadTestDTO> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setWorkspaceId(currentWorkspaceId);
        request.setUserId(SessionUtils.getUserId());
        PageHelper.startPage(1, count, true);
        return performanceTestService.recentTestPlans(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<LoadTestDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setProjectId(SessionUtils.getCurrentProjectId());
        return PageUtils.setPageInfo(page, performanceTestService.list(request));
    }

    @GetMapping("/list/{projectId}")
    public List<LoadTest> list(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return performanceTestService.getLoadTestByProjectId(projectId);
    }


    @GetMapping("/state/get/{testId}")
    public LoadTest listByTestId(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getLoadTestBytestId(testId);
    }

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public String save(
            @RequestPart("request") SaveTestPlanRequest request,
            @RequestPart(value = "file") List<MultipartFile> files
    ) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return performanceTestService.save(request, files);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    public String edit(
            @RequestPart("request") EditTestPlanRequest request,
            @RequestPart(value = "file", required = false) List<MultipartFile> files
    ) {
        checkPermissionService.checkPerformanceTestOwner(request.getId());
        return performanceTestService.edit(request, files);
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
    public String getJmxContent(@PathVariable String testId) {
        checkPermissionService.checkPerformanceTestOwner(testId);
        return performanceTestService.getJmxContent(testId);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        checkPermissionService.checkPerformanceTestOwner(request.getId());
        performanceTestService.delete(request);
    }

    @PostMapping("/run")
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
        return fileService.getFileMetadataByTestId(testId);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> downloadJmx(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileService.loadFileAsBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(bytes);
    }

    @GetMapping("dashboard/tests")
    public List<DashboardTestDTO> dashboardTests() {
        return performanceTestService.dashboardTests(SessionUtils.getCurrentWorkspaceId());
    }

    @PostMapping(value = "/copy")
    public void copy(@RequestBody SaveTestPlanRequest request) {
        performanceTestService.copy(request);
    }

    @PostMapping(value = "/schedule/create")
    public void createSchedule(@RequestBody Schedule request) {
        performanceTestService.createSchedule(request);
    }

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody Schedule request) {
        performanceTestService.updateSchedule(request);
    }

    @PostMapping("/list/schedule/{goPage}/{pageSize}")
    public List<ScheduleDao> listSchedule(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryScheduleRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return performanceTestService.listSchedule(request);
    }

    @PostMapping("/list/schedule")
    public List<ScheduleDao> listSchedule(@RequestBody QueryScheduleRequest request) {
        return performanceTestService.listSchedule(request);
    }
}
