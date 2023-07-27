package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.*;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.testcase.*;
import io.metersphere.request.testplan.FileOperationRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.FileService;
import io.metersphere.service.TestCaseService;
import io.metersphere.service.wapper.CheckPermissionService;
import io.metersphere.xpack.track.dto.EditTestCaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private CheckPermissionService trackCheckPermissionService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;

    @Resource
    private FileService fileService;

    @Resource
    private BaseProjectApplicationService projectApplicationService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        ProjectApplication projectApplication = projectApplicationService.getProjectApplication(request.getProjectId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
        if (projectApplication != null && StringUtils.isNotEmpty(projectApplication.getTypeValue()) && request.getCombine() != null) {
            request.getCombine().put("caseCustomNum", projectApplication.getTypeValue());
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.listTestCase(request));
    }

    @PostMapping("/public/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Pager<List<TestCaseDTO>> publicList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        testCaseService.setPublicListRequestParam(request);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.publicListTestCase(request));
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> list(@PathVariable String projectId) {
        baseCheckPermissionService.checkProjectOwner(projectId);
        QueryTestCaseRequest request = new QueryTestCaseRequest();
        request.setProjectId(projectId);
        return testCaseService.listTestCase(request);
    }

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseDTO> list(@RequestBody QueryTestCaseRequest request) {
        baseCheckPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.listTestCase(request);
    }

    @PostMapping("/list/minder")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_MINDER_OPERATE)
    public List<TestCaseDTO> listForMinder(@RequestBody QueryTestCaseRequest request) {
        baseCheckPermissionService.checkProjectOwner(request.getProjectId());
        return testCaseService.listTestCaseForMinder(request);
    }

    @PostMapping("/list/minder/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_MINDER_OPERATE)
    public Pager<List<TestCaseDTO>> listForMinder(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestCaseRequest request) {
        baseCheckPermissionService.checkProjectOwner(request.getProjectId());
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testCaseService.listTestCaseForMinder(request));
    }

    @GetMapping("/relationship/case/{id}/{relationshipType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<RelationshipEdgeDTO> getRelationshipCase(@PathVariable("id") String id, @PathVariable("relationshipType") String relationshipType) {
        return testCaseService.getRelationshipCase(id, relationshipType);
    }

    @PostMapping("/relationship/add")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE)
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

    @GetMapping("/relate/test/list/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseTestDao> getRelateTest(@PathVariable String caseId) {
        return testCaseService.getRelateTest(caseId);
    }

    @PostMapping("/relate/test/{type}/{caseId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void relateTest(@PathVariable String type, @PathVariable String caseId, @RequestBody List<String> apiIds) {
        testCaseService.relateTest(type, caseId, apiIds);
    }

    @GetMapping("/relate/delete/{caseId}/{testId}")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE)
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
    public TestCaseDTO getTestCase(@PathVariable String testCaseId) {
        return testCaseService.getTestCase(testCaseId);
    }

    @GetMapping("/get/version/{refId}/{versionId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public TestCaseDTO getTestCaseByVersion(@PathVariable String refId, @PathVariable String versionId) {
        return testCaseService.getTestCaseByVersion(refId, versionId);
    }

    @GetMapping("/get/step/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public TestCaseWithBLOBs getTestCaseStep(@PathVariable String testCaseId) {
        return testCaseService.getTestCaseStep(testCaseId);
    }

    @GetMapping("/get/simple/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public TestCaseWithBLOBs getSimpleCase(@PathVariable String testCaseId) {
        return testCaseService.getSimpleCase(testCaseId);
    }

    @GetMapping("/get/edit/simple/{testCaseId}")
    // 权限校验在service中
    public TestCaseWithBLOBs getSimpleCaseForEdit(@PathVariable String testCaseId) {
        return testCaseService.getSimpleCaseForEdit(testCaseId);
    }

    @GetMapping("/project/{testCaseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public Project getProjectByTestCaseId(@PathVariable String testCaseId) {
        trackCheckPermissionService.checkTestCaseOwner(testCaseId);
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
        }
        return testCaseService.add(request, files);
    }

    @PostMapping("/edit/order")
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        trackCheckPermissionService.checkTestCaseOwner(request.getMoveId());
        testCaseService.updateOrder(request);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.getTestCase(#request.id)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.UPDATE, subject = "测试用例通知")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE}, logical = Logical.OR)
    public TestCase editTestCase(@RequestPart("request") EditTestCaseRequest request) {
        return testCaseService.edit(request);
    }

    @PostMapping(value = "/edit/testPlan", consumes = {"multipart/form-data"})
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_PLAN_READ_RUN}, logical = Logical.OR)
    public String editTestCaseByTestPlan(@RequestPart("request") EditTestCaseRequest request, @RequestPart(value = "file", required = false) List<MultipartFile> files) {
        return testCaseService.editTestCase(request, files);
    }

    @PostMapping("/delete/{testCaseId}")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    public int deleteTestCase(@PathVariable String testCaseId) {
        trackCheckPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.deleteTestCaseBySameVersion(testCaseId);
    }

    @PostMapping("/deleteToGc/{testCaseId}")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getTestCase(#testCaseId)", targetClass = TestCaseService.class,
            subject = "测试用例通知")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    public int deleteToGC(@PathVariable String testCaseId) {
        trackCheckPermissionService.checkTestCaseOwner(testCaseId);
        return testCaseService.deleteTestCaseToGc(testCaseId);
    }

    @GetMapping("/deletePublic/{versionId}/{refId}")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#testCaseId)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getTestCase(#testCaseId)", targetClass = TestCaseService.class,
            subject = "测试用例通知")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_ADD_PUBLIC)
    public void deletePublic(@PathVariable String versionId, @PathVariable String refId) {
        testCaseService.deleteTestCasePublic(versionId, refId);
    }


    @PostMapping("/import")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.IMPORT, project = "#request.projectId")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_IMPORT)
    public ExcelResponse testCaseImport(@RequestPart("request") TestCaseImportRequest request, @RequestPart("file") MultipartFile file, HttpServletRequest httpRequest) {
        baseCheckPermissionService.checkProjectOwner(request.getProjectId());
        try {
            return testCaseService.testCaseImport(file, request, httpRequest);
        } catch (MSException e) {
            Object detail = e.getDetail();
            // 如果异常信息里带了 ExcelResponse，则返回提示信息
            if (detail instanceof ExcelResponse) {
                return (ExcelResponse) detail;
            }
            throw e;
        }
    }

    @GetMapping("/check/permission/{projectId}")
    public boolean checkProjectPermission(@PathVariable String projectId) {
        return baseCheckPermissionService.getUserRelatedProjectIds().contains(projectId);
    }

    @GetMapping("/export/template/{projectId}/{importType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    public void testCaseTemplateExport(@PathVariable String projectId, @PathVariable String importType, HttpServletResponse response) {
        testCaseService.testCaseTemplateExport(projectId, importType, response);
    }

    @GetMapping("/export/xmind/template/{projectId}/{importType}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    public void xmindTemplate(@PathVariable String projectId, @PathVariable String importType, HttpServletResponse response) {
        testCaseService.testCaseXmindTemplateExport(projectId, importType, response);
    }

    @PostMapping("/export/testcase")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public void testCaseExport(HttpServletResponse response, @RequestBody TestCaseExportRequest request) {
        testCaseService.exportTestCaseZip(response, request);
    }

    @PostMapping("/export/testcase/xmind")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EXPORT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public void testCaseXmindExport(HttpServletResponse response, @RequestBody TestCaseBatchRequest request) {
        testCaseService.testCaseXmindExport(response, request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_EDIT,
            PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_ADD_PUBLIC, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_MOVE}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.UPDATE, subject = "测试用例通知")
    public void editTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.editTestCaseBath(request);
    }

    @PostMapping("/batch/relate/demand")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_EDIT)
    public void batchRelateDemand(@RequestBody TestCaseBatchRequest request) {
        testCaseService.batchRelateDemand(request);
    }

    @PostMapping("/batch/copy")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_COPY, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_COPY}, logical = Logical.OR)
    public void copyTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.copyTestCaseBath(request);
    }

    @PostMapping("/batch/copy/public")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_COPY,
            PermissionConstants.PROJECT_TRACK_CASE_READ_COPY, PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_ADD, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.CREATE, subject = "测试用例通知")
    public void copyTestCaseBathPublic(@RequestBody TestCaseBatchRequest request) {
        testCaseService.copyTestCaseBathPublic(request);
    }


    @PostMapping("/batch/delete")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_DELETE}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    public void deleteTestCaseBath(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteTestCaseBath(request);
    }

    @PostMapping("/batch/deleteToGc")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_DELETE}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.DELETE, subject = "测试用例通知")
    public void deleteToGcBatch(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteToGcBatch(request);
    }

    @PostMapping("/batch/movePublic/deleteToGc")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.findByBatchRequest(#request)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.DELETE, subject = "测试用例通知")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_DELETE)
    public void deleteToGcBatchPublic(@RequestBody TestCaseBatchRequest request) {
        testCaseService.deleteToGcBatchPublic(request);
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = TestCaseService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_RECOVER, PermissionConstants.PROJECT_TRACK_CASE_READ_BATCH_REDUCTION}, logical = Logical.OR)
    public void reduction(@RequestBody TestCaseBatchRequest request) {
        testCaseService.reduction(request);
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
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.CREATE, title = "#testCaseWithBLOBs.name", content = "#msClass.getLogDetails(#testCaseWithBLOBs.id)", msClass = TestCaseService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_CREATE)
    public TestCaseWithBLOBs saveTestCase(@RequestBody EditTestCaseRequest request) {
        request.setId(UUID.randomUUID().toString());
        return testCaseService.addTestCase(request);
    }

    @PostMapping("/minder/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_MINDER_OPERATE)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.MINDER_OPERATION, project = "#request.projectId", beforeEvent = "#msClass.getCaseLogDetails(#request)", content = "#msClass.getCaseLogDetails(#request)", msClass = TestCaseService.class)
    public void minderEdit(@RequestBody TestCaseMinderEditRequest request) {
        testCaseService.minderEdit(request);
    }

    @GetMapping("/follow/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<String> getFollows(@PathVariable String caseId) {
        return testCaseService.getFollows(caseId);
    }

    @PostMapping("/edit/follows/{caseId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    @MsRequestLog(module = OperLogModule.TRACK_TEST_CASE)
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

    @GetMapping("/update/custom/num/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void updateCustomNum(@PathVariable String projectId) {
        testCaseService.updateTestCaseCustomNumByProjectId(projectId);
    }

    /**
     * 统计测试用例
     * By.jianguo：
     * 项目报告服务需要统计测试用例
     */
    @PostMapping("/count")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCaseCountChartResult> countTestCaseByRequest(@RequestBody TestCaseCountRequest request) {
        return testCaseService.countTestCaseByRequest(request);
    }

    /**
     * 通过不同类型统计测试用例
     * By.jianguo：
     * 项目报告服务通过不同类型需要统计测试用例。
     */
    @PostMapping("/count/{type}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestAnalysisChartResult> countTestCaseByRequest(@PathVariable String type, @RequestBody TestAnalysisChartRequest request) {
        return testCaseService.countTestCaseByTypeAndRequest(type, request);
    }

    @PostMapping("/select/by/id")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ)
    public List<TestCase> selectByIds(@RequestBody QueryTestCaseRequest request) {
        return testCaseService.selectByIds(request);
    }
}
