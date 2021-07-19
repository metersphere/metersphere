package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiCaseBatchRequest;
import io.metersphere.api.dto.DeleteCheckResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.service.TestPlanApiCaseService;
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

    @PostMapping("/list")
    public List<ApiTestCaseResult> list(@RequestBody ApiTestCaseRequest request) {
        return apiTestCaseService.list(request);
    }

    @GetMapping("/findById/{id}")
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
    public Pager<List<ApiTestCaseDTO>> listSimple(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiTestCaseRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
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

    @PostMapping("/get/caseBLOBs/request")
    public List<ApiTestCaseInfo> getCaseBLOBs(@RequestBody ApiTestCaseRequest request) {

        List<ApiTestCaseInfo> returnList = apiTestCaseService.findApiTestCaseBLOBs(request);
        return returnList;
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @MsAuditLog(module = "api_definition", type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = ApiTestCaseService.class)
    public ApiTestCase create(@RequestPart("request") SaveApiTestCaseRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiTestCaseService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @MsAuditLog(module = "api_definition", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", title = "#request.name", content = "#msClass.getLogDetails(#request)", msClass = ApiTestCaseService.class)
    public ApiTestCase update(@RequestPart("request") SaveApiTestCaseRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiTestCaseService.update(request, bodyFiles);
    }

    @GetMapping("/delete/{id}")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestCaseService.class)
    public void delete(@PathVariable String id) {
        apiTestCaseService.delete(id);
    }

    @GetMapping("/deleteToGc/{id}")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiTestCaseService.class)
    public void deleteToGc(@PathVariable String id) {
        apiTestCaseService.deleteToGc(id);
    }
    @PostMapping("/removeToGc")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiTestCaseService.class)
    public void removeToGc(@RequestBody List<String> ids) {
        apiTestCaseService.removeToGc(ids);
    }

    @GetMapping("/get/{id}")
    public ApiTestCaseWithBLOBs get(@PathVariable String id) {
        return apiTestCaseService.get(id);
    }

    @PostMapping("/batch/edit")
    public void editApiBath(@RequestBody ApiCaseBatchRequest request) {
        apiTestCaseService.editApiBath(request);
    }

    @PostMapping("/batch/editByParam")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public void editApiBathByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.editApiBathByParam(request);
    }
    @PostMapping("/reduction")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#request.ids)", content = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public List<String> reduction(@RequestBody ApiTestBatchRequest request) {
        List<String> cannotReductionTestCaseApiName = apiTestCaseService.reduction(request);
        return cannotReductionTestCaseApiName;
    }

    @PostMapping("/deleteBatch")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiTestCaseService.class)
    public void deleteBatch(@RequestBody List<String> ids) {
        apiTestCaseService.deleteBatch(ids);
    }

    @PostMapping("/deleteBatchByParam")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
    public void deleteBatchByParam(@RequestBody ApiTestBatchRequest request) {
        apiTestCaseService.deleteBatchByParam(request);
    }

    @PostMapping("/deleteToGcByParam")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiTestCaseService.class)
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

    @PostMapping(value = "/jenkins/run")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.EXECUTE, content = "#msClass.getLogDetails(#request.caseId)", msClass = ApiTestCaseService.class)
    public String jenkinsRun(@RequestBody RunCaseRequest request) {
        return apiTestCaseService.run(request);
    }

    @GetMapping(value = "/jenkins/exec/result/{id}")
    public String getExecResult(@PathVariable String id) {
        return apiTestCaseService.getExecResult(id);

    }
}