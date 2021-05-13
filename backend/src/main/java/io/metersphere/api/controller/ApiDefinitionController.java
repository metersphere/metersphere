package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.ScheduleInfoSwaggerUrlRequest;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.EsbApiParamService;
import io.metersphere.api.service.EsbImportService;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.json.JSONSchemaGenerator;
import io.metersphere.commons.utils.CronUtils;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.service.CheckPermissionService;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(value = "/api/definition")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class ApiDefinitionController {
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private CheckPermissionService checkPermissionService;
    @Resource
    private EsbApiParamService esbApiParamService;
    @Resource
    private EsbImportService esbImportService;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiDefinitionService.list(request));
    }

    @PostMapping("/list/relevance/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionResult>> listRelevance(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiDefinitionService.listRelevance(request));
    }

    @PostMapping("/list/relevance/review/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionResult>> listRelevanceReview(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiDefinitionService.listRelevanceReview(request));
    }

    @PostMapping("/list/all")
    public List<ApiDefinitionResult> list(@RequestBody ApiDefinitionRequest request) {
        return apiDefinitionService.list(request);
    }

    @PostMapping("/list/batch")
    public List<ApiDefinitionResult> listBatch(@RequestBody ApiBatchRequest request) {
        return apiDefinitionService.listBatch(request);
    }


    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void create(@RequestPart("request") SaveApiDefinitionRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        apiDefinitionService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public ApiDefinitionWithBLOBs update(@RequestPart("request") SaveApiDefinitionRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        checkPermissionService.checkProjectOwner(request.getProjectId());
        return apiDefinitionService.update(request, bodyFiles);
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void delete(@PathVariable String id) {
        apiDefinitionService.delete(id);
    }

    @PostMapping("/deleteBatch")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void deleteBatch(@RequestBody List<String> ids) {
        apiDefinitionService.deleteBatch(ids);
    }

    @PostMapping(value = "/updateEsbRequest")
    public SaveApiDefinitionRequest updateEsbRequest(@RequestBody SaveApiDefinitionRequest request) {
        if (StringUtils.equals(request.getMethod(), "ESB")) {
            //ESB的接口类型数据，采用TCP方式去发送。并将方法类型改为TCP。 并修改发送数据
            request = esbApiParamService.updateEsbRequest(request);
        }
        return request;
    }

    @PostMapping("/deleteBatchByParams")
    public void deleteBatchByParams(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.deleteByParams(request);
    }

    @PostMapping("/removeToGc")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void removeToGc(@RequestBody List<String> ids) {
        apiDefinitionService.removeToGc(ids);
    }

    @PostMapping("/removeToGcByParams")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER}, logical = Logical.OR)
    public void removeToGcByParams(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.removeToGcByParams(request);
    }

    @PostMapping("/reduction")
    public void reduction(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.reduction(request);
    }

    @GetMapping("/get/{id}")
    public ApiDefinition get(@PathVariable String id) {
        return apiDefinitionService.get(id);
    }

    @PostMapping(value = "/run/debug", consumes = {"multipart/form-data"})
    public String runDebug(@RequestPart("request") RunDefinitionRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        return apiDefinitionService.run(request, bodyFiles);
    }

    @PostMapping(value = "/run", consumes = {"multipart/form-data"})
    public String run(@RequestPart("request") RunDefinitionRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        request.setReportId(null);
        return apiDefinitionService.run(request, bodyFiles);
    }

    @GetMapping("/report/get/{testId}/{test}")
    public APIReportResult get(@PathVariable String testId, @PathVariable String test) {
        return apiDefinitionService.getResult(testId, test);
    }

    @GetMapping("/report/getReport/{testId}")
    public APIReportResult getReport(@PathVariable String testId) {
        return apiDefinitionService.getDbResult(testId);
    }

    @GetMapping("/report/getReport/{testId}/{type}")
    public APIReportResult getReport(@PathVariable String testId, @PathVariable String type) {
        return apiDefinitionService.getDbResult(testId, type);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ApiDefinitionImport testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiTestImportRequest request) {
        return apiDefinitionService.apiTestImport(file, request);
    }

    @PostMapping(value = "/export/{type}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ApiExportResult export(@RequestBody ApiBatchRequest request, @PathVariable String type) {
        return apiDefinitionService.export(request, type);
    }

    //定时任务创建
    @PostMapping(value = "/schedule/create")
    public void createSchedule(@RequestBody ScheduleRequest request) throws MalformedURLException {
        apiDefinitionService.createSchedule(request);
    }

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody Schedule request) {
        apiDefinitionService.updateSchedule(request);
    }

    //查找定时任务资源Id
    @PostMapping(value = "/getResourceId")
    public String getResourceId(@RequestBody SwaggerUrlRequest swaggerUrlRequest) {
        return apiDefinitionService.getResourceId(swaggerUrlRequest);
    }

    //查找定时任务列表
    @GetMapping("/scheduleTask/{projectId}")
    public List<SwaggerTaskResult> getSwaggerScheduleList(@PathVariable String projectId) {
        List<SwaggerTaskResult> resultList = apiDefinitionService.getSwaggerScheduleList(projectId);
        int dataIndex = 1;
        for (SwaggerTaskResult swaggerTaskResult :
                resultList) {
            swaggerTaskResult.setIndex(dataIndex++);
            Date nextExecutionTime = CronUtils.getNextTriggerTime(swaggerTaskResult.getRule());
            if (nextExecutionTime != null) {
                swaggerTaskResult.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return resultList;
    }

    //更新定时任务
    @PostMapping(value = "/schedule/updateByPrimyKey")
    public void updateScheduleEnableByPrimyKey(@RequestBody ScheduleInfoSwaggerUrlRequest request) {
        Schedule schedule = scheduleService.getSchedule(request.getTaskId());
        schedule.setEnable(request.getTaskStatus());
        apiDefinitionService.updateSchedule(schedule);
    }

    //删除定时任务和swaggereUrl
    @PostMapping("/schedule/deleteByPrimyKey")
    public void deleteSchedule(@RequestBody ScheduleInfoSwaggerUrlRequest request) {
        apiDefinitionService.deleteSchedule(request);
    }

    @PostMapping("/getReference")
    public ReferenceDTO getReference(@RequestBody ApiScenarioRequest request) {
        return apiDefinitionService.getReference(request);
    }

    @PostMapping("/batch/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void editApiBath(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.editApiBath(request);
    }

    @PostMapping("/batch/editByParams")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void editByParams(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.editApiByParam(request);
    }

    @PostMapping("/relevance")
    public void testPlanRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiDefinitionService.testPlanRelevance(request);
    }

    @PostMapping("/relevance/review")
    public void testCaseReviewRelevance(@RequestBody ApiCaseRelevanceRequest request) {
        apiDefinitionService.testCaseReviewRelevance(request);
    }

    @PostMapping("/preview")
    public String preview(@RequestBody String jsonSchema) {
        return JSONSchemaGenerator.getJson(jsonSchema);
    }

    @GetMapping("/export/esbExcelTemplate")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void testCaseTemplateExport(HttpServletResponse response) {
        esbImportService.templateExport(response);
    }

    @GetMapping("/getMockEnvironment/{projectId}/{protocal}")
    public ApiTestEnvironmentWithBLOBs getMockEnvironment(@PathVariable String projectId, @PathVariable String protocal, HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        String baseUrl = "";
        if (requestUrl.contains("/api/definition")) {
            baseUrl = requestUrl.split("/api/definition")[0];
        }
        return apiTestEnvironmentService.getMockEnvironmentByProjectId(projectId, protocal, baseUrl);
    }

}
