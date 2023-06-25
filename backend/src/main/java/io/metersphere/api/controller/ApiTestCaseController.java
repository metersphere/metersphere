package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiCaseEditRequest;
import io.metersphere.api.dto.ApiCaseRunRequest;
import io.metersphere.api.dto.DeleteCheckResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestEnvironment;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.service.TestPlanApiCaseService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/testcase")
public class ApiTestCaseController {

    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ApiCaseExecuteService apiCaseExecuteService;
    @Resource
    private ApiExecuteService apiExecuteService;

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public List<ApiTestCaseResult> list(@RequestBody ApiTestCaseRequest request) {
        return apiTestCaseService.list(request);
    }

    @GetMapping("/findById/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public ApiTestCaseResult single(@PathVariable String id) {
        ApiTestCaseRequest request = new ApiTestCaseRequest();
        request.setId(id);
        List<ApiTestCaseResult> list = apiTestCaseService.list(request);
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @GetMapping("/getStateByTestPlan/{id}")
    public String getStateByTestPlan(@PathVariable String id) {
        String status = testPlanApiCaseService.getState(id);
        return status;

    }

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public Pager<List<ApiTestCaseDTO>> listSimple(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setSelectEnvironment(true);
        return PageUtils.setPageInfo(page, apiTestCaseService.listSimple(request));
    }

    @GetMapping("/list/{projectId}")
    public List<ApiTestCaseDTO> list(@PathVariable String projectId) {
        ApiTestCaseRequest request = new ApiTestCaseRequest();
        request.setProjectId(projectId);
        return apiTestCaseService.listSimple(request);
    }

    @PostMapping("/get/request")
    public Map<String, String> listSimple(@RequestBody ApiTestCaseRequest request) {
        return apiTestCaseService.getRequest(request);
    }

    @GetMapping("/get/env/{id}")
    public ApiTestEnvironment getApiCaseEnvironment(@PathVariable("id") String caseId) {
        return apiTestCaseService.getApiCaseEnvironment(caseId);
    }

    @PostMapping("/get/caseBLOBs/request")
    public List<ApiTestCaseInfo> getCaseBLOBs(@RequestBody ApiTestCaseRequest request) {

        List<ApiTestCaseInfo> returnList = apiTestCaseService.findApiTestCaseBLOBs(request);
        return returnList;
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @RequiresPermissions(value= {PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_CASE, PermissionConstants.PROJECT_API_DEFINITION_READ_COPY_CASE}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_CREATE, subject = "接口用例通知")
    public ApiTestCase create(@RequestPart("request") SaveApiTestCaseRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiTestCaseService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, subject = "接口用例通知")
    public ApiTestCase update(@RequestPart("request") SaveApiTestCaseRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiTestCaseService.update(request, bodyFiles);
    }

    @PostMapping(value = "/updateExecuteInfo")
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, subject = "接口用例通知")
    public ApiTestCase updateExecuteInfo(@RequestBody SaveApiTestCaseRequest request) {
        return apiTestCaseService.updateExecuteInfo(request);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestCaseService.class)
    public void delete(@PathVariable String id) {
        apiTestCaseService.delete(id);
    }

    @GetMapping("/deleteToGc/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.get(#id)", targetClass = ApiTestCaseService.class,
            subject = "接口用例通知")
    public void deleteToGc(@PathVariable String id) {
        apiTestCaseService.deleteToGc(id);
    }

    @PostMapping("/removeToGc")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiTestCaseService.class)
    public void removeToGc(@RequestBody List<String> ids) {
        apiTestCaseService.removeToGc(ids);
    }

    @GetMapping("/get/{id}")
    public ApiTestCaseInfo get(@PathVariable String id) {
        return apiTestCaseService.get(id);
    }

    @PostMapping("/batch/edit")
    public void editApiBath(@RequestBody ApiCaseEditRequest request) {
        apiTestCaseService.editApiBath(request);
    }

    @PostMapping("/batch/editByParam")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, target = "#targetClass.getApiCaseByIds(#request.ids)", targetClass = ApiTestCaseService.class,
            subject = "接口用例通知")
    public void editApiBathByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.editApiBathByParam(request);
    }

    @PostMapping("/edit/order")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        apiTestCaseService.updateOrder(request);
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public List<String> reduction(@RequestBody ApiTestBatchRequest request) {
        List<String> cannotReductionTestCaseApiName = apiTestCaseService.reduction(request);
        return cannotReductionTestCaseApiName;
    }

    @PostMapping("/deleteBatch")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiTestCaseService.class)
    public void deleteBatch(@RequestBody List<String> ids) {
        apiTestCaseService.deleteBatch(ids);
    }

    @PostMapping("/deleteBatchByParam")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public void deleteBatchByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.deleteBatchByParam(request);
    }

    @PostMapping("/deleteToGcByParam")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.getApiCaseByIds(#request.ids)", targetClass = ApiTestCaseService.class,
            subject = "接口用例通知")
    public void deleteToGcByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.deleteToGcByParam(request);
    }

    @PostMapping("/checkDeleteDatas")
    public DeleteCheckResult checkDeleteDatas(@RequestBody ApiTestBatchRequest request) {
        return apiTestCaseService.checkDeleteDatas(request);
    }

    @PostMapping("/relevance")
    public void testPlanRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByCase(request);
    }

    @PostMapping("/relevance/review")
    public void testCaseReviewRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByApiByReview(request);
    }

    @PostMapping(value = "/batch/run")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_RUN)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.caseId)", msClass = ApiTestCaseService.class)
    public List<MsExecResponseDTO> batchRun(@RequestBody ApiCaseRunRequest request) {
        request.setTriggerMode(ReportTriggerMode.BATCH.name());
        return apiCaseExecuteService.run(request);
    }

    @PostMapping(value = "/jenkins/run")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_RUN)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.caseId)", msClass = ApiTestCaseService.class)
    public MsExecResponseDTO jenkinsRun(@RequestBody RunCaseRequest request) {
        return apiExecuteService.jenkinsRun(request);
    }

    @GetMapping(value = "/jenkins/exec/result/{id}")
    public String getExecResult(@PathVariable String id) {
        return apiTestCaseService.getExecResult(id);

    }

    @GetMapping("follow/{testId}")
    public List<String> getFollows(@PathVariable String testId) {
        return apiTestCaseService.getFollows(testId);
    }

    @PostMapping("/update/follows/{testId}")
    public void saveFollows(@PathVariable String testId, @RequestBody List<String> follows) {
        apiTestCaseService.saveFollows(testId, follows);
    }
}
