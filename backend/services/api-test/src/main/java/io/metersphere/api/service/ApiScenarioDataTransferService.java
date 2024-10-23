package io.metersphere.api.service;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.constants.ApiScenarioExportType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiDefinitionExportDetail;
import io.metersphere.api.dto.converter.ApiScenarioImportParseResult;
import io.metersphere.api.dto.converter.ApiScenarioPreImportAnalysisResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.export.ApiScenarioExportResponse;
import io.metersphere.api.dto.export.MetersphereApiScenarioExportResponse;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.ApiScenarioImportParser;
import io.metersphere.api.parser.ImportParserFactory;
import io.metersphere.api.service.definition.ApiDefinitionExportService;
import io.metersphere.api.service.definition.ApiDefinitionImportService;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.service.scenario.ApiScenarioLogService;
import io.metersphere.api.service.scenario.ApiScenarioModuleService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.api.utils.ApiScenarioImportUtils;
import io.metersphere.functional.domain.ExportTask;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.PermissionCheckService;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.ExportMsgDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.manager.ExportTaskManager;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.service.FileService;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.socket.ExportWebSocketHandler;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.TreeNodeParseUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.metersphere.project.utils.NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioDataTransferService {

    private final ThreadLocal<Long> currentApiScenarioOrder = new ThreadLocal<>();

    private final ThreadLocal<Long> currentScenarioModuleOrder = new ThreadLocal<>();

    private final ThreadLocal<Long> currentApiModuleOrder = new ThreadLocal<>();

    private final ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();

    private final ThreadLocal<Long> currentApiTestCaseOrder = new ThreadLocal<>();

    private static final String EXPORT_CASE_TMP_DIR = "apiScenario";

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ExportTaskManager exportTaskManager;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Resource
    private FileService fileService;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ApiScenarioLogService apiScenarioLogService;
    @Resource
    private ApiDefinitionExportService apiDefinitionExportService;
    @Resource
    private PermissionCheckService permissionCheckService;
    @Resource
    private ApiDefinitionImportService apiDefinitionImportService;
    @Resource
    private ApiDefinitionModuleService apiDefinitionModuleService;
    @Resource
    private ApiTestCaseService apiTestCaseService;

    public String exportScenario(ApiScenarioBatchExportRequest request, String type, String userId) {
        String returnId;
        try {
            exportTaskManager.exportCheck(request.getProjectId(), ExportConstants.ExportType.API_SCENARIO.toString(), userId);
            returnId = exportTaskManager.exportAsyncTask(
                    request.getProjectId(),
                    request.getFileId(), userId,
                    ExportConstants.ExportType.API_SCENARIO.name(), request, t -> {
                        try {
                            return exportApiScenarioZip(request, type, userId);
                        } catch (Exception e) {
                            throw new MSException(e);
                        }
                    }).getId();
        } catch (InterruptedException e) {
            LogUtils.error("导出失败：" + e);
            throw new MSException(e);
        }
        return returnId;
    }


    public void importScenario(MultipartFile file, ApiScenarioImportRequest request) {
        ApiScenarioImportParser parser = ImportParserFactory.getApiScenarioImportParser(request.getType());
        ApiScenarioImportParseResult parseResult;
        try {
            assert parser != null;
            parseResult = parser.parse(file.getInputStream(), request);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("parse_data_error"));
        }

        if (CollectionUtils.isEmpty(parseResult.getImportScenarioList())) {
            throw new MSException(Translator.get("parse_empty_data"));
        }
        //解析
        ApiScenarioPreImportAnalysisResult preImportAnalysisResult = this.importAnalysis(
                parseResult, request.getOperator(), request.getProjectId(), request.getModuleId(), apiScenarioModuleService.getTree(request.getProjectId()));
        //存储
        this.save(preImportAnalysisResult, request.getProjectId(), request.getOperator(), request.isCoverData());
    }

    private void save(ApiScenarioPreImportAnalysisResult preImportAnalysisResult, String projectId, String operator, boolean isCoverData) {
        List<LogDTO> operationLogs = new ArrayList<>();
        // 更新、修改数据
        {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            // 接口定义模块
            if (CollectionUtils.isNotEmpty(preImportAnalysisResult.getInsertApiModuleList())) {
                Map<String, List<BaseTreeNode>> projectMap = preImportAnalysisResult.getInsertApiModuleList().stream().collect(Collectors.groupingBy(BaseTreeNode::getProjectId));
                ApiDefinitionModuleMapper batchApiModuleMapper = sqlSession.getMapper(ApiDefinitionModuleMapper.class);
                projectMap.forEach((targetProjectId, nodeList) -> {
                    currentApiModuleOrder.remove();
                    nodeList.forEach(t -> {
                        ApiDefinitionModule module = new ApiDefinitionModule();
                        module.setId(t.getId());
                        module.setName(t.getName());
                        module.setParentId(t.getParentId());
                        module.setProjectId(t.getProjectId());
                        module.setCreateUser(operator);
                        module.setPos(getImportNextOrder(apiDefinitionModuleService::getNextOrder, currentApiModuleOrder, targetProjectId));
                        module.setCreateTime(System.currentTimeMillis());
                        module.setUpdateUser(operator);
                        module.setUpdateTime(System.currentTimeMillis());
                        batchApiModuleMapper.insertSelective(module);
                    });
                    sqlSession.flushStatements();
                });
            }
            // 接口定义
            if (CollectionUtils.isNotEmpty(preImportAnalysisResult.getInsertApiDefinitions())) {
                Map<String, List<ApiDefinitionDetail>> projectMap = preImportAnalysisResult.getInsertApiDefinitions().stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getProjectId));
                ApiDefinitionMapper batchApiMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
                ApiDefinitionBlobMapper batchApiBlobMapper = sqlSession.getMapper(ApiDefinitionBlobMapper.class);
                projectMap.forEach((targetProjectId, apiList) -> {
                    currentApiOrder.remove();
                    String versionId = extBaseProjectVersionMapper.getDefaultVersion(targetProjectId);
                    for (ApiDefinitionDetail t : apiList) {
                        ApiDefinition apiDefinition = new ApiDefinition();
                        BeanUtils.copyBean(apiDefinition, t);
                        apiDefinition.setPos(getImportNextOrder(apiDefinitionImportService::getNextOrder, currentApiOrder, targetProjectId));
                        apiDefinition.setLatest(true);
                        apiDefinition.setStatus(ApiDefinitionStatus.PROCESSING.name());
                        apiDefinition.setRefId(apiDefinition.getId());
                        apiDefinition.setVersionId(versionId);
                        apiDefinition.setCreateUser(operator);
                        apiDefinition.setCreateTime(System.currentTimeMillis());
                        apiDefinition.setUpdateUser(operator);
                        apiDefinition.setUpdateTime(System.currentTimeMillis());
                        batchApiMapper.insertSelective(apiDefinition);
                        //插入blob数据
                        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
                        apiDefinitionBlob.setId(apiDefinition.getId());
                        apiDefinitionBlob.setRequest(ApiDataUtils.toJSONString(t.getRequest()).getBytes(StandardCharsets.UTF_8));
                        apiDefinitionBlob.setResponse(ApiDataUtils.toJSONString(t.getResponse()).getBytes(StandardCharsets.UTF_8));
                        batchApiBlobMapper.insertSelective(apiDefinitionBlob);
                    }
                    sqlSession.flushStatements();
                });


            }
            // 接口用例
            if (CollectionUtils.isNotEmpty(preImportAnalysisResult.getInsertApiTestCaseList())) {
                Map<String, List<ApiTestCaseDTO>> projectMap = preImportAnalysisResult.getInsertApiTestCaseList().stream().collect(Collectors.groupingBy(ApiTestCaseDTO::getProjectId));

                ApiTestCaseMapper batchApiCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
                ApiTestCaseBlobMapper batchApiBlobCaseMapper = sqlSession.getMapper(ApiTestCaseBlobMapper.class);

                projectMap.forEach((targetProjectId, testCaseList) -> {
                    currentApiTestCaseOrder.remove();
                    String versionId = extBaseProjectVersionMapper.getDefaultVersion(targetProjectId);
                    testCaseList.forEach(t -> {
                        ApiTestCase apiTestCase = new ApiTestCase();
                        BeanUtils.copyBean(apiTestCase, t);
                        apiTestCase.setProjectId(targetProjectId);
                        apiTestCase.setPos(getImportNextOrder(apiTestCaseService::getNextOrder, currentApiTestCaseOrder, targetProjectId));
                        apiTestCase.setNum(NumGenerator.nextNum(targetProjectId, ApplicationNumScope.API_DEFINITION));
                        apiTestCase.setStatus(ApiDefinitionStatus.PROCESSING.name());
                        apiTestCase.setVersionId(versionId);
                        apiTestCase.setCreateUser(operator);
                        apiTestCase.setCreateTime(System.currentTimeMillis());
                        apiTestCase.setUpdateUser(operator);
                        apiTestCase.setUpdateTime(System.currentTimeMillis());
                        batchApiCaseMapper.insertSelective(apiTestCase);
                        //插入blob数据
                        ApiTestCaseBlob apiTestCaseBlob = new ApiTestCaseBlob();
                        apiTestCaseBlob.setId(apiTestCase.getId());

                        apiTestCaseBlob.setRequest(apiDefinitionImportService.getMsTestElementStr(t.getRequest()).getBytes());
                        batchApiBlobCaseMapper.insertSelective(apiTestCaseBlob);
                    });
                    sqlSession.flushStatements();
                });
            }

            //场景模块
            this.insertModule(operator, preImportAnalysisResult.getInsertScenarioModuleList(), sqlSession);
            if (isCoverData) {
                this.updateScenarios(projectId, operator, preImportAnalysisResult.getUpdateApiScenarioData(), sqlSession);
            }
            String versionId = extBaseProjectVersionMapper.getDefaultVersion(projectId);
            //场景
            this.insertScenarios(operator, versionId, preImportAnalysisResult.getInsertApiScenarioData(), sqlSession);

            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        //记录log以及发送通知
        {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            List<ApiScenarioImportDetail> noticeCreateLists = new ArrayList<>();
            List<ApiScenarioImportDetail> noticeUpdateLists = new ArrayList<>();

            preImportAnalysisResult.getInsertScenarioModuleList().forEach(t ->
                    operationLogs.add(ApiDefinitionImportUtils.genImportLog(project, t.getId(), t.getName(), t, OperationLogModule.API_SCENARIO_MANAGEMENT_MODULE, operator, OperationLogType.ADD.name()))
            );

            preImportAnalysisResult.getInsertApiScenarioData().forEach(t -> {
                ApiScenarioImportDetail scenarioImportDetail = new ApiScenarioImportDetail();
                BeanUtils.copyBean(scenarioImportDetail, t);
                noticeCreateLists.add(scenarioImportDetail);
                operationLogs.add(ApiScenarioImportUtils.genImportLog(project, t.getId(), t.getName(), scenarioImportDetail, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO, operator, OperationLogType.IMPORT.name()));
            });

            preImportAnalysisResult.getUpdateApiScenarioData().forEach(t -> {
                ApiScenarioImportDetail scenarioImportDetail = new ApiScenarioImportDetail();
                BeanUtils.copyBean(scenarioImportDetail, t);
                noticeUpdateLists.add(scenarioImportDetail);
                operationLogs.add(ApiScenarioImportUtils.genImportLog(project, t.getId(), t.getName(), scenarioImportDetail, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO, operator, OperationLogType.UPDATE.name()));
            });

            //发送通知
            User user = userMapper.selectByPrimaryKey(operator);
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_SCENARIO_TASK, NoticeConstants.Event.CREATE,
                    new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeCreateLists), Map.class)), user, projectId);
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_SCENARIO_TASK, NoticeConstants.Event.UPDATE,
                    new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeUpdateLists), Map.class)), user, projectId);
        }
        operationLogService.batchAdd(operationLogs);
    }

    private void updateScenarios(String projectId, String operator, List<ApiScenarioImportDetail> updateApiScenarioData, SqlSession sqlSession) {
        if (CollectionUtils.isEmpty(updateApiScenarioData)) {
            return;
        }
        ApiScenarioMapper scenarioBatchMapper = sqlSession.getMapper(ApiScenarioMapper.class);
        ApiScenarioBlobMapper scenarioBlobBatchMapper = sqlSession.getMapper(ApiScenarioBlobMapper.class);
        ApiScenarioCsvStepMapper csvStepBatchMapper = sqlSession.getMapper(ApiScenarioCsvStepMapper.class);
        ApiScenarioStepMapper stepBatchMapper = sqlSession.getMapper(ApiScenarioStepMapper.class);
        ApiScenarioStepBlobMapper stepBlobBatchMapper = sqlSession.getMapper(ApiScenarioStepBlobMapper.class);
        ExtApiScenarioStepMapper extStepMapper = sqlSession.getMapper(ExtApiScenarioStepMapper.class);
        SubListUtils.dealForSubList(updateApiScenarioData, 100, list -> {
            //首先筛选出空步骤的场景，用于删除已有步骤
            List<String> emptyStepScenarioIds = new ArrayList<>();
            {
                list.forEach(t -> {
                    if (t.getSteps() != null && CollectionUtils.isEmpty(t.getSteps())) {
                        emptyStepScenarioIds.add(t.getId());
                    }
                });
                if (!emptyStepScenarioIds.isEmpty()) {
                    ApiScenarioStepExample deleteStepExample = new ApiScenarioStepExample();
                    deleteStepExample.createCriteria().andScenarioIdIn(emptyStepScenarioIds);
                    stepBatchMapper.deleteByExample(deleteStepExample);
                    ApiScenarioStepBlobExample deleteStepBlobExample = new ApiScenarioStepBlobExample();
                    deleteStepBlobExample.createCriteria().andScenarioIdIn(emptyStepScenarioIds);
                    stepBlobBatchMapper.deleteByExample(deleteStepBlobExample);
                }
            }

            //更新场景
            list.forEach(request -> {
                // 更新基础信息
                ApiScenario scenario = BeanUtils.copyBean(new ApiScenario(), request);
                scenario.setUpdateUser(operator);
                scenario.setUpdateTime(System.currentTimeMillis());
                scenario.setStepTotal(CollectionUtils.isNotEmpty(request.getSteps()) ? request.getSteps().size() : 0);
                scenarioBatchMapper.updateByPrimaryKeySelective(scenario);
                if (request.getScenarioConfig() != null) {
                    // 更新场景配置
                    ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
                    apiScenarioBlob.setId(scenario.getId());
                    apiScenarioBlob.setConfig(JSON.toJSONString(request.getScenarioConfig()).getBytes());
                    scenarioBlobBatchMapper.updateByPrimaryKeyWithBLOBs(apiScenarioBlob);
                }


                if (!emptyStepScenarioIds.contains(scenario.getId())) {
                    List<String> originStepDetailIds = extStepMapper.getStepIdsByScenarioId(scenario.getId());
                    // 更新场景步骤
                    this.updateApiScenarioStep(request, operator, originStepDetailIds, csvStepBatchMapper, stepBatchMapper, stepBlobBatchMapper);
                }

                // 处理 csv 文件
                apiScenarioService.handleCsvUpdate(request.getScenarioConfig(), scenario, operator);

            });
            sqlSession.flushStatements();
        });
    }

    private void updateApiScenarioStep(ApiScenarioImportDetail request, String userId, List<String> originStepDetailIds,
                                       ApiScenarioCsvStepMapper csvStepBatchMapper, ApiScenarioStepMapper apiScenarioStepBatchMapper, ApiScenarioStepBlobMapper stepBlobBatchMapper) {
        List<ApiScenarioStepRequest> steps = request.getSteps();
        String scenarioId = request.getId();
        String projectId = request.getProjectId();
        // steps 不为 null 则修改
        if (steps != null) {
            if (CollectionUtils.isEmpty(steps)) {
                return;
            }
            apiScenarioService.checkCircularRef(scenarioId, steps);

            List<ApiScenarioCsvStep> scenarioCsvSteps = new ArrayList<>();
            // 获取待更新的步骤
            List<ApiScenarioStep> apiScenarioSteps = apiScenarioService.getApiScenarioSteps(null, steps, scenarioCsvSteps);
            apiScenarioSteps.forEach(step -> step.setScenarioId(scenarioId));
            scenarioCsvSteps.forEach(step -> step.setScenarioId(scenarioId));

            scenarioCsvSteps = apiScenarioService.filterNotExistCsv(request.getScenarioConfig(), scenarioCsvSteps);
            this.saveStepCsv(scenarioId, scenarioCsvSteps, csvStepBatchMapper);
            //            apiScenarioService.
            // 获取待更新的步骤详情
            apiScenarioService.addSpecialStepDetails(steps, request.getStepDetails());
            List<ApiScenarioStepBlob> updateStepBlobs = apiScenarioService.getUpdateStepBlobs(apiScenarioSteps, request.getStepDetails());
            updateStepBlobs.forEach(step -> step.setScenarioId(scenarioId));

            List<String> stepIds = apiScenarioSteps.stream().map(ApiScenarioStep::getId).collect(Collectors.toList());
            List<String> deleteStepIds = ListUtils.subtract(originStepDetailIds, stepIds);

            // 步骤表-全部先删除再插入
            ApiScenarioStepExample deleteStepExample = new ApiScenarioStepExample();
            deleteStepExample.createCriteria().andScenarioIdEqualTo(scenarioId);
            apiScenarioStepBatchMapper.deleteByExample(deleteStepExample);
            apiScenarioStepBatchMapper.batchInsert(apiScenarioSteps);

            // 详情表-删除已经删除的步骤详情
            SubListUtils.dealForSubList(deleteStepIds, 100, subIds -> {
                ApiScenarioStepBlobExample stepBlobExample = new ApiScenarioStepBlobExample();
                stepBlobExample.createCriteria().andIdIn(subIds);
                stepBlobBatchMapper.deleteByExample(stepBlobExample);
                // 批量删除关联文件
                String scenarioStepDirPrefix = DefaultRepositoryDir.getApiScenarioStepDir(projectId, scenarioId, StringUtils.EMPTY);
                apiFileResourceService.deleteByResourceIds(scenarioStepDirPrefix, subIds, projectId, userId, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO);
            });

            // 添加新增的步骤详情
            List<ApiScenarioStepBlob> addApiScenarioStepsDetails = updateStepBlobs.stream()
                    .filter(step -> !originStepDetailIds.contains(step.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addApiScenarioStepsDetails)) {
                stepBlobBatchMapper.batchInsert(addApiScenarioStepsDetails);
            }
            // 更新原有的步骤详情
            updateStepBlobs.stream()
                    .filter(step -> originStepDetailIds.contains(step.getId()))
                    .forEach(stepBlobBatchMapper::updateByPrimaryKeySelective);
        } else if (MapUtils.isNotEmpty(request.getStepDetails())) {
            // steps 为 null，stepDetails 不为 null，则只更新详情
            // 更新原有的步骤详情
            request.getStepDetails().forEach((stepId, stepDetail) -> {
                if (originStepDetailIds.contains(stepId)) {
                    ApiScenarioStepBlob apiScenarioStepBlob = apiScenarioService.getApiScenarioStepBlob(stepId, stepDetail);
                    stepBlobBatchMapper.updateByPrimaryKeySelective(apiScenarioStepBlob);
                }
            });
        }
    }

    private void insertModule(String operator, List<BaseTreeNode> insertModuleList, SqlSession sqlSession) {
        if (CollectionUtils.isEmpty(insertModuleList)) {
            return;
        }
        ApiScenarioModuleMapper batchApiDefinitionMapper = sqlSession.getMapper(ApiScenarioModuleMapper.class);

        Map<String, List<BaseTreeNode>> projectMap = insertModuleList.stream().collect(Collectors.groupingBy(BaseTreeNode::getProjectId));

        projectMap.forEach((targetProjectId, list) -> {
            list.forEach(t -> {
                ApiScenarioModule module = new ApiScenarioModule();
                module.setId(t.getId());
                module.setName(t.getName());
                module.setParentId(t.getParentId());
                module.setProjectId(t.getProjectId());
                module.setCreateUser(operator);
                module.setPos(getImportNextOrder(apiScenarioModuleService::getNextOrder, currentScenarioModuleOrder, targetProjectId));
                module.setCreateTime(System.currentTimeMillis());
                module.setUpdateUser(operator);
                module.setUpdateTime(module.getCreateTime());
                batchApiDefinitionMapper.insertSelective(module);
            });
            sqlSession.flushStatements();
        });
    }

    private void insertScenarios(String operator, String versionId, List<ApiScenarioImportDetail> insertScenarioList, SqlSession sqlSession) {
        // 创建场景
        if (CollectionUtils.isEmpty(insertScenarioList)) {
            return;
        }

        ApiScenarioMapper apiScenarioBatchMapper = sqlSession.getMapper(ApiScenarioMapper.class);
        ApiScenarioBlobMapper apiScenarioBlobBatchMapper = sqlSession.getMapper(ApiScenarioBlobMapper.class);
        ApiScenarioCsvMapper csvBatchMapper = sqlSession.getMapper(ApiScenarioCsvMapper.class);
        ApiScenarioCsvStepMapper csvStepBatchMapper = sqlSession.getMapper(ApiScenarioCsvStepMapper.class);
        ApiScenarioStepMapper stepBatchMapper = sqlSession.getMapper(ApiScenarioStepMapper.class);
        ApiScenarioStepBlobMapper stepBlobBatchMapper = sqlSession.getMapper(ApiScenarioStepBlobMapper.class);

        Map<String, List<ApiScenarioImportDetail>> projectMap = insertScenarioList.stream().collect(Collectors.groupingBy(ApiScenarioImportDetail::getProjectId));

        projectMap.forEach((targetProjectId, targetList) -> {
            currentApiScenarioOrder.remove();
            SubListUtils.dealForSubList(targetList, 100, list -> {
                list.forEach(t -> {
                    ApiScenario scenario = new ApiScenario();
                    BeanUtils.copyBean(scenario, t);
                    scenario.setNum(apiScenarioService.getNextNum(targetProjectId));
                    scenario.setPos(getImportNextOrder(apiScenarioService::getNextOrder, currentApiScenarioOrder, targetProjectId));
                    scenario.setLatest(true);
                    scenario.setCreateUser(operator);
                    scenario.setUpdateUser(operator);
                    scenario.setCreateTime(System.currentTimeMillis());
                    scenario.setUpdateTime(System.currentTimeMillis());
                    scenario.setVersionId(versionId);
                    scenario.setRefId(scenario.getId());
                    scenario.setLastReportStatus(StringUtils.EMPTY);
                    scenario.setDeleted(false);
                    scenario.setRequestPassRate("0");
                    scenario.setStepTotal(CollectionUtils.size(t.getSteps()));
                    apiScenarioBatchMapper.insert(scenario);

                    // 更新场景配置
                    ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
                    apiScenarioBlob.setId(scenario.getId());
                    if (t.getScenarioConfig() == null) {
                        apiScenarioBlob.setConfig(JSON.toJSONString(new ScenarioConfig()).getBytes());
                    } else {
                        apiScenarioBlob.setConfig(JSON.toJSONString(t.getScenarioConfig()).getBytes());
                    }
                    apiScenarioBlobBatchMapper.insert(apiScenarioBlob);
                    // 处理csv文件
                    this.handCsvFilesAdd(t, operator, scenario, csvBatchMapper, csvStepBatchMapper);
                    // 处理添加的步骤
                    this.handleStepAdd(t, scenario, csvStepBatchMapper, stepBatchMapper, stepBlobBatchMapper);
                });
                sqlSession.flushStatements();
            });
        });


    }

    private void handleStepAdd(ApiScenarioImportDetail t, ApiScenario scenario,
                               ApiScenarioCsvStepMapper csvStepBatchMapper, ApiScenarioStepMapper stepBatchMapper, ApiScenarioStepBlobMapper stepBlobBatchMapper) {
        // 插入步骤
        if (CollectionUtils.isNotEmpty(t.getSteps())) {
            //检测循环引用
            apiScenarioService.checkCircularRef(scenario.getId(), t.getSteps());

            // 获取待添加的步骤
            List<ApiScenarioCsvStep> csvSteps = new ArrayList<>();
            List<ApiScenarioStep> steps = apiScenarioService.getApiScenarioSteps(null, t.getSteps(), csvSteps);
            steps.forEach(step -> step.setScenarioId(scenario.getId()));
            // 处理特殊的步骤详情
            apiScenarioService.addSpecialStepDetails(t.getSteps(), t.getStepDetails());
            List<ApiScenarioStepBlob> apiScenarioStepBlobs = apiScenarioService.getUpdateStepBlobs(steps, t.getStepDetails());
            apiScenarioStepBlobs.forEach(step -> step.setScenarioId(scenario.getId()));

            //保存步骤
            if (CollectionUtils.isNotEmpty(steps)) {
                stepBatchMapper.batchInsert(steps);
            }
            if (CollectionUtils.isNotEmpty(apiScenarioStepBlobs)) {
                stepBlobBatchMapper.batchInsert(apiScenarioStepBlobs);
            }
            csvSteps = apiScenarioService.filterNotExistCsv(t.getScenarioConfig(), csvSteps);
            this.saveStepCsv(scenario.getId(), csvSteps, csvStepBatchMapper);
        }
    }

    private void saveStepCsv(String scenarioId, List<ApiScenarioCsvStep> csvSteps, ApiScenarioCsvStepMapper csvStepBatchMapper) {
        // 先删除
        ApiScenarioCsvStepExample csvStepExample = new ApiScenarioCsvStepExample();
        csvStepExample.createCriteria().andScenarioIdEqualTo(scenarioId);
        csvStepBatchMapper.deleteByExample(csvStepExample);
        // 再添加
        if (CollectionUtils.isNotEmpty(csvSteps)) {
            csvStepBatchMapper.batchInsert(csvSteps);
        }
    }

    private void handCsvFilesAdd(ApiScenarioImportDetail t, String operator, ApiScenario scenario,
                                 ApiScenarioCsvMapper batchCsvMapper, ApiScenarioCsvStepMapper batchCsvStepMapper) {
        List<CsvVariable> csvVariables = apiScenarioService.getCsvVariables(t.getScenarioConfig());

        if (CollectionUtils.isEmpty(csvVariables)) {
            return;
        }
        // 处理 csv 相关数据表
        this.handleCsvDataUpdate(csvVariables, scenario, List.of(), batchCsvMapper, batchCsvStepMapper);
        // 处理文件的上传 （调用流程很长，目前没想到有好的批量处理方法。暂时直接调用Service）
        apiScenarioService.handleCsvFileAdd(csvVariables, List.of(), scenario, operator);
    }

    private void handleCsvDataUpdate(List<CsvVariable> csvVariables, ApiScenario scenario, List<String> dbCsvIds,
                                     ApiScenarioCsvMapper batchCsvMapper, ApiScenarioCsvStepMapper batchCsvStepMapper) {
        List<String> csvIds = csvVariables.stream()
                .map(CsvVariable::getId)
                .toList();

        List<String> deleteCsvIds = ListUtils.subtract(dbCsvIds, csvIds);

        //删除不存在的数据
        if (CollectionUtils.isNotEmpty(deleteCsvIds)) {
            ApiScenarioCsvExample example = new ApiScenarioCsvExample();
            example.createCriteria().andIdIn(deleteCsvIds);
            batchCsvMapper.deleteByExample(example);

            ApiScenarioCsvStepExample stepExample = new ApiScenarioCsvStepExample();
            stepExample.createCriteria().andIdIn(deleteCsvIds);
            batchCsvStepMapper.deleteByExample(stepExample);
        }

        Set<String> dbCsvIdSet = new HashSet<>(dbCsvIds);
        List<ApiScenarioCsv> addCsvList = new ArrayList<>();
        csvVariables.forEach(item -> {
            ApiScenarioCsv scenarioCsv = new ApiScenarioCsv();
            BeanUtils.copyBean(scenarioCsv, item);
            scenarioCsv.setScenarioId(scenario.getId());
            scenarioCsv.setProjectId(scenario.getProjectId());

            ApiFile file = item.getFile();
            scenarioCsv.setFileId(file.getFileId());
            scenarioCsv.setFileName(file.getFileName());
            scenarioCsv.setAssociation(BooleanUtils.isFalse(file.getLocal()));
            if (!dbCsvIdSet.contains(item.getId())) {
                addCsvList.add(scenarioCsv);
            } else {
                batchCsvMapper.updateByPrimaryKey(scenarioCsv);
            }
        });

        if (CollectionUtils.isNotEmpty(addCsvList)) {
            batchCsvMapper.batchInsert(addCsvList);
        }
    }

    private Long getImportNextOrder(Function<String, Long> subFunc, ThreadLocal<Long> nextOrder, String projectId) {
        Long order = nextOrder.get();
        if (order == null) {
            order = subFunc.apply(projectId);
        }
        order = order + DEFAULT_NODE_INTERVAL_POS;
        nextOrder.set(order);
        return order;
    }

    private ApiScenarioPreImportAnalysisResult importAnalysis(ApiScenarioImportParseResult parseResult, String operator, String projectId, String moduleId, List<BaseTreeNode> apiScenarioModules) {
        ApiScenarioPreImportAnalysisResult analysisResult = new ApiScenarioPreImportAnalysisResult();

        Map<String, String> moduleIdPathMap = apiScenarioModules.stream().collect(Collectors.toMap(BaseTreeNode::getId, BaseTreeNode::getPath));
        Map<String, BaseTreeNode> modulePathMap = apiScenarioModules.stream().collect(Collectors.toMap(BaseTreeNode::getPath, k -> k, (k1, k2) -> k1));

        ReplaceScenarioResource replaceScenarioResource = this.parseRelatedDataToAnalysisResult(operator, projectId, parseResult, analysisResult);

        List<ApiScenarioImportDetail> importScenarios = parseResult.getImportScenarioList();
        for (ApiScenarioImportDetail importScenario : importScenarios) {
            //处理模块
            if (StringUtils.isBlank(moduleId) || StringUtils.equalsIgnoreCase(moduleId, ModuleConstants.DEFAULT_NODE_ID) || !moduleIdPathMap.containsKey(moduleId)) {
                importScenario.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                importScenario.setModulePath(moduleIdPathMap.get(ModuleConstants.DEFAULT_NODE_ID));
            } else {
                if (StringUtils.isBlank(importScenario.getModulePath())) {
                    importScenario.setModulePath(moduleIdPathMap.get(moduleId));
                } else if (StringUtils.startsWith(importScenario.getModulePath(), "/")) {
                    importScenario.setModulePath(moduleIdPathMap.get(moduleId) + importScenario.getModulePath());
                } else {
                    importScenario.setModulePath(moduleIdPathMap.get(moduleId) + "/" + importScenario.getModulePath());
                }
            }
        }

        //检查重复的场景（模块+名称）
        Map<String, List<ApiScenarioImportDetail>> modulePathScenario = importScenarios.stream().collect(Collectors.groupingBy(ApiScenarioImportDetail::getModulePath));
        modulePathScenario.forEach((modulePath, scenarios) -> {
            if (!StringUtils.startsWith(modulePath, "/")) {
                modulePath = "/" + modulePath;
            }
            if (modulePathMap.containsKey(modulePath)) {
                List<ApiScenario> existenceScenarios = extApiScenarioMapper.selectBaseInfoByModuleIdAndProjectId(modulePathMap.get(modulePath).getId(), projectId);
                Map<String, String> existenceNameIdMap = existenceScenarios.stream().collect(Collectors.toMap(ApiScenario::getName, ApiScenario::getId, (k1, k2) -> k1));
                String finalModulePath = modulePath;
                scenarios.forEach(scenario -> {
                    if (existenceNameIdMap.containsKey(scenario.getName())) {
                        scenario.setId(existenceNameIdMap.get(scenario.getName()));
                        analysisResult.getUpdateApiScenarioData().add(scenario);
                    } else {
                        scenario.setId(IDGenerator.nextStr());
                        scenario.setModuleId(modulePathMap.get(finalModulePath).getId());
                        analysisResult.getInsertApiScenarioData().add(scenario);
                    }
                });
            } else {
                //模块不存在的必定是新建
                List<BaseTreeNode> insertModuleList = TreeNodeParseUtils.getInsertNodeByPath(modulePathMap, modulePath);
                insertModuleList.forEach(item -> item.setProjectId(projectId));
                analysisResult.getInsertScenarioModuleList().addAll(insertModuleList);
                String finalModulePath = modulePath;
                scenarios.forEach(scenario -> {
                    scenario.setId(IDGenerator.nextStr());
                    scenario.setModuleId(modulePathMap.get(finalModulePath).getId());
                });
                analysisResult.getInsertApiScenarioData().addAll(scenarios);
            }
        });
        //将要补的场景数据导入进来
        analysisResult.getInsertApiScenarioData().addAll(replaceScenarioResource.getInsertRelatedApiScenarioData());

        for (ApiScenarioImportDetail importScenario : analysisResult.getInsertApiScenarioData()) {
            // 处理步骤里的关联资源文件
            importScenario.getSteps().forEach(item -> {
                if (StringUtils.equalsIgnoreCase(item.getStepType(), ApiScenarioStepType.API.name())) {
                    ApiDefinitionDetail apiDetail = replaceScenarioResource.getApi(item.getResourceId());
                    if (apiDetail != null) {
                        item.setResourceId(apiDetail.getId());
                        item.setProjectId(importScenario.getProjectId());
                        item.setOriginProjectId(apiDetail.getProjectId());
                    }
                } else if (StringUtils.equalsIgnoreCase(item.getStepType(), ApiScenarioStepType.API_CASE.name())) {
                    ApiTestCaseDTO newData = replaceScenarioResource.getApiCase(item.getResourceId());
                    if (newData != null) {
                        item.setResourceId(newData.getId());
                        item.setProjectId(importScenario.getProjectId());
                        item.setOriginProjectId(newData.getProjectId());
                    }
                } else if (StringUtils.equalsIgnoreCase(item.getStepType(), ApiScenarioStepType.API_SCENARIO.name())) {
                    ApiScenarioImportDetail newData = replaceScenarioResource.getApiScenario(item.getResourceId());
                    if (newData != null) {
                        item.setResourceId(newData.getId());
                        item.setProjectId(importScenario.getProjectId());
                        item.setOriginProjectId(newData.getProjectId());
                    }
                }
            });
        }
        for (ApiScenarioImportDetail updateScenario : analysisResult.getUpdateApiScenarioData()) {
            // 处理步骤里的关联资源文件
            updateScenario.getSteps().forEach(item -> {
                if (StringUtils.equalsIgnoreCase(item.getStepType(), ApiScenarioStepType.API.name())) {
                    ApiDefinitionDetail apiDetail = replaceScenarioResource.getApi(item.getResourceId());
                    if (apiDetail != null) {
                        item.setResourceId(apiDetail.getId());
                        item.setProjectId(updateScenario.getProjectId());
                        item.setOriginProjectId(apiDetail.getProjectId());
                    }
                } else if (StringUtils.equalsIgnoreCase(item.getStepType(), ApiScenarioStepType.API_CASE.name())) {
                    ApiTestCaseDTO newData = replaceScenarioResource.getApiCase(item.getResourceId());
                    if (newData != null) {
                        item.setResourceId(newData.getId());
                        item.setProjectId(updateScenario.getProjectId());
                        item.setOriginProjectId(newData.getProjectId());
                    }
                } else if (StringUtils.equalsIgnoreCase(item.getStepType(), ApiScenarioStepType.API_SCENARIO.name())) {
                    ApiScenarioImportDetail newData = replaceScenarioResource.getApiScenario(item.getResourceId());
                    if (newData != null) {
                        item.setResourceId(newData.getId());
                        item.setProjectId(updateScenario.getProjectId());
                        item.setOriginProjectId(newData.getProjectId());
                    }
                }
            });
        }
        return analysisResult;
    }

    private ReplaceScenarioResource parseRelatedDataToAnalysisResult(String operator, String projectId, ApiScenarioImportParseResult parseResult, ApiScenarioPreImportAnalysisResult analysisResult) {
        /*
        1.判断接口定义中要创建的部分，然后进行新旧ID映射
        2.判断用例中的接口定义是否在上一步中创建，如果没有则进行筛选判断符合条件的接口定义（若没有则新建）并关联到其下面。
         */
        Map<String, List<ApiDefinitionDetail>> projectApiMap = new HashMap<>();
        Map<String, List<ApiTestCaseDTO>> projectApiCaseMap = new HashMap<>();
        Map<String, List<ApiScenarioImportDetail>> projectScenarioMap = new HashMap<>();
        // 对导入文件中的数据进行分组
        {
            for (Map.Entry<String, List<ApiDefinitionDetail>> entry : parseResult.getRelatedApiDefinitions().stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getProjectId)).entrySet()) {
                String targetProjectId = entry.getKey();
                List<ApiDefinitionDetail> apiDefinitionDetails = entry.getValue();
                //  如果没有权限，需要在当前项目进行校验
                if (projectMapper.selectByPrimaryKey(targetProjectId) == null) {
                    targetProjectId = projectId;
                } else if (!permissionCheckService.userHasProjectPermission(operator, targetProjectId, PermissionConstants.PROJECT_API_DEFINITION_ADD)) {
                    targetProjectId = projectId;
                }
                if (projectApiMap.containsKey(targetProjectId)) {
                    projectApiMap.get(targetProjectId).addAll(apiDefinitionDetails);
                } else {
                    projectApiMap.put(targetProjectId, apiDefinitionDetails);
                }
            }
            for (Map.Entry<String, List<ApiTestCaseDTO>> entry : parseResult.getRelatedApiTestCaseList().stream().collect(Collectors.groupingBy(ApiTestCaseDTO::getProjectId)).entrySet()) {
                String targetProjectId = entry.getKey();
                List<ApiTestCaseDTO> apiTestCaseList = entry.getValue();
                //  如果没有权限，需要在当前项目进行校验
                if (projectMapper.selectByPrimaryKey(targetProjectId) == null) {
                    targetProjectId = projectId;
                } else if (!permissionCheckService.userHasProjectPermission(operator, targetProjectId, PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD)) {
                    targetProjectId = projectId;
                }
                if (projectApiCaseMap.containsKey(targetProjectId)) {
                    projectApiCaseMap.get(targetProjectId).addAll(apiTestCaseList);
                } else {
                    projectApiCaseMap.put(targetProjectId, apiTestCaseList);
                }
            }
            for (Map.Entry<String, List<ApiScenarioImportDetail>> entry : parseResult.getRelatedScenarioList().stream().collect(Collectors.groupingBy(ApiScenarioImportDetail::getProjectId)).entrySet()) {
                String targetProjectId = entry.getKey();
                List<ApiScenarioImportDetail> scenarioList = entry.getValue();
                //  如果没有权限，需要在当前项目进行校验
                if (projectMapper.selectByPrimaryKey(targetProjectId) == null) {
                    targetProjectId = projectId;
                } else if (!permissionCheckService.userHasProjectPermission(operator, targetProjectId, PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD)) {
                    targetProjectId = projectId;
                }
                if (projectScenarioMap.containsKey(targetProjectId)) {
                    projectScenarioMap.get(targetProjectId).addAll(scenarioList);
                } else {
                    projectScenarioMap.put(targetProjectId, scenarioList);
                }
            }
        }

        ReplaceScenarioResource returnResource = new ReplaceScenarioResource();
        // 1.处理接口定义
        {
            for (Map.Entry<String, List<ApiDefinitionDetail>> entry : projectApiMap.entrySet()) {
                String targetProjectId = entry.getKey();
                List<ApiDefinitionDetail> apiDefinitionDetails = entry.getValue();

                Map<String, List<ApiDefinitionDetail>> protocolImportMap = apiDefinitionDetails.stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getProtocol));

                ApiDefinitionPageRequest pageRequest = new ApiDefinitionPageRequest();
                pageRequest.setProjectId(targetProjectId);
                pageRequest.setProtocols(new ArrayList<>(protocolImportMap.keySet()));
                List<ApiDefinitionDetail> existenceApiDefinitionList = extApiDefinitionMapper.importList(pageRequest);

                Map<String, List<ApiDefinitionDetail>> existenceProtocolMap = existenceApiDefinitionList.stream().collect(Collectors.groupingBy(ApiDefinitionDetail::getProtocol));

                for (Map.Entry<String, List<ApiDefinitionDetail>> protocolEntry : protocolImportMap.entrySet()) {
                    returnResource.getApiDefinitionIdMap().putAll(ApiScenarioImportUtils.getApiIdInTargetList(
                            protocolEntry.getValue(), existenceProtocolMap.get(protocolEntry.getKey()), protocolEntry.getKey(), projectId, analysisResult));
                }

                // 解析接口定义的模块
                List<BaseTreeNode> apiModules = apiDefinitionImportService.buildTreeData(targetProjectId, null);
                Map<String, BaseTreeNode> modulePathMap = apiModules.stream().collect(Collectors.toMap(BaseTreeNode::getPath, k -> k, (k1, k2) -> k1));
                for (ApiDefinitionDetail apiDefinitionDetail : analysisResult.getInsertApiDefinitions()) {
                    List<BaseTreeNode> insertModuleList = TreeNodeParseUtils.getInsertNodeByPath(modulePathMap, apiDefinitionDetail.getModulePath());
                    insertModuleList.forEach(item -> item.setProjectId(targetProjectId));
                    analysisResult.getInsertApiModuleList().addAll(insertModuleList);
                }
            }
        }
        // 2.处理接口用例
        {
            for (Map.Entry<String, List<ApiTestCaseDTO>> entry : projectApiCaseMap.entrySet()) {
                String targetProjectId = entry.getKey();
                List<ApiTestCaseDTO> apiTestCaseDTOS = entry.getValue();
                Map<String, List<ApiTestCaseDTO>> protocolMap = apiTestCaseDTOS.stream().collect(Collectors.groupingBy(ApiTestCaseDTO::getProtocol));
                //将项目下的用例，通过协议、关键词进行分组处理。

                ApiDefinitionPageRequest pageRequest = new ApiDefinitionPageRequest();
                pageRequest.setProjectId(targetProjectId);
                pageRequest.setProtocols(new ArrayList<>(protocolMap.keySet()));
                List<ApiDefinitionDetail> existenceApiDefinitionList = extApiDefinitionMapper.importList(pageRequest);

                for (Map.Entry<String, List<ApiTestCaseDTO>> protocolEntry : protocolMap.entrySet()) {
                    String protocol = protocolEntry.getKey();
                    List<ApiTestCaseDTO> protocolList = protocolEntry.getValue();

                    Map<String, List<ApiTestCaseDTO>> apiIdMap = protocolList.stream().collect(Collectors.groupingBy(ApiTestCaseDTO::getApiDefinitionId));
                    for (Map.Entry<String, List<ApiTestCaseDTO>> apiIdEntry : apiIdMap.entrySet()) {
                        String replaceApiDefinitionId = returnResource.getApi(apiIdEntry.getKey()) == null ? StringUtils.EMPTY : returnResource.getApi(apiIdEntry.getKey()).getId();

                        List<ApiTestCaseDTO> testCaseList = apiIdEntry.getValue();

                        if (StringUtils.isBlank(replaceApiDefinitionId)) {
                            // 用例对应的接口在上述步骤中未处理
                            boolean apiExistence = ApiScenarioImportUtils.isApiExistence(
                                    protocol, testCaseList.getFirst().getMethod(), testCaseList.getFirst().getPath(),
                                    testCaseList.getFirst().getModulePath(), testCaseList.getFirst().getApiDefinitionName(), existenceApiDefinitionList);

                            if (apiExistence) {
                                Map<Long, ApiTestCaseDTO> existenceApiCaseNumMap = extApiTestCaseMapper.selectBaseInfoByProjectIdAndApiId(targetProjectId, apiIdEntry.getKey())
                                        .stream().collect(Collectors.toMap(ApiTestCaseDTO::getNum, Function.identity(), (k1, k2) -> k1));
                                for (ApiTestCaseDTO apiTestCaseDTO : apiIdEntry.getValue()) {
                                    if (existenceApiCaseNumMap.containsKey(apiTestCaseDTO.getNum())) {
                                        returnResource.putApiTestCase(apiTestCaseDTO.getId(), existenceApiCaseNumMap.get(apiTestCaseDTO.getNum()));
                                    } else {
                                        apiTestCaseDTO.setId(IDGenerator.nextStr());
                                        apiTestCaseDTO.setProjectId(targetProjectId);
                                        returnResource.putApiTestCase(apiTestCaseDTO.getId(), apiTestCaseDTO);
                                        analysisResult.setApiTestCase(apiTestCaseDTO);
                                    }
                                }
                            } else {
                                // 同步创建API
                                ApiDefinitionDetail apiDefinitionDetail = new ApiDefinitionDetail();
                                apiDefinitionDetail.setProjectId(targetProjectId);
                                apiDefinitionDetail.setModulePath(testCaseList.getFirst().getModulePath());
                                apiDefinitionDetail.setId(IDGenerator.nextStr());
                                apiDefinitionDetail.setName(testCaseList.getFirst().getApiDefinitionName());
                                apiDefinitionDetail.setRequest(testCaseList.getFirst().getRequest());
                                apiDefinitionDetail.setProtocol(testCaseList.getFirst().getProtocol());
                                apiDefinitionDetail.setPath(testCaseList.getFirst().getPath());
                                apiDefinitionDetail.setMethod(testCaseList.getFirst().getMethod());
                                apiDefinitionDetail.setResponse(new ArrayList<>());
                                returnResource.putApiDefinitionId(apiIdEntry.getKey(), apiDefinitionDetail);
                                analysisResult.setApiDefinition(apiDefinitionDetail);

                                for (ApiTestCaseDTO apiTestCaseDTO : testCaseList) {
                                    apiTestCaseDTO.setId(IDGenerator.nextStr());
                                    apiTestCaseDTO.setProjectId(targetProjectId);
                                    apiTestCaseDTO.setApiDefinitionId(apiDefinitionDetail.getId());
                                    returnResource.putApiTestCase(apiTestCaseDTO.getId(), apiTestCaseDTO);
                                    analysisResult.setApiTestCase(apiTestCaseDTO);
                                }
                            }

                        } else {
                            Map<Long, ApiTestCaseDTO> existenceApiCaseNumMap = extApiTestCaseMapper.selectBaseInfoByProjectIdAndApiId(targetProjectId, replaceApiDefinitionId)
                                    .stream().collect(Collectors.toMap(ApiTestCaseDTO::getNum, Function.identity(), (k1, k2) -> k1));
                            for (ApiTestCaseDTO apiTestCaseDTO : testCaseList) {
                                apiTestCaseDTO.setApiDefinitionId(replaceApiDefinitionId);
                                if (existenceApiCaseNumMap.containsKey(apiTestCaseDTO.getNum())) {
                                    returnResource.putApiTestCase(apiTestCaseDTO.getId(), existenceApiCaseNumMap.get(apiTestCaseDTO.getNum()));
                                } else {
                                    apiTestCaseDTO.setProjectId(targetProjectId);
                                    returnResource.putApiTestCase(apiTestCaseDTO.getId(), apiTestCaseDTO);
                                    apiTestCaseDTO.setId(IDGenerator.nextStr());
                                    analysisResult.setApiTestCase(apiTestCaseDTO);
                                }
                            }
                        }
                    }
                }
            }
        }
        // 3. 处理场景
        {
            for (Map.Entry<String, List<ApiScenarioImportDetail>> entry : projectScenarioMap.entrySet()) {
                String targetProjectId = entry.getKey();
                List<BaseTreeNode> apiScenarioModules = apiScenarioModuleService.getTree(targetProjectId);

                Map<String, String> moduleIdPathMap = apiScenarioModules.stream().collect(Collectors.toMap(BaseTreeNode::getId, BaseTreeNode::getPath));
                Map<String, BaseTreeNode> modulePathMap = apiScenarioModules.stream().collect(Collectors.toMap(BaseTreeNode::getPath, k -> k, (k1, k2) -> k1));

                List<ApiScenarioImportDetail> importScenarios = parseResult.getRelatedScenarioList();

                for (ApiScenarioImportDetail importScenario : importScenarios) {
                    importScenario.setProjectId(targetProjectId);
                    if (StringUtils.isBlank(importScenario.getModulePath()) || StringUtils.equals(importScenario.getModulePath().trim(), "/")) {
                        importScenario.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                        importScenario.setModulePath(moduleIdPathMap.get(ModuleConstants.DEFAULT_NODE_ID));
                    } else {
                        if (!StringUtils.startsWith(importScenario.getModulePath(), "/")) {
                            importScenario.setModulePath("/" + importScenario.getModulePath());
                        }
                        if (StringUtils.endsWith(importScenario.getModulePath(), "/")) {
                            importScenario.setModulePath(importScenario.getModulePath().substring(0, importScenario.getModulePath().length() - 1));
                        }
                    }
                }

                Map<String, List<ApiScenarioImportDetail>> modulePathScenario = importScenarios.stream().collect(Collectors.groupingBy(ApiScenarioImportDetail::getModulePath));
                for (Map.Entry<String, List<ApiScenarioImportDetail>> modulePathEntry : modulePathScenario.entrySet()) {
                    String modulePath = modulePathEntry.getKey();

                    Map<String, ApiScenario> apiScenarioNameMap = new HashMap<>();
                    String moduleId = null;
                    // 设置moduleId
                    if (!modulePathMap.containsKey(modulePath)) {
                        List<BaseTreeNode> insertModuleList = TreeNodeParseUtils.getInsertNodeByPath(modulePathMap, modulePath);
                        insertModuleList.forEach(item -> item.setProjectId(targetProjectId));
                        analysisResult.getInsertScenarioModuleList().addAll(insertModuleList);
                        moduleId = modulePathMap.get(modulePath).getId();
                    } else {
                        moduleId = modulePathMap.get(modulePath).getId();
                        apiScenarioNameMap = extApiScenarioMapper.selectBaseInfoByModuleIdAndProjectId(moduleId, targetProjectId)
                                .stream().collect(Collectors.toMap(ApiScenario::getName, Function.identity(), (k1, k2) -> k1));
                    }

                    Map<String, ApiScenarioImportDetail> createdNameOldIds = new HashMap<>();

                    for (ApiScenarioImportDetail scenario : modulePathEntry.getValue()) {
                        scenario.setModuleId(moduleId);
                        if (createdNameOldIds.containsKey(scenario.getName())) {
                            returnResource.putApiScenarioId(scenario.getId(), createdNameOldIds.get(scenario.getName()));
                        } else {
                            if (apiScenarioNameMap.containsKey(scenario.getName())) {
                                ApiScenarioImportDetail oldData = new ApiScenarioImportDetail();
                                BeanUtils.copyBean(oldData, apiScenarioNameMap.get(scenario.getName()));
                                returnResource.putApiScenarioId(scenario.getId(), oldData);
                                createdNameOldIds.put(scenario.getName(), oldData);
                            } else {
                                String oldId = scenario.getId();
                                scenario.setId(IDGenerator.nextStr());
                                scenario.setProjectId(targetProjectId);
                                returnResource.getInsertRelatedApiScenarioData().add(scenario);
                                returnResource.putApiScenarioId(oldId, scenario);
                            }
                        }
                    }
                }

            }
        }
        return returnResource;
    }

    private String exportApiScenarioZip(ApiScenarioBatchExportRequest request, String exportType, String userId) throws Exception {
        File tmpDir = new File(LocalRepositoryDir.getSystemTempDir() + File.separatorChar + EXPORT_CASE_TMP_DIR + "_" + IDGenerator.nextStr());
        if (!tmpDir.mkdirs()) {
            throw new MSException(Translator.get("upload_fail"));
        }
        String fileType = "zip";
        try {
            User user = userMapper.selectByPrimaryKey(userId);
            noticeSendService.setLanguage(user.getLanguage());
            //获取导出的ids集合
            List<String> ids = apiScenarioService.doSelectIds(request, false);
            if (CollectionUtils.isEmpty(ids)) {
                return null;
            }
            Map<String, String> moduleMap = this.apiScenarioModuleService.getTree(request.getProjectId()).stream().collect(Collectors.toMap(BaseTreeNode::getId, BaseTreeNode::getPath));

            String fileFolder = tmpDir.getPath() + File.separatorChar + request.getFileId();
            int fileIndex = 1;
            SubListUtils.dealForSubList(ids, 500, subList -> {
                request.setSelectIds(subList);
                ApiScenarioExportResponse exportResponse = this.genMetersphereExportResponse(request, moduleMap, exportType, userId);
                TempFileUtils.writeExportFile(fileFolder + File.separatorChar + "scenario_export_" + fileIndex + ".ms", exportResponse);
            });
            File zipFile = MsFileUtils.zipFile(tmpDir.getPath(), request.getFileId());
            if (zipFile == null) {
                return null;
            }
            fileService.upload(zipFile, new FileRequest(DefaultRepositoryDir.getExportApiTempDir(), StorageType.MINIO.name(), request.getFileId()));

            // 生成日志
            LogDTO logDTO = apiScenarioLogService.exportExcelLog(request.getFileId(), exportType, userId, projectMapper.selectByPrimaryKey(request.getProjectId()));
            operationLogService.add(logDTO);

            String taskId = exportTaskManager.getExportTaskId(request.getProjectId(), ExportConstants.ExportType.API_SCENARIO.name(), ExportConstants.ExportState.PREPARED.toString(), userId, null, fileType);
            ExportWebSocketHandler.sendMessageSingle(
                    new ExportMsgDTO(request.getFileId(), taskId, ids.size(), true, MsgType.EXEC_RESULT.name())
            );
            return taskId;
        } catch (Exception e) {
            LogUtils.error(e);
            List<ExportTask> exportTasks = exportTaskManager.getExportTasks(request.getProjectId(), ExportConstants.ExportType.API_SCENARIO.name(), ExportConstants.ExportState.PREPARED.toString(), userId, null);
            if (CollectionUtils.isNotEmpty(exportTasks)) {
                exportTaskManager.updateExportTask(ExportConstants.ExportState.ERROR.name(), exportTasks.getFirst().getId(), fileType);
            }
            ExportMsgDTO exportMsgDTO = new ExportMsgDTO(request.getFileId(), "", 0, false, MsgType.EXEC_RESULT.name());
            ExportWebSocketHandler.sendMessageSingle(exportMsgDTO);
            throw new MSException(e);
        } finally {
            MsFileUtils.deleteDir(tmpDir.getPath());
        }
    }

    private ApiScenarioExportResponse genMetersphereExportResponse(ApiScenarioBatchExportRequest request, Map<String, String> moduleMap, String exportType, String userId) {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        MetersphereApiScenarioExportResponse response = apiScenarioService.selectAndSortScenarioDetailWithIds(request.getSelectIds(), moduleMap);
        response.setProjectId(project.getId());
        response.setOrganizationId(project.getOrganizationId());

        if (StringUtils.equalsIgnoreCase(ApiScenarioExportType.METERSPHERE_ALL_DATA.name(), exportType)) {
            // 全量导出，导出引用的api、apiCase
            List<String> apiDefinitionIdList = new ArrayList<>();
            List<String> apiCaseIdList = new ArrayList<>();
            response.getScenarioStepList().forEach(step -> {
                if (StringUtils.isNotBlank(step.getResourceId())) {
                    if (StringUtils.equalsIgnoreCase(step.getStepType(), ApiScenarioStepType.API.name())) {
                        if (!apiDefinitionIdList.contains(step.getResourceId())) {
                            apiDefinitionIdList.add(step.getResourceId());
                        }
                    } else if (StringUtils.equalsIgnoreCase(step.getStepType(), ApiScenarioStepType.API_CASE.name())) {
                        if (!apiCaseIdList.contains(step.getResourceId())) {
                            apiCaseIdList.add(step.getResourceId());
                        }
                    }
                }
            });
            List<ApiTestCaseWithBlob> apiTestCaseWithBlobs = null;
            if (CollectionUtils.isNotEmpty(apiCaseIdList)) {
                apiTestCaseWithBlobs = extApiTestCaseMapper.selectAllDetailByIds(apiCaseIdList);
                if (CollectionUtils.isNotEmpty(apiCaseIdList)) {
                    apiTestCaseWithBlobs.forEach(item -> {
                        if (!apiDefinitionIdList.contains(item.getApiDefinitionId())) {
                            apiDefinitionIdList.add(item.getApiDefinitionId());
                        }
                    });
                }
            }


            Map<String, Map<String, String>> projectApiModuleIdMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(apiDefinitionIdList)) {
                List<ApiDefinitionWithBlob> apiList = extApiDefinitionMapper.selectApiDefinitionWithBlob(apiDefinitionIdList);
                List<String> projectIdList = apiList.stream().map(ApiDefinitionWithBlob::getProjectId).distinct().toList();
                projectIdList.forEach(projectId -> projectApiModuleIdMap.put(projectId, apiDefinitionExportService.buildModuleIdPathMap(request.getProjectId())));
                for (ApiDefinitionWithBlob blob : apiList) {
                    ApiDefinitionExportDetail detail = new ApiDefinitionExportDetail();
                    BeanUtils.copyBean(detail, blob);
                    if (blob.getRequest() != null) {
                        detail.setRequest(ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class));
                    }
                    if (blob.getResponse() != null) {
                        detail.setResponse(ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class));
                    }
                    String moduleName;
                    if (StringUtils.equals(blob.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
                        moduleName = Translator.get("api_unplanned_request");
                    } else if (projectApiModuleIdMap.containsKey(detail.getProjectId()) && projectApiModuleIdMap.get(detail.getProjectId()).containsKey(blob.getModuleId())) {
                        moduleName = projectApiModuleIdMap.get(detail.getProjectId()).get(blob.getModuleId());
                    } else {
                        detail.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                        moduleName = Translator.get("api_unplanned_request");
                    }
                    detail.setModulePath(moduleName);
                    response.addExportApi(detail);

                }
            }
            if (CollectionUtils.isNotEmpty(apiTestCaseWithBlobs)) {
                for (ApiTestCaseWithBlob apiTestCaseWithBlob : apiTestCaseWithBlobs) {
                    String projectId = apiTestCaseWithBlob.getProjectId();
                    if (!projectApiModuleIdMap.containsKey(projectId)) {
                        projectApiModuleIdMap.put(projectId, apiDefinitionExportService.buildModuleIdPathMap(projectId));
                    }
                    String moduleId = apiTestCaseWithBlob.getModuleId();

                    ApiTestCaseDTO dto = new ApiTestCaseDTO();
                    BeanUtils.copyBean(dto, apiTestCaseWithBlob);
                    dto.setTags(apiTestCaseWithBlob.getTags());
                    dto.setRequest(ApiDataUtils.parseObject(new String(apiTestCaseWithBlob.getRequest()), AbstractMsTestElement.class));

                    String moduleName;
                    if (StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
                        moduleName = Translator.get("api_unplanned_request");
                    } else if (projectApiModuleIdMap.containsKey(projectId) && projectApiModuleIdMap.get(projectId).containsKey(moduleId)) {
                        moduleName = projectApiModuleIdMap.get(projectId).get(moduleId);
                    } else {
                        dto.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                        moduleName = Translator.get("api_unplanned_request");
                    }
                    dto.setModulePath(moduleName);
                    response.addExportApiCase(dto);
                }
            }

        } else {
            // 普通导出,所有的引用都改为复制，并且Api、ApiCase改为CUSTOM_REQUEST
            response.setRefTypeToCopy();
            response.setStepTypeToCustomRequest();
        }
        return response;
    }

    public void stopExport(String taskId, String userId) {
        exportTaskManager.sendStopMessage(taskId, userId);
    }

    public void downloadFile(String projectId, String fileId, String userId, HttpServletResponse httpServletResponse) {
        List<ExportTask> exportTasks = exportTaskManager.getExportTasks(projectId, ExportConstants.ExportType.API_SCENARIO.name(), ExportConstants.ExportState.SUCCESS.toString(), userId, fileId);
        if (CollectionUtils.isEmpty(exportTasks)) {
            return;
        }
        ExportTask tasksFirst = exportTasks.getFirst();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(tasksFirst.getFileId());
        fileRequest.setFolder(DefaultRepositoryDir.getExportApiTempDir());
        fileRequest.setStorage(StorageType.MINIO.name());
        String fileName = "Metersphere_scenario_" + project.getName() + "." + tasksFirst.getFileType();
        try {
            InputStream fileInputStream = fileService.getFileAsStream(fileRequest);
            FileDownloadUtils.zipFilesWithResponse(fileName, fileInputStream, httpServletResponse);
        } catch (Exception e) {
            throw new MSException("get file error");
        }
    }
}

