package io.metersphere.controller.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.exec.generator.JSONSchemaGenerator;
import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.parse.api.ApiDefinitionImport;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.JSONToDocumentUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.BaseCase;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RelationshipEdgeDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.EsbApiParamService;
import io.metersphere.service.definition.EsbImportService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping(value = "/api/definition")
public class ApiDefinitionController {
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private EsbApiParamService esbApiParamService;
    @Resource
    private EsbImportService esbImportService;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Resource
    private ExecThreadPoolExecutor execThreadPoolExecutor;
    @Resource
    private ApiExecuteService apiExecuteService;

    @PostMapping("/list/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_API_DEFINITION:READ")
    public Pager<List<ApiDefinitionResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        apiDefinitionService.checkFilterHasCoverage(request);
        apiDefinitionService.getApplicationUpdateRule(request);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiDefinitionService.list(request));
    }

    @PostMapping("/list/week/{goPage}/{pageSize}")
    @RequiresPermissions("PROJECT_API_DEFINITION:READ")
    public Pager<List<ApiDefinitionResult>> weekList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiDefinitionService.weekList(request));
    }

    @PostMapping("/list/relevance/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionResult>> listRelevance(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        return apiDefinitionService.listRelevance(request, goPage, pageSize);
    }

    @PostMapping("/list/relevance/review/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionResult>> listRelevanceReview(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, apiDefinitionService.listRelevanceReview(request));
    }


    @PostMapping("/select/by/id")
    public List<ApiDefinition> selectByIds(@RequestBody ApiDefinitionRequest request) {
        return apiDefinitionService.selectByIds(request);
    }

    @PostMapping("/list/all")
    @RequiresPermissions("PROJECT_API_DEFINITION:READ")
    public List<ApiDefinitionResult> list(@RequestBody ApiDefinitionRequest request) {
        return apiDefinitionService.list(request);
    }

    @PostMapping("/list/batch")
    public List<ApiDefinitionResult> listBatch(@RequestBody ApiBatchRequest request) {
        return apiDefinitionService.listBatch(request);
    }


    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.CREATE, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiDefinitionService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.CREATE, subject = "接口定义通知")
    public ApiDefinitionResult create(@RequestPart("request") SaveApiDefinitionRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = ApiDefinitionService.class)
    // @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.UPDATE, subject = "接口定义通知")
    public ApiDefinitionResult update(@RequestPart("request") SaveApiDefinitionRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.update(request, bodyFiles);
    }

    @GetMapping("/delete/{id}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#id)", msClass = ApiDefinitionService.class)
    public void delete(@PathVariable String id) {
        apiDefinitionService.delete(id);
    }

    @PostMapping("/del-ids")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiDefinitionService.class)
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

    @PostMapping("/del-batch")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.BATCH_DEL, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiDefinitionService.class)
    public void deleteBatchByParams(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.deleteByParams(request);
    }

    @PostMapping("/move-gc")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.GC, beforeEvent = "#msClass.getLogDetails(#ids)", msClass = ApiDefinitionService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, target = "#targetClass.getBLOBs(#ids)", targetClass = ApiDefinitionService.class, event = NoticeConstants.Event.DELETE, subject = "接口定义通知")
    public void removeToGc(@RequestBody List<String> ids) {
        apiDefinitionService.removeToGc(ids);
    }

    @PostMapping("/move-gc-batch")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_DELETE_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.BATCH_GC, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiDefinitionService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getBLOBs(#request.ids)", targetClass = ApiDefinitionService.class, subject = "接口定义通知")
    public void removeToGcByParams(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.removeToGcByParams(request);
    }

    @PostMapping("/reduction")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.RESTORE, beforeEvent = "#msClass.getLogDetails(#request.ids)", msClass = ApiDefinitionService.class)
    public void reduction(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.reduction(request);
    }

    @GetMapping("/get/{id}")
    @RequiresPermissions("PROJECT_API_DEFINITION:READ")
    public ApiDefinitionResult getApiDefinitionResult(@PathVariable String id) {
        return apiDefinitionService.getById(id);
    }

    @PostMapping(value = "/run/debug", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.DEBUG, title = "#request.name", project = "#request.projectId")
    public MsExecResponseDTO runDebug(@RequestPart("request") RunDefinitionRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        return apiDefinitionService.run(request, bodyFiles);
    }

    @PostMapping(value = "/run", consumes = {"multipart/form-data"})
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.EXECUTE, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public MsExecResponseDTO run(@RequestPart("request") RunDefinitionRequest request, @RequestPart(value = "files", required = false) List<MultipartFile> bodyFiles) {
        request.setReportId(null);
        return apiDefinitionService.run(request, bodyFiles);
    }

    @GetMapping("/report/getReport/{testId}")
    public ApiReportResult getReport(@PathVariable String testId) {
        return apiDefinitionService.getDbResult(testId);
    }

    @GetMapping("/report/get/{testId}")
    public ApiReportResult getReportById(@PathVariable String testId) {
        return apiDefinitionService.getReportById(testId);
    }

    @GetMapping("/report/getReport/{testId}/{type}")
    public ApiReportResult getReport(@PathVariable String testId, @PathVariable String type) {
        return apiDefinitionService.getDbResult(testId, type);
    }

    @GetMapping("/report/plan/getReport/{testId}/{type}")
    public ApiReportResult getTestPlanApiCaseReport(@PathVariable String testId, @PathVariable String type) {
        return apiDefinitionService.getTestPlanApiCaseReport(testId, type);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_IMPORT_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.IMPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ApiDefinitionImport testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiTestImportRequest request) {
        return apiDefinitionService.apiTestImport(file, request);
    }

    @PostMapping(value = "/export/{type}")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EXPORT_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.EXPORT, sourceId = "#request.id", title = "#request.name", project = "#request.projectId")
    public ApiExportResult export(@RequestBody ApiBatchRequest request, @PathVariable String type) {
        return apiDefinitionService.export(request, type);
    }

    //定时任务创建
    @PostMapping(value = "/schedule/create")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.CREATE, title = "#request.scheduleFrom", project = "#request.projectId")
    public void createSchedule(@RequestBody ScheduleRequest request) {
        apiDefinitionService.createSchedule(request);
    }

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody ScheduleRequest request) {
        apiDefinitionService.updateSchedule(request);
    }

    //查找定时任务资源Id
    @PostMapping(value = "/get-resource")
    public String getResourceId(@RequestBody SwaggerUrlRequest swaggerUrlRequest) {
        return apiDefinitionService.getResourceId(swaggerUrlRequest);
    }

    //查找定时任务列表
    @GetMapping("/schedule/{projectId}")
    public List<SwaggerTaskResult> getSwaggerScheduleList(@PathVariable String projectId) {
        return apiDefinitionService.getSwaggerScheduleList(projectId);
    }

    //更新定时任务更新定时任务
    @PostMapping(value = "/schedule-switch")
    public void updateScheduleEnable(@RequestBody Schedule request) {
        apiDefinitionService.switchSchedule(request);
    }

    //删除定时任务和swaggereUrl
    @PostMapping("/del-schedule")
    public void deleteSchedule(@RequestBody ScheduleRequest request) {
        apiDefinitionService.deleteSchedule(request);
    }

    @PostMapping("/get-reference")
    public ReferenceDTO getReference(@RequestBody ApiScenarioRequest request) {
        return apiDefinitionService.getReference(request);
    }

    @PostMapping("/batch/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_API)
    public void editApiBath(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.editApiBath(request);
    }

    @PostMapping("/edit-batch")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EDIT_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = ApiDefinitionService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.API_DEFINITION_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getBLOBs(#request.ids)", targetClass = ApiDefinitionService.class, subject = "接口定义通知")
    public void editByParams(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.editApiByParam(request);
    }

    @PostMapping("/copy-batch")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_COPY_API)
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.BATCH_UPDATE, beforeEvent = "#msClass.getLogDetails(#request)", content = "#msClass.getLogDetails(#request)", msClass = ApiDefinitionService.class)
    public void batchCopy(@RequestBody ApiBatchRequest request) {
        apiDefinitionService.batchCopy(request);
    }

    @PostMapping("/relevance")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_PLAN, type = OperLogConstants.ASSOCIATE_CASE, content = "#msClass.getLogDetails(#request)", msClass = ApiDefinitionService.class)
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

    @GetMapping("/export-esb-template")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_EXPORT_API)
    public void testCaseTemplateExport(HttpServletResponse response) {
        esbImportService.templateExport(response);
    }

    @GetMapping("/mock-environment/{projectId}")
    public ApiTestEnvironmentWithBLOBs getMockEnvironment(@PathVariable String projectId) {
        return apiTestEnvironmentService.getMockEnvironmentByProjectId(projectId);
    }

    @PostMapping("/sort")
    public void orderCase(@RequestBody ResetOrderRequest request) {
        apiDefinitionService.updateOrder(request);
    }

    @GetMapping("/relationship/{id}/{relationshipType}")
    public List<RelationshipEdgeDTO> getRelationshipApi(@PathVariable("id") String id, @PathVariable("relationshipType") String relationshipType) {
        return apiDefinitionService.getRelationshipApi(id, relationshipType);
    }

    @GetMapping("/relationship/count/{id}")
    public int getRelationshipApi(@PathVariable("id") String id) {
        return apiDefinitionService.getRelationshipCount(id);
    }

    @PostMapping("/relationship/relate/{goPage}/{pageSize}")
    public Pager<List<ApiDefinitionResult>> getRelationshipRelateList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        return apiDefinitionService.getRelationshipRelateList(request, goPage, pageSize);
    }

    @PostMapping("/relationship/add")
    public void saveRelationshipBatch(@RequestBody ApiDefinitionRelationshipEdgeRequest request) {
        apiDefinitionService.saveRelationshipBatch(request);
    }

    @GetMapping("/follow/{definitionId}")
    public List<String> getFollows(@PathVariable String definitionId) {
        return apiDefinitionService.getFollows(definitionId);
    }

    @GetMapping("/document/{id}/{type}")
    public List<DocumentElement> getDocument(@PathVariable String id, @PathVariable String type) {
        return apiDefinitionService.getDocument(id, type);
    }

    @PostMapping("/generator")
    public List<DocumentElement> jsonGenerator(@RequestBody Body body) {
        return JSONToDocumentUtil.getDocument(body.getRaw(), body.getType());
    }

    @PostMapping("/update/follows/{definitionId}")
    public void saveFollows(@PathVariable String definitionId, @RequestBody List<String> follows) {
        apiDefinitionService.saveFollows(definitionId, follows);
    }


    @PostMapping("/delete/follows/batch")
    public void deleteFollows(@RequestBody List<String> definitionIds) {
        apiDefinitionService.deleteFollows(definitionIds);
    }

    @GetMapping("/getWorkerQueue")
    public String getWorkerQueue() {
        return execThreadPoolExecutor.getWorkerQueue();
    }

    @GetMapping("versions/{definitionId}")
    public List<ApiDefinitionResult> getApiDefinitionVersions(@PathVariable String definitionId) {
        return apiDefinitionService.getApiDefinitionVersions(definitionId);
    }

    @GetMapping("get/{version}/{refId}")
    public ApiDefinitionResult getApiDefinition(@PathVariable String version, @PathVariable String refId) {
        return apiDefinitionService.getApiDefinitionByVersion(refId, version);
    }

    @GetMapping("delete/{version}/{refId}")
    public void deleteApiDefinition(@PathVariable String version, @PathVariable String refId) {
        apiDefinitionService.deleteApiDefinitionByVersion(refId, version);
    }

    @GetMapping("/cited-scenario/{definitionId}")
    public Integer getCitedScenarioCount(@PathVariable String definitionId) {
        return apiDefinitionService.getCitedScenarioCount(definitionId);
    }

    @PostMapping("/raw-to-xml")
    public List<TcpTreeTableDataStruct> rawToXml(@RequestBody ParseTreeDataDTO parseTreeDataDTO) {
        return apiDefinitionService.rawToXml(parseTreeDataDTO.getStringData());
    }

    @PostMapping("/get-hash-tree")
    public String getHashTree(@RequestBody MsJSR223Processor request) {
        return apiExecuteService.getHashTree(request);
    }

    @PostMapping("/update/file")
    public void updateFileMetadataId(@RequestBody List<ReplaceFileIdRequest> requestList) {
        apiDefinitionService.updateFileMetadataId(requestList);
    }

    @GetMapping("/get-base-case/{projectId}")
    @RequiresPermissions("PROJECT_API_DEFINITION:READ")
    public List<BaseCase> getBaseCaseByProjectId(@PathVariable String projectId) {
        return apiDefinitionService.getBaseCaseByProjectId(projectId);
    }
}
