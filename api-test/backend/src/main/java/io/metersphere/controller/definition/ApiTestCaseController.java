package io.metersphere.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.base.domain.ApiDefinitionExecResultExpand;
import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestEnvironment;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.scenario.ApiScenarioService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/testcase")
public class ApiTestCaseController {

    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiCaseExecuteService apiCaseExecuteService;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioService apiScenarioService;

    @PostMapping("/list")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public List<ApiTestCaseResult> list(@RequestBody ApiTestCaseRequest request) {
        return apiTestCaseService.list(request);
    }

    @PostMapping("/select/by/id")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEFINITION_READ , PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_CASE}, logical = Logical.OR)
    public List<ApiTestCase> selectByIds(@RequestBody ApiTestCaseRequest request) {
        return apiTestCaseService.selectByIds(request);
    }

    @GetMapping("/get-details/{id}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEFINITION_READ , PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_CASE}, logical = Logical.OR)
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

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public Pager<List<ApiTestCaseDTO>> listSimple(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        request.setSelectEnvironment(true);
        apiTestCaseService.initRequestBySearch(request);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiTestCaseService.listSimple(request));
    }

    @GetMapping("/list/{projectId}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public List<ApiTestCaseDTO> list(@PathVariable String projectId) {
        ApiTestCaseRequest request = new ApiTestCaseRequest();
        request.setProjectId(projectId);
        return apiTestCaseService.listSimple(request);
    }

    @GetMapping("/get/pass-rate/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public String  getPassRate(@PathVariable String id) {
        return apiTestCaseService.getPassRate(id);
    }

    @PostMapping("/get/request")
    public Map<String, String> listSimple(@RequestBody ApiTestCaseRequest request) {
        return apiTestCaseService.getRequest(request);
    }

    @GetMapping("/get/env/{id}")
    public ApiTestEnvironment getApiCaseEnvironment(@PathVariable("id") String caseId) {
        return apiTestCaseService.getApiCaseEnvironment(caseId);
    }

    @PostMapping("/get/env")
    public Map<String, String> getApiCaseEnvironments(@RequestBody List<String> caseIds) {
        return apiTestCaseService.getApiCaseEnvironments(caseIds);
    }

    @PostMapping("/list-blobs")
    public List<ApiTestCaseInfo> getCaseBLOBs(@RequestBody ApiTestCaseRequest request) {
        List<ApiTestCaseInfo> returnList = apiTestCaseService.findApiTestCaseBLOBs(request);
        return returnList;
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_CASE)
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

    @GetMapping("/move-gc/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.get(#id)", targetClass = ApiTestCaseService.class, subject = "接口用例通知")
    public void deleteToGc(@PathVariable String id) {
        apiTestCaseService.deleteToGc(id);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    public ApiTestCaseInfo get(@PathVariable String id) {
        return apiTestCaseService.get(id);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_CASE)
    public void editApiBath(@RequestBody ApiCaseEditRequest request) {
        apiTestCaseService.editApiBath(request);
    }

    @PostMapping("/edit-batch")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_UPDATE, target = "#targetClass.getApiCaseByIds(#request.ids)", targetClass = ApiTestCaseService.class, subject = "接口用例通知")
    public void editApiBathByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.editApiBathByParam(request);
    }

    @PostMapping("/sort")
    @MsRequestLog(module = OperLogModule.API_DEFINITION_CASE)
    public void orderCase(@RequestBody ResetOrderRequest request) {
        apiTestCaseService.updateOrder(request);
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public List<String> reduction(@RequestBody ApiTestBatchRequest request) {
        List<String> cannotReductionTestCaseApiName = apiTestCaseService.reduction(request);
        return cannotReductionTestCaseApiName;
    }

    @PostMapping("/del-ids")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiTestCaseService.class)
    public void deleteBatch(@RequestBody List<String> ids) {
        apiTestCaseService.deleteBatch(ids);
    }

    @PostMapping("/del-batch")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public void deleteBatchByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.deleteBatchByParam(request);
    }

    @PostMapping("/move-batch-gc")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_CASE)
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CASE_DELETE, target = "#targetClass.getApiCaseByIds(#request.ids)", targetClass = ApiTestCaseService.class, subject = "接口用例通知")
    public void deleteToGcByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.deleteToGcByParam(request);
    }

    @PostMapping("/get-del-reference")
    public DeleteCheckResult checkDeleteData(@RequestBody ApiTestBatchRequest request) {
        return apiTestCaseService.checkDeleteData(request);
    }

    @PostMapping("/relevance")
    @MsRequestLog(module = OperLogModule.API_DEFINITION_CASE)
    public void testPlanRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByCase(request);
    }

    @PostMapping("/relevance/review")
    @MsRequestLog(module = OperLogModule.API_DEFINITION_CASE)
    public void testCaseReviewRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByApiByReview(request);
    }

    @PostMapping(value = "/batch/run")
    @MsAuditLog(module = OperLogModule.API_DEFINITION_CASE, type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.caseId)", msClass = ApiTestCaseService.class)
    public List<MsExecResponseDTO> batchRun(@RequestBody ApiCaseRunRequest request) {
        request.setTriggerMode(ReportTriggerMode.BATCH.name());
        return apiCaseExecuteService.run(request);
    }

    @PostMapping(value = "/jenkins/run")
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
    @MsRequestLog(module = OperLogModule.API_DEFINITION_CASE)
    public void saveFollows(@PathVariable String testId, @RequestBody List<String> follows) {
        apiTestCaseService.saveFollows(testId, follows);
    }

    @GetMapping("/cited-scenario/{testId}")
    public Integer getCitedScenarioCount(@PathVariable String testId) {
        return apiTestCaseService.getCitedScenarioCount(testId);
    }

    @PostMapping("/list-execute-res/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionExecResultExpand>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPIReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiDefinitionExecResultService.apiReportList(request));
    }

    /**
     * 统计接口用例
     * By.jianguo：
     * 项目报告服务需要统计接口用例
     */
    @PostMapping("/count")
    public List<ApiCountChartResult> countApiCaseByRequest(@RequestBody ApiCountRequest request) {
        return apiTestCaseService.countByRequest(request);
    }

    @PostMapping("/getApiCaseByIds")
    public List<ApiTestCase> getApiCaseByIds(@RequestBody List<String> ids) {
        return apiTestCaseService.getApiCaseByIds(ids);
    }

    @PostMapping("/getScenarioCaseByIds")
    public List<ApiScenario> getScenarioCaseByIds(@RequestBody List<String> ids) {
        return apiScenarioService.getScenarioCaseByIds(ids);
    }

    /**
     * 性能测试调用，同步接口用例的jmx
     *
     * @param request
     * @return
     */
    @PostMapping("/export/jmx")
    public List<JmxInfoDTO> getScenarioCaseByIds(@RequestBody ApiCaseExportJmxRequest request) {
        return apiTestCaseService.exportJmx(request.getCaseIds(), request.getEnvId());
    }

    @PostMapping("/case-count")
    public int getCaseCountById(@RequestBody SaveApiTestCaseRequest request) {
        return apiTestCaseService.getCaseCountById(request.getId());
    }
}