@Data
class ReplaceScenarioResource {
    private Map<String, ApiDefinitionDetail> apiDefinitionIdMap = new HashMap<>();
    private Map<String, ApiTestCaseDTO> apiTestCaseIdMap = new HashMap<>();
    private Map<String, ApiScenarioImportDetail> apiScenarioIdMap = new HashMap<>();

    @Schema(description = "要新增的关联场景")
    List<ApiScenarioImportDetail> insertRelatedApiScenarioData = new ArrayList<>();

    public ApiDefinitionDetail getApi(String apiOldId) {
        return apiDefinitionIdMap.get(apiOldId);
    }

    public ApiTestCaseDTO getApiCase(String oldId) {
        return apiTestCaseIdMap.get(oldId);
    }

    public ApiScenarioImportDetail getApiScenario(String oldId) {
        return apiScenarioIdMap.get(oldId);
    }


    public void putApiDefinitionId(String oldId, ApiDefinitionDetail newData) {
        apiDefinitionIdMap.put(oldId, newData);
    }

    public void putApiTestCase(String oldId, ApiTestCaseDTO newData) {
        apiTestCaseIdMap.put(oldId, newData);
    }

    public void putApiScenarioId(String oldId, ApiScenarioImportDetail newData) {
        apiScenarioIdMap.put(oldId, newData);
    }
}
