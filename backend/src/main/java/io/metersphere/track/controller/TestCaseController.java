package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.FileService;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.testcase.EditTestCaseRequest;
import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import io.metersphere.track.request.testcase.TestCaseBatchRequest;
import io.metersphere.track.request.testcase.TestCaseMinderEditRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.TestCaseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RequestMapping("/test/case")
@RestController
public class TestCaseController {

    @Resource
    TestCaseService testCaseService;
    @Resource
    private CheckPermissionService checkPermissionService;
    @Resource
    private FileService fileService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.listTestCase(request));
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions("PROJECT_TRACK_CASE:READ")
    public List<TestCaseDTO> list(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCase(request);
    }

    @PostMapping("/list")
    @RequiresPermissions("PROJECT_TRACK_CASE:READ")
    public List<TestCaseDTO> list(@RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.listTestCase(request);
    }

    @PostMapping("/list/minder")
    public List<TestCaseWithBLOBs> listDetail(@RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.listTestCaseForMinder(request);
    }

    /*jenkins项目下所有接口和性能测试用例*/
    @GetMapping("/list/method/{projectId}")
    public List<TestCaseDTO> listByMethod(@PathVariable String projectId) {
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCaseMthod(request);
    }

    @PostMapping("/list/ids")
    public List<TestCaseDTO> getTestPlanCaseIds(@RequestBody QueryTestCaseRequest request) {
        return testCaseService.listTestCaseIds(request);
    }


    @GetMapping("recent/{count}")
    public List<TestCase> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setWorkspaceId(currentWorkspaceId);
        request.setUserId(SessionUtils.getUserId());
        return testCaseService.recentTestPlans(request, count);
    }

    @PostMapping("/relate/{goPage}/{pageSize}")
    public Pager<List<TestCase>> getTestCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getTestCaseRelateList(request));
    }

    @PostMapping("/relate/issue/{goPage}/{pageSize}")
    public Pager<List<TestCaseDTO>> getTestCaseIssueRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getTestCaseIssueRelateList(request));
    }

    @PostMapping("/reviews/case/{goPage}/{pageSize}")
    public Pager<List<TestCase>> getReviewCase(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getReviewCase(request));
    }

    @GetMapping("/get/{testCaseId}")
    public TestCaseWithBLOBs getTestCase(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.getTestCase(testCaseId);
    }

    @GetMapping("/project/{testCaseId}")
    public Project getProjectByTestCaseId(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.getProjectByTestCaseId(testCaseId);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE)
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    public String addTestCase(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        request.setId(UUID.randomUUID().toString());
        return testCaseService.save(request, files);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    public String editTestCase(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return testCaseService.edit(request, files);
    }

    @PostMapping(value = "/edit/testPlan", consumes = {"multipart/form-data"})
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogBeforeDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    public String editTestCaseByTestPlan(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return testCaseService.editTestCase(request, files);
    }

    @PostMapping("/delete/{testCaseId}")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    public int deleteTestCase(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.deleteTestCase(testCaseId);
    }

    @PostMapping("/deleteToGc/{testCaseId}")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    public int deleteToGC(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.deleteTestCaseToGc(testCaseId);
    }


    @PostMapping("/import/{projectId}/{userId}/{importType}")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.IMPORT, project = "#projectId")
    public ExcelResponse testCaseImport(MultipartFile file, @PathVariable String projectId, @PathVariable String userId, @PathVariable String importType, HttpServletRequest request) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseService.testCaseImport(file, projectId, userId, importType,request);
    }

    @PostMapping("/importIgnoreError/{projectId}/{userId}/{importType}")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.IMPORT, project = "#projectId")
    public ExcelResponse testCaseImportIgnoreError(MultipartFile file, @PathVariable String projectId, @PathVariable String userId, @PathVariable String importType, HttpServletRequest request) {
        checkPermissionService.checkProjectOwner(projectId);
        return testCaseService.testCaseImportIgnoreError(file, projectId, userId,importType, request);
    }

    @GetMapping("/export/template/{projectId}/{importType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    public void testCaseTemplateExport(@PathVariable String projectId,@PathVariable String importType,HttpServletResponse response) {
        testCaseService.testCaseTemplateExport(projectId,importType,response);
    }

    @GetMapping("/export/xmindTemplate/{projectId}/{importType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    public void xmindTemplate(@PathVariable String projectId,@PathVariable String importType,HttpServletResponse response) {
        testCaseService.testCaseXmindTemplateExport(projectId,importType,response);
    }

    @PostMapping("/export/testcase")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public void testCaseExport(HttpServletResponse response, @RequestBody TestCaseBatchRequest request) {
        testCaseService.testCaseExport(response, request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void editTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.editTestCaseBath(request);
    }

    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void deleteTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteTestCaseBath(request);
    }

    @PostMapping("/batch/deleteToGc")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void deleteToGcBatch(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteToGcBatch(request);
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    public void reduction(@RequestBody TestCaseBatchRequest request) {
        testCaseService.reduction(request);
    }


    @GetMapping("/file/metadata/{caseId}")
    public List<FileMetadata> getFileMetadata(@PathVariable String caseId) {
        return fileService.getFileMetadataByCaseId(caseId);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> download(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileService.loadFileAsBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(bytes);
    }

    @GetMapping("/file/preview/{fileId}")
    public ResponseEntity<byte[]> preview(@PathVariable String fileId) {
        byte[] bytes = fileService.loadFileAsBytes(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                .body(bytes);
    }

    @PostMapping("/save")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.CREATE, title = "#testCaseWithBLOBs.name", content = "#msClass.getLogDetails(#testCaseWithBLOBs.id)", msClass = TestCaseService.class)
    public TestCaseWithBLOBs saveTestCase(@RequestBody TestCaseWithBLOBs testCaseWithBLOBs) {
        testCaseWithBLOBs.setId(UUID.randomUUID().toString());
        return testCaseService.addTestCase(testCaseWithBLOBs);
    }

    @PostMapping("/minder/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.BATCH_UPDATE, project = "#request.projectId", beforeEvent = "#msClass.getCaseLogDetails(#request)", content = "#msClass.getCaseLogDetails(#request)", msClass = TestCaseService.class)
    public void minderEdit(@RequestBody TestCaseMinderEditRequest request) {
        testCaseService.minderEdit(request);
    }


}
