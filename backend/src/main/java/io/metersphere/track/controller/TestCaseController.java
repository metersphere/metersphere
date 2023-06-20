package io.metersphere.track.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.RelationshipEdgeDTO;
import io.metersphere.dto.TestCaseTestDao;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.FileService;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.dto.TestCaseNodeDTO;
import io.metersphere.track.request.testcase.*;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @PostMapping("/publicList/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> publicList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.publicListTestCase(request));
    }


    @PostMapping("/public/case/node")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseNodeDTO> getPublicCaseNode(@RequestBody QueryTestCaseRequest request) {
        return testCaseService.getPublicCaseNode(request);
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> list(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCase(request);
    }

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> list(@RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.listTestCase(request);
    }

    @PostMapping("/list/minder")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> listForMinder(@RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.listTestCaseForMinder(request);
    }

    @PostMapping("/list/minder/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> listForMinder(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.listTestCaseForMinder(request));
    }

    /*jenkins项目下所有接口和性能测试用例*/
    @GetMapping("/list/method/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> listByMethod(@PathVariable String projectId) {
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCaseMthod(request);
    }

    @GetMapping("/relationship/case/{id}/{relationshipType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<RelationshipEdgeDTO> getRelationshipCase(@PathVariable("id") String id, @PathVariable("relationshipType") String relationshipType) {
        return testCaseService.getRelationshipCase(id, relationshipType);
    }

    @PostMapping("/relationship/add")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void saveRelationshipBatch(@RequestBody TestCaseRelationshipEdgeRequest request) {
        testCaseService.saveRelationshipBatch(request);
    }

    @GetMapping("/relationship/case/count/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public int getRelationshipCase(@PathVariable("id") String id) {
        return testCaseService.getRelationshipCount(id);
    }

    @GetMapping("recent/{count}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCase> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setWorkspaceId(currentWorkspaceId);
        request.setUserId(SessionUtils.getUserId());
        return testCaseService.recentTestPlans(request, count);
    }

    @PostMapping("/relate/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> getTestCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        return testCaseService.getTestCaseRelateList(request, goPage, pageSize);
    }

    @PostMapping("/relationship/relate/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> getRelationshipRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        return testCaseService.getRelationshipRelateList(request, goPage, pageSize);
    }

    @PostMapping("/relate/issue/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> getTestCaseIssueRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getTestCaseIssueRelateList(request));
    }

    @PostMapping("/relevance/api/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public Pager<List<ApiTestCaseDTO>> getTestCaseApiCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getTestCaseApiCaseRelateList(request));
    }

    @PostMapping("/relevance/scenario/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    public Pager<List<ApiScenarioDTO>> getTestCaseScenarioCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiScenarioRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getTestCaseScenarioCaseRelateList(request));
    }

    @PostMapping("/relevance/load/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ)
    public Pager<List<LoadTestDTO>> getTestCaseLoadCaseRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody LoadCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getTestCaseLoadCaseRelateList(request));
    }

    @GetMapping("/relate/test/list/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseTestDao> getRelateTest(@PathVariable String caseId) {
        return testCaseService.getRelateTest(caseId);
    }

    @PostMapping("/relate/test/{type}/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void relateTest(@PathVariable String type, @PathVariable String caseId, @RequestBody List<String> apiIds) {
        testCaseService.relateTest(type, caseId, apiIds);
    }

    @GetMapping("/relate/delete/{caseId}/{testId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void relateDelete(@PathVariable String caseId, @PathVariable String testId) {
        testCaseService.relateDelete(caseId, testId);
    }

    @PostMapping("/reviews/case/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> getReviewCase(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.getReviewCase(request));
    }

    @GetMapping("/get/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public TestCaseWithBLOBs getTestCase(@PathVariable String testCaseId) {
        return testCaseService.getTestCase(testCaseId);
    }

    @GetMapping("/get/step/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public TestCaseWithBLOBs getTestCaseStep(@PathVariable String testCaseId) {
        return testCaseService.getTestCaseStep(testCaseId);
    }

    @GetMapping("/project/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Project getProjectByTestCaseId(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.getProjectByTestCaseId(testCaseId);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, targetClass = TestCaseMapper.class,
            event = NoticeConstants.Event.CREATE, subject = "测试用例通知")
    public TestCase addTestCase(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        if (StringUtils.isBlank(request.getId())) {
            //新增 后端生成 id
            request.setId(UUID.randomUUID().toString());
        } else {
            //复制，前端生成 id
        }
        return testCaseService.save(request, files);
    }

    @PostMapping("/edit/order")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        checkPermissionService.checkTestCaseOwner(request.getMoveId());
        testCaseService.updateOrder(request);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.getTestCase(#request.id)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.UPDATE, subject = "测试用例通知")
    public TestCase editTestCase(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return testCaseService.edit(request, files);
    }

    @PostMapping(value = "/edit/testPlan", consumes = {"multipart/form-data"})
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogBeforeDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    public String editTestCaseByTestPlan(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return testCaseService.editTestCase(request, files, Boolean.TRUE);
    }

    @PostMapping("/delete/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    public int deleteTestCase(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.deleteTestCaseBySameVersion(testCaseId);
    }

    @PostMapping("/deleteToGc/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getTestCase(#testCaseId)", targetClass = TestCaseService.class,
            subject = "测试用例通知")
    public int deleteToGC(@PathVariable String testCaseId) {
        checkPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.deleteTestCaseToGc(testCaseId);
    }

    @GetMapping("/deletePublic/{versionId}/{refId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_ADD_PUBLIC)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getTestCase(#testCaseId)", targetClass = TestCaseService.class,
            subject = "测试用例通知")
    public void deletePublic(@PathVariable String versionId, @PathVariable String refId) {
        testCaseService.deleteTestCasePublic(versionId, refId);
    }


    @PostMapping("/import")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_IMPORT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.IMPORT, project = "#request.projectId")
    public ExcelResponse testCaseImport(@RequestPart("request") TestCaseImportRequest request, @RequestPart("file") MultipartFile file, HttpServletRequest httpRequest) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.testCaseImport(file, request, httpRequest);
    }

    @GetMapping("/export/template/{projectId}/{importType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    public void testCaseTemplateExport(@PathVariable String projectId, @PathVariable String importType, HttpServletResponse response) {
        testCaseService.testCaseTemplateExport(projectId, importType, response);
    }

    @GetMapping("/export/xmindTemplate/{projectId}/{importType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    public void xmindTemplate(@PathVariable String projectId, @PathVariable String importType, HttpServletResponse response) {
        testCaseService.testCaseXmindTemplateExport(projectId, importType, response);
    }

    @PostMapping("/export/testcase")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public void testCaseExport(HttpServletResponse response, @RequestBody TestCaseBatchRequest request) {
        testCaseService.testCaseExport(response, request);
    }

    @PostMapping("/export/testcase/xmind")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public void testCaseXmindExport(HttpServletResponse response, @RequestBody TestCaseBatchRequest request) {
        testCaseService.testCaseXmindExport(response, request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_EDIT,
            PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_ADD_PUBLIC}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.UPDATE, subject = "测试用例通知")
    public void editTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.editTestCaseBath(request);
    }

    @PostMapping("/batch/copy")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_COPY)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void copyTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.copyTestCaseBath(request);
    }

    @PostMapping("/batch/copy/public")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_COPY, PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.CREATE, subject = "测试用例通知")
    public void copyTestCaseBathPublic(@RequestBody TestCaseBatchRequest request) {
        testCaseService.copyTestCaseBathPublic(request);
    }


    @PostMapping("/batch/delete")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void deleteTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteTestCaseBath(request);
    }

    @PostMapping("/batch/deleteToGc")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.DELETE, subject = "测试用例通知")
    public void deleteToGcBatch(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteToGcBatch(request.getIds());
    }

    @PostMapping("/batch/movePublic/deleteToGc")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.DELETE, subject = "测试用例通知")
    public void deleteToGcBatchPublic(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteToGcBatchPublic(request);
    }

    @PostMapping("/reduction")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_RECOVER, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_REDUCTION}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void reduction(@RequestBody TestCaseBatchRequest request) {
        testCaseService.reduction(request);
    }


    @GetMapping("/file/metadata/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<FileMetadata> getFileMetadata(@PathVariable String caseId) {
        return fileService.getFileMetadataByCaseId(caseId);
    }

    @PostMapping("/file/download")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public ResponseEntity<byte[]> download(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileService.loadFileAsBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileOperationRequest.getName(), StandardCharsets.UTF_8) + "\"")
                .body(bytes);
    }

    @GetMapping("/file/preview/{fileId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public ResponseEntity<byte[]> preview(@PathVariable String fileId) {
        byte[] bytes = fileService.loadFileAsBytes(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                .body(bytes);
    }

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.CREATE, title = "#testCaseWithBLOBs.name", content = "#msClass.getLogDetails(#testCaseWithBLOBs.id)", msClass = TestCaseService.class)
    public TestCaseWithBLOBs saveTestCase(@RequestBody EditTestCaseRequest request) {
        request.setId(UUID.randomUUID().toString());
        return testCaseService.addTestCase(request);
    }

    @PostMapping("/minder/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_UPDATE, project = "#request.projectId", beforeEvent = "#msClass.getCaseLogDetails(#request)", content = "#msClass.getCaseLogDetails(#request)", msClass = TestCaseService.class)
    public void minderEdit(@RequestBody TestCaseMinderEditRequest request) {
        testCaseService.minderEdit(request);
    }

    @GetMapping("/follow/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<String> getFollows(@PathVariable String caseId) {
        return testCaseService.getFollows(caseId);
    }

    @PostMapping("/edit/follows/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_PLAN_READ_EDIT)
    public void editTestFollows(@PathVariable String caseId, @RequestBody List<String> follows) {
        testCaseService.saveFollows(caseId, follows);
    }

    @GetMapping("versions/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> getTestCaseVersions(@PathVariable String caseId) {
        return testCaseService.getTestCaseVersions(caseId);
    }

    @GetMapping("get/{version}/{refId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public TestCaseDTO getTestCase(@PathVariable String version, @PathVariable String refId) {
        return testCaseService.getTestCaseByVersion(refId, version);
    }

    @GetMapping("delete/{version}/{refId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    public void deleteTestCaseByVersion(@PathVariable String version, @PathVariable String refId) {
        testCaseService.deleteTestCaseByVersion(refId, version);
    }

    /**
     * 判断是否有其他信息
     *
     * @param caseId 用例 ID
     * @return
     */
    @GetMapping("hasOtherInfo/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Boolean hasOtherInfo(@PathVariable String caseId) {
        return testCaseService.hasOtherInfo(caseId);
    }
}
