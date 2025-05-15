package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiExecResultDTO;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.ReferenceDTO;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.ExecutePageRequest;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.export.MetersphereApiScenarioExportResponse;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.step.StepParser;
import io.metersphere.api.parser.step.StepParserFactory;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiDefinitionUtils;
import io.metersphere.api.utils.ApiScenarioBatchOperationUtils;
import io.metersphere.api.utils.ApiScenarioUtils;
import io.metersphere.functional.domain.FunctionalCaseTestExample;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;
import io.metersphere.plugin.api.spi.AbstractMsProtocolTestElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.MoveNodeService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.EnvironmentGroupExample;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.ScheduleConfig;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ScheduleUtils;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.metersphere.api.controller.result.ApiResultCode.API_SCENARIO_CIRCULAR_REFERENCE;
import static io.metersphere.api.controller.result.ApiResultCode.API_SCENARIO_EXIST;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioService extends MoveNodeService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ApiScenarioLogService apiScenarioLogService;
    @Resource
    private ApiScenarioFollowerMapper apiScenarioFollowerMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiScenarioStepMapper apiScenarioStepMapper;
    @Resource
    private ExtApiScenarioStepMapper extApiScenarioStepMapper;
    @Resource
    private ExtApiScenarioStepBlobMapper extApiScenarioStepBlobMapper;
    @Resource
    private ApiScenarioStepBlobMapper apiScenarioStepBlobMapper;
    @Resource
    private ApiScenarioBlobMapper apiScenarioBlobMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiScenarioCsvMapper apiScenarioCsvMapper;
    @Resource
    private ApiScenarioCsvStepMapper apiScenarioCsvStepMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private OperationHistoryService operationHistoryService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiScenarioNoticeService apiScenarioNoticeService;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;
    @Resource
    private ApiScenarioFileService apiScenarioFileService;

    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String TAGS = "Tags";
    public static final String ENVIRONMENT = "Environment";
    private static final String SCENARIO_TABLE = "api_scenario";
    private static final String SCENARIO = "SCENARIO";


    public List<ApiScenarioDTO> getScenarioPage(ApiScenarioPageRequest request, boolean isRepeat, String testPlanId) {
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request, isRepeat, testPlanId);
        if (CollectionUtils.isNotEmpty(list)) {
            processApiScenario(list);
        }
        return list;
    }

    private void processApiScenario(List<ApiScenarioDTO> scenarioLists) {
        Set<String> userIds = extractUserIds(scenarioLists);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        List<String> envIds = scenarioLists.stream().map(ApiScenarioDTO::getEnvironmentId).toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(envIds);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        Map<String, String> envMap = environments.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));
        EnvironmentGroupExample groupExample = new EnvironmentGroupExample();
        groupExample.createCriteria().andIdIn(envIds);
        List<EnvironmentGroup> environmentGroups = environmentGroupMapper.selectByExample(groupExample);
        Map<String, String> groupMap = environmentGroups.stream().collect(Collectors.toMap(EnvironmentGroup::getId, EnvironmentGroup::getName));
        //取模块id为新的set
        List<String> moduleIds = scenarioLists.stream().map(ApiScenarioDTO::getModuleId).distinct().toList();
        ApiScenarioModuleExample moduleExample = new ApiScenarioModuleExample();
        moduleExample.createCriteria().andIdIn(moduleIds);
        List<ApiScenarioModule> modules = apiScenarioModuleMapper.selectByExample(moduleExample);
        //生成map key为id value为name
        Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(ApiScenarioModule::getId, ApiScenarioModule::getName));
        //查询定时任务
        List<String> scenarioIds = scenarioLists.stream().map(ApiScenarioDTO::getId).toList();
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdIn(scenarioIds).andResourceTypeEqualTo(ScheduleResourceType.API_SCENARIO.name());
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        Map<String, Schedule> scheduleMap = schedules.stream().collect(Collectors.toMap(Schedule::getResourceId, t -> t));
        //获取所有的lastResultId
        List<String> lastResultIds = scenarioLists.stream().map(ApiScenarioDTO::getLastReportId).toList();
        List<ApiScenarioReport> reports = apiScenarioReportService.getApiScenarioReportByIds(lastResultIds);
        // 生成map key是id value是ScriptIdentifier  但是getScriptIdentifier为空的不放入
        Map<String, String> reportMap = reports.stream().filter(report -> StringUtils.isNotBlank(report.getScriptIdentifier())).collect(Collectors.toMap(ApiScenarioReport::getId, ApiScenarioReport::getScriptIdentifier));
        scenarioLists.forEach(item -> {
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));
            item.setModulePath(StringUtils.isNotBlank(moduleMap.get(item.getModuleId())) ? moduleMap.get(item.getModuleId()) : Translator.get("api_unplanned_scenario"));
            if (!item.getGrouped() && envMap.containsKey(item.getEnvironmentId())) {
                item.setEnvironmentName(envMap.get(item.getEnvironmentId()));
            } else if (item.getGrouped() && groupMap.containsKey(item.getEnvironmentId())) {
                item.setEnvironmentName(groupMap.get(item.getEnvironmentId()));
            }
            if (MapUtils.isNotEmpty(scheduleMap) && scheduleMap.containsKey(item.getId())) {
                Schedule schedule = scheduleMap.get(item.getId());
                ApiScenarioScheduleConfigRequest request = new ApiScenarioScheduleConfigRequest();
                request.setEnable(schedule.getEnable());
                request.setCron(schedule.getValue());
                request.setScenarioId(item.getId());
                if (schedule.getConfig() != null) {
                    request.setConfig(JSON.parseObject(schedule.getConfig(), ApiRunModeConfigDTO.class));
                }
                item.setScheduleConfig(request);
                if (schedule.getEnable()) {
                    item.setNextTriggerTime(ScheduleUtils.getNextTriggerTime(schedule.getValue()));
                }
            }
            if (MapUtils.isNotEmpty(reportMap) && reportMap.containsKey(item.getLastReportId())) {
                item.setScriptIdentifier(reportMap.get(item.getLastReportId()));
            }
        });
    }

    private Set<String> extractUserIds(List<ApiScenarioDTO> list) {
        return list.stream()
                .flatMap(apiScenario -> Stream.of(apiScenario.getUpdateUser(), apiScenario.getDeleteUser(), apiScenario.getCreateUser()))
                .collect(Collectors.toSet());
    }

    public void batchEdit(ApiScenarioBatchEditRequest request, String userId) {
        List<String> ids = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        SubListUtils.dealForSubList(ids, 500, subList -> batchEditByType(request, subList, userId, request.getProjectId()));
    }

    private void batchEditByType(ApiScenarioBatchEditRequest request, List<String> ids, String userId, String projectId) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        ApiScenario updateScenario = new ApiScenario();
        updateScenario.setUpdateUser(userId);
        updateScenario.setUpdateTime(System.currentTimeMillis());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper mapper = sqlSession.getMapper(ApiScenarioMapper.class);

        switch (request.getType()) {
            case PRIORITY -> batchUpdatePriority(example, updateScenario, request.getPriority(), mapper);
            case STATUS -> batchUpdateStatus(example, updateScenario, request.getStatus(), mapper);
            case TAGS -> batchUpdateTags(example, updateScenario, request, ids, mapper);
            case ENVIRONMENT -> batchUpdateEnvironment(example, updateScenario, request, mapper);
            default -> throw new MSException(Translator.get("batch_edit_type_error"));
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        List<ApiScenario> scenarioInfoByIds = extApiScenarioMapper.getInfoByIds(ids, false);
        apiScenarioLogService.batchEditLog(scenarioInfoByIds, userId, projectId);
        apiScenarioNoticeService.batchSendNotice(ids, userId, projectId, NoticeConstants.Event.UPDATE);
    }

    private void batchUpdateEnvironment(ApiScenarioExample example, ApiScenario updateScenario,
                                        ApiScenarioBatchEditRequest request, ApiScenarioMapper mapper) {
        if (BooleanUtils.isFalse(request.isGrouped())) {
            if (StringUtils.isBlank(request.getEnvId())) {
                throw new MSException(Translator.get("environment_id_is_null"));
            }
            Environment environment = environmentMapper.selectByPrimaryKey(request.getEnvId());
            if (environment == null) {
                throw new MSException(Translator.get("environment_is_not_exist"));
            }
            updateScenario.setGrouped(false);
            updateScenario.setEnvironmentId(request.getEnvId());
        } else {
            if (StringUtils.isBlank(request.getGroupId())) {
                throw new MSException(Translator.get("environment_group_id_is_null"));
            }
            EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(request.getGroupId());
            if (environmentGroup == null) {
                throw new MSException(Translator.get("environment_group_is_not_exist"));
            }
            updateScenario.setGrouped(true);
            updateScenario.setEnvironmentId(request.getGroupId());
        }
        mapper.updateByExampleSelective(updateScenario, example);

    }

    private void batchUpdateTags(ApiScenarioExample example, ApiScenario updateScenario,
                                 ApiScenarioBatchEditRequest request, List<String> ids,
                                 ApiScenarioMapper mapper) {
        if (request.isAppend()) {
            Map<String, ApiScenario> scenarioMap = extApiScenarioMapper.getTagsByIds(ids, false)
                    .stream()
                    .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));
            if (MapUtils.isNotEmpty(scenarioMap)) {
                scenarioMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v.getTags())) {
                        List<String> orgTags = v.getTags();
                        orgTags.addAll(request.getTags());
                        v.setTags(ServiceUtils.parseTags(orgTags.stream().distinct().toList()));
                    } else {
                        v.setTags(request.getTags());
                    }
                    v.setUpdateTime(updateScenario.getUpdateTime());
                    v.setUpdateUser(updateScenario.getUpdateUser());
                    mapper.updateByPrimaryKeySelective(v);
                });
            }
        } else {
            updateScenario.setTags(request.isClear() ? new ArrayList<>() : ServiceUtils.parseTags(request.getTags().stream().distinct().toList()));
            mapper.updateByExampleSelective(updateScenario, example);
        }
    }

    private void batchUpdateStatus(ApiScenarioExample example, ApiScenario updateScenario, String status, ApiScenarioMapper mapper) {
        if (StringUtils.isBlank(status)) {
            throw new MSException(Translator.get("status_is_null"));
        }
        updateScenario.setStatus(status);
        mapper.updateByExampleSelective(updateScenario, example);
    }

    private void batchUpdatePriority(ApiScenarioExample example, ApiScenario updateScenario, String priority, ApiScenarioMapper mapper) {
        if (StringUtils.isBlank(priority)) {
            throw new MSException(Translator.get("priority_is_null"));
        }
        updateScenario.setPriority(priority);
        mapper.updateByExampleSelective(updateScenario, example);
    }

    public List<String> doSelectIds(ApiScenarioBatchRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> ids = extApiScenarioMapper.getIds(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return new ArrayList<>(ids.stream().distinct().toList());
        } else {
            return request.getSelectIds();
        }
    }

    public void follow(String id, String userId) {
        checkResourceExist(id);
        ApiScenarioFollowerExample example = new ApiScenarioFollowerExample();
        example.createCriteria().andApiScenarioIdEqualTo(id).andUserIdEqualTo(userId);
        if (apiScenarioFollowerMapper.countByExample(example) > 0) {
            apiScenarioFollowerMapper.deleteByPrimaryKey(id, userId);
            apiScenarioLogService.unfollowLog(id, userId);
        } else {
            ApiScenarioFollower apiScenarioFollower = new ApiScenarioFollower();
            apiScenarioFollower.setApiScenarioId(id);
            apiScenarioFollower.setUserId(userId);
            apiScenarioFollowerMapper.insertSelective(apiScenarioFollower);
            apiScenarioLogService.followLog(id, userId);
        }
    }

    public ApiScenario add(ApiScenarioAddRequest request, String creator) {
        checkAddExist(request);
        request.setTags(ServiceUtils.parseTags(request.getTags()));
        ApiScenario scenario = getAddApiScenario(request, creator);
        scenario.setStepTotal(request.getSteps().size());
        apiScenarioMapper.insert(scenario);

        // 更新场景配置
        ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
        apiScenarioBlob.setId(scenario.getId());
        apiScenarioBlob.setConfig(JSON.toJSONString(request.getScenarioConfig()).getBytes());
        apiScenarioBlobMapper.insert(apiScenarioBlob);

        // 处理复制场景时的文件复制
        handleCopyFromScenarioFile(request, scenario, creator);

        // 处理csv文件
        handCsvFilesAdd(request, creator, scenario);

        // 处理添加的步骤
        handleStepAdd(request, scenario, creator);

        // 处理步骤文件
        apiScenarioFileService.handleStepFilesAdd(request, creator, scenario);

        return scenario;
    }

    /**
     * 处理复制场景时的文件复制
     *
     * @param request
     * @param scenario
     */
    private void handleCopyFromScenarioFile(ApiScenarioAddRequest request, ApiScenario scenario, String userId) {
        String copyFromScenarioId = request.getCopyFromScenarioId();
        if (StringUtils.isNotBlank(copyFromScenarioId)) {
            // 查询原场景的 csv 文件
            ApiScenarioCsvExample example = new ApiScenarioCsvExample();
            example.createCriteria().andScenarioIdEqualTo(copyFromScenarioId);
            Map<String, ApiScenarioCsv> csvMap = apiScenarioCsvMapper.selectByExample(example)
                    .stream()
                    .collect(Collectors.toMap(ApiScenarioCsv::getFileId, Function.identity()));

            if (!csvMap.isEmpty()) {
                ScenarioConfig scenarioConfig = request.getScenarioConfig();
                List<CsvVariable> csvVariables = scenarioConfig.getVariable().getCsvVariables();
                for (CsvVariable csvVariable : csvVariables) {
                    String originFileId = csvVariable.getFile().getFileId();
                    ApiScenarioCsv copyFromCsv = csvMap.get(originFileId);
                    if (copyFromCsv != null && BooleanUtils.isTrue(csvVariable.getFile().getLocal())) {
                        // 重新生成 fileId，从原场景复制文件
                        csvVariable.getFile().setFileId(IDGenerator.nextStr());
                        String sourceDir = DefaultRepositoryDir.getApiScenarioDir(request.getProjectId(), copyFromScenarioId);
                        String targetDir = DefaultRepositoryDir.getApiScenarioDir(request.getProjectId(), scenario.getId());
                        apiFileResourceService.copyFile(sourceDir + "/" + copyFromCsv.getFileId(),
                                targetDir + "/" + csvVariable.getFile().getFileId(),
                                csvVariable.getFile().getFileName());

                        // 在临时文件没有找到文件，则不会创建关联关系，这里手动添加
                        ApiFileResource apiFileResource = new ApiFileResource();
                        apiFileResource.setFileId(csvVariable.getFile().getFileId());
                        apiFileResource.setResourceId(scenario.getId());
                        apiFileResource.setResourceType(ApiResourceType.API_SCENARIO.name());
                        apiFileResource.setProjectId(scenario.getProjectId());
                        apiFileResource.setCreateTime(System.currentTimeMillis());
                        apiFileResource.setFileName(csvVariable.getFile().getFileName());
                        apiFileResourceMapper.insert(apiFileResource);
                    }
                }
            }
        }
    }

    private void handleStepAdd(ApiScenarioAddRequest request, ApiScenario scenario, String userId) {
        // 插入步骤
        if (CollectionUtils.isNotEmpty(request.getSteps())) {
            checkCircularRef(scenario.getId(), request.getSteps());
            // 获取待添加的步骤
            List<ApiScenarioCsvStep> csvSteps = new ArrayList<>();
            List<ApiScenarioStep> steps = getApiScenarioSteps(null, request.getSteps(), csvSteps);
            steps.forEach(step -> step.setScenarioId(scenario.getId()));
            // 处理特殊的步骤详情
            ApiScenarioCopyStepMap apiScenarioCopyStepMap = addSpecialStepDetails(request.getSteps(), request.getStepDetails());

            if (CollectionUtils.isNotEmpty(steps)) {
                apiScenarioStepMapper.batchInsert(steps);
            }

            // 处理copy的步骤文件
            apiScenarioFileService.handleSaveCopyStepFiles(apiScenarioCopyStepMap, request.getStepDetails(), scenario, userId);

            List<ApiScenarioStepBlob> apiScenarioStepBlobs = getUpdateStepBlobs(steps, request.getStepDetails());
            apiScenarioStepBlobs.forEach(step -> step.setScenarioId(scenario.getId()));

            if (CollectionUtils.isNotEmpty(apiScenarioStepBlobs)) {
                apiScenarioStepBlobMapper.batchInsert(apiScenarioStepBlobs);
            }

            csvSteps.forEach(step -> step.setScenarioId(scenario.getId()));

            csvSteps = filterNotExistCsv(request.getScenarioConfig(), csvSteps);
            saveStepCsv(scenario.getId(), csvSteps);
        }
    }

    public List<ApiScenarioCsvStep> filterNotExistCsv(ScenarioConfig scenarioConfig, List<ApiScenarioCsvStep> csvSteps) {
        Set<String> csvIdSet =
                getCsvVariables(scenarioConfig)
                        .stream()
                        .map(CsvVariable::getId)
                        .collect(Collectors.toSet());

        csvSteps = csvSteps.stream()
                .filter(step -> csvIdSet.contains(step.getFileId()))
                .collect(Collectors.toList());
        return csvSteps;
    }

    private void handCsvFilesAdd(ApiScenarioAddRequest request, String creator, ApiScenario scenario) {
        List<CsvVariable> csvVariables = getCsvVariables(request.getScenarioConfig());

        if (CollectionUtils.isEmpty(csvVariables)) {
            return;
        }

        // 处理 csv 相关数据表
        apiScenarioFileService.handleCsvDataUpdate(csvVariables, scenario, List.of());

        // 处理文件的上传
        apiScenarioFileService.handleCsvFileAdd(csvVariables, List.of(), scenario, creator);
    }

    public List<CsvVariable> getCsvVariables(ScenarioConfig scenarioConfig) {
        if (scenarioConfig == null || scenarioConfig.getVariable() == null || scenarioConfig.getVariable().getCsvVariables() == null) {
            return List.of();
        }
        return scenarioConfig.getVariable().getCsvVariables();
    }

    private void saveStepCsv(String scenarioId, List<ApiScenarioCsvStep> csvSteps) {
        // 先删除
        ApiScenarioCsvStepExample csvStepExample = new ApiScenarioCsvStepExample();
        csvStepExample.createCriteria().andScenarioIdEqualTo(scenarioId);
        apiScenarioCsvStepMapper.deleteByExample(csvStepExample);

        // 再添加
        if (CollectionUtils.isNotEmpty(csvSteps)) {
            SubListUtils.dealForSubList(csvSteps, 100, subList -> apiScenarioCsvStepMapper.batchInsert(subList));
        }
    }

    private ApiScenario getAddApiScenario(ApiScenarioAddRequest request, String creator) {
        ApiScenario scenario = new ApiScenario();
        BeanUtils.copyBean(scenario, request);
        scenario.setId(IDGenerator.nextStr());
        scenario.setNum(getNextNum(request.getProjectId()));
        scenario.setPos(getNextOrder(request.getProjectId()));
        scenario.setLatest(true);
        scenario.setCreateUser(creator);
        scenario.setUpdateUser(creator);
        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        scenario.setRefId(scenario.getId());
        scenario.setLastReportStatus(StringUtils.EMPTY);
        scenario.setDeleted(false);
        scenario.setRequestPassRate("0");
        scenario.setStepTotal(CollectionUtils.isEmpty(request.getSteps()) ? 0 : request.getSteps().size());
        return scenario;
    }

    public ApiScenario update(ApiScenarioUpdateRequest request, String updater) {
        checkResourceExist(request.getId());
        checkUpdateExist(request);
        request.setTags(ServiceUtils.parseTags(request.getTags()));
        // 更新基础信息
        ApiScenario scenario = BeanUtils.copyBean(new ApiScenario(), request);
        scenario.setUpdateUser(updater);
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setStepTotal(CollectionUtils.isNotEmpty(request.getSteps()) ? request.getSteps().size() : 0);
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);

        if (request.getScenarioConfig() != null) {
            // 更新场景配置
            ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
            apiScenarioBlob.setId(scenario.getId());
            apiScenarioBlob.setConfig(JSON.toJSONString(request.getScenarioConfig()).getBytes());
            apiScenarioBlobMapper.updateByPrimaryKeyWithBLOBs(apiScenarioBlob);
        }

        ApiScenario originScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());

        // 更新场景步骤
        updateApiScenarioStep(request, originScenario, updater);

        // 处理步骤文件
        apiScenarioFileService.handleStepFilesUpdate(request, updater, originScenario);

        // 处理 csv 文件
        apiScenarioFileService.handleCsvUpdate(request.getScenarioConfig(), scenario, updater);

        return scenario;
    }

    /**
     * 更新场景步骤
     */
    private void updateApiScenarioStep(ApiScenarioUpdateRequest request, ApiScenario scenario, String userId) {
        List<ApiScenarioStepRequest> steps = request.getSteps();
        // steps 不为 null 则修改
        if (steps != null) {
            if (CollectionUtils.isEmpty(steps)) {
                // 如果是空数组，则删除所有步骤
                deleteStepByScenarioId(scenario.getId());
                deleteStepDetailByScenarioId(scenario.getId());
                return;
            }
            checkCircularRef(scenario.getId(), steps);

            List<ApiScenarioCsvStep> scenarioCsvSteps = new ArrayList<>();
            // 获取待更新的步骤
            List<ApiScenarioStep> apiScenarioSteps = getApiScenarioSteps(null, steps, scenarioCsvSteps);
            apiScenarioSteps.forEach(step -> step.setScenarioId(scenario.getId()));
            scenarioCsvSteps.forEach(step -> step.setScenarioId(scenario.getId()));

            scenarioCsvSteps = filterNotExistCsv(request.getScenarioConfig(), scenarioCsvSteps);
            saveStepCsv(scenario.getId(), scenarioCsvSteps);

            // 获取待更新的步骤详情
            ApiScenarioCopyStepMap apiScenarioCopyStepMap = addSpecialStepDetails(steps, request.getStepDetails());

            List<ApiScenarioStepBlob> updateStepBlobs = getUpdateStepBlobs(apiScenarioSteps, request.getStepDetails());
            updateStepBlobs.forEach(step -> step.setScenarioId(scenario.getId()));

            List<String> stepIds = apiScenarioSteps.stream().map(ApiScenarioStep::getId).collect(Collectors.toList());
            List<String> originStepIds = extApiScenarioStepMapper.getStepIdsByScenarioId(scenario.getId());
            List<String> deleteStepIds = ListUtils.subtract(originStepIds, stepIds);

            // 步骤表-全部先删除再插入
            deleteStepByScenarioId(scenario.getId());
            apiScenarioStepMapper.batchInsert(apiScenarioSteps);

            // 处理copy的步骤文件
            apiScenarioFileService.handleSaveCopyStepFiles(apiScenarioCopyStepMap, request.getStepDetails(), scenario, userId);

            // 详情表-删除已经删除的步骤详情
            SubListUtils.dealForSubList(deleteStepIds, 100, subIds -> {
                ApiScenarioStepBlobExample stepBlobExample = new ApiScenarioStepBlobExample();
                stepBlobExample.createCriteria().andIdIn(subIds);
                apiScenarioStepBlobMapper.deleteByExample(stepBlobExample);
                // 批量删除关联文件
                String scenarioStepDirPrefix = DefaultRepositoryDir.getApiScenarioStepDir(scenario.getProjectId(), scenario.getId(), StringUtils.EMPTY);
                apiFileResourceService.deleteByResourceIds(scenarioStepDirPrefix, subIds, scenario.getProjectId(), userId, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO);
            });

            // 查询原有的步骤详情
            Set<String> originStepDetailIds = new HashSet<>(extApiScenarioStepBlobMapper.getStepIdsByScenarioId(scenario.getId()));

            // 添加新增的步骤详情
            List<ApiScenarioStepBlob> addApiScenarioStepsDetails = updateStepBlobs.stream()
                    .filter(step -> !originStepDetailIds.contains(step.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addApiScenarioStepsDetails)) {
                apiScenarioStepBlobMapper.batchInsert(addApiScenarioStepsDetails);
            }
            // 更新原有的步骤详情
            updateStepBlobs.stream()
                    .filter(step -> originStepDetailIds.contains(step.getId()))
                    .forEach(apiScenarioStepBlobMapper::updateByPrimaryKeySelective);
        } else if (MapUtils.isNotEmpty(request.getStepDetails())) {
            // steps 为 null，stepDetails 不为 null，则只更新详情
            // 查询原有的步骤详情
            Set<String> originStepDetailIds = new HashSet<>(extApiScenarioStepBlobMapper.getStepIdsByScenarioId(scenario.getId()));
            // 更新原有的步骤详情
            request.getStepDetails().forEach((stepId, stepDetail) -> {
                if (originStepDetailIds.contains(stepId)) {
                    ApiScenarioStepBlob apiScenarioStepBlob = getApiScenarioStepBlob(stepId, stepDetail);
                    apiScenarioStepBlobMapper.updateByPrimaryKeySelective(apiScenarioStepBlob);
                }
            });
        }
    }

    /**
     * 检测循环依赖
     *
     * @param scenarioId
     * @param steps
     */
    public void checkCircularRef(String scenarioId, List<ApiScenarioStepRequest> steps) {
        traversalStepTree(steps, step -> {
            if (isRefOrPartialRef(step.getRefType()) && StringUtils.equals(step.getResourceId(), scenarioId)) {
                throw new MSException(API_SCENARIO_CIRCULAR_REFERENCE);
            }
            return true;
        });
    }

    public ApiScenarioStepBlob getApiScenarioStepBlob(String stepId, Object stepDetail) {
        ApiScenarioStepBlob apiScenarioStepBlob = new ApiScenarioStepBlob();
        apiScenarioStepBlob.setId(stepId);
        apiScenarioStepBlob.setContent(JSON.toJSONString(stepDetail).getBytes());
        return apiScenarioStepBlob;
    }

    private void deleteStepDetailByScenarioId(String scenarioId) {
        ApiScenarioStepBlobExample blobExample = new ApiScenarioStepBlobExample();
        blobExample.createCriteria().andScenarioIdEqualTo(scenarioId);
        apiScenarioStepBlobMapper.deleteByExample(blobExample);
    }

    private void deleteStepByScenarioId(String scenarioId) {
        ApiScenarioStepExample example = new ApiScenarioStepExample();
        example.createCriteria().andScenarioIdEqualTo(scenarioId);
        apiScenarioStepMapper.deleteByExample(example);
    }

    /**
     * 获取待更新的 ApiScenarioStepBlob 列表
     */
    public List<ApiScenarioStepBlob> getUpdateStepBlobs(List<ApiScenarioStep> apiScenarioSteps, Map<String, Object> stepDetails) {
        if (MapUtils.isEmpty(stepDetails)) {
            return Collections.emptyList();
        }

        Map<String, ApiScenarioStep> scenarioStepMap = apiScenarioSteps.stream()
                .collect(Collectors.toMap(ApiScenarioStep::getId, Function.identity()));

        List<ApiScenarioStepBlob> apiScenarioStepBlobs = new ArrayList<>();
        stepDetails.forEach((stepId, stepDetail) -> {
            ApiScenarioStep step = scenarioStepMap.get(stepId);
            if (step == null) {
                return;
            }
            if (hasDetail(step.getStepType(), step.getRefType())) {
                // 非引用的步骤，如果有编辑内容，保存到blob表
                // 如果引用的是接口定义，也保存详情，因为应用接口定义允许修改参数
                ApiScenarioStepBlob apiScenarioStepBlob = new ApiScenarioStepBlob();
                apiScenarioStepBlob.setId(stepId);
                if (stepDetail instanceof byte[] detailBytes) {
                    apiScenarioStepBlob.setContent(detailBytes);
                } else {
                    apiScenarioStepBlob.setContent(JSON.toJSONString(stepDetail).getBytes());
                }
                apiScenarioStepBlobs.add(apiScenarioStepBlob);
            }
        });
        return apiScenarioStepBlobs;
    }

    private boolean isPartialRef(String refType) {
        return StringUtils.equals(refType, ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    public boolean isRefOrPartialRef(String refType) {
        return StringUtils.equalsAny(refType, ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    private boolean isRef(String refType) {
        return StringUtils.equals(refType, ApiScenarioStepRefType.REF.name());
    }

    /**
     * 是否有步骤详情
     * 非完全引用的步骤
     *
     * @param stepType
     * @param refType
     * @return
     */
    private boolean hasDetail(String stepType, String refType) {
        return !isRef(refType) || isRefApi(stepType, refType);
    }

    private boolean hasDetail(ApiScenarioStepCommonDTO step) {
        return hasDetail(step.getStepType(), step.getRefType());
    }

    /**
     * 解析步骤树结构
     * 获取待更新的 ApiScenarioStep 列表
     */
    public List<ApiScenarioStep> getApiScenarioSteps(ApiScenarioStepCommonDTO parent,
                                                     List<ApiScenarioStepRequest> steps, List<ApiScenarioCsvStep> csvSteps) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyList();
        }
        List<ApiScenarioStep> apiScenarioSteps = new ArrayList<>();
        long sort = 1;
        for (ApiScenarioStepRequest step : steps) {
            ApiScenarioStep apiScenarioStep = new ApiScenarioStep();
            BeanUtils.copyBean(apiScenarioStep, step);
            apiScenarioStep.setSort(sort++);
            if (parent != null) {
                apiScenarioStep.setParentId(parent.getId());
            }
            if (step.getConfig() != null) {
                apiScenarioStep.setConfig(JSON.toJSONString(step.getConfig()));
            }
            apiScenarioSteps.add(apiScenarioStep);

            if (StringUtils.equals(step.getStepType(), ApiScenarioStepType.API_SCENARIO.name())
                    && StringUtils.equalsAny(step.getRefType(), ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name())) {
                // 引用的步骤不解析子步骤
                continue;
            }

            if (CollectionUtils.isNotEmpty(step.getCsvIds())) {
                //如果是csv文件  需要保存到apiScenarioCsvStep表中
                step.getCsvIds().forEach(csvId -> {
                    ApiScenarioCsvStep csvStep = new ApiScenarioCsvStep();
                    csvStep.setId(IDGenerator.nextStr());
                    csvStep.setStepId(apiScenarioStep.getId());
                    csvStep.setFileId(csvId);
                    csvSteps.add(csvStep);
                });
            }
            // 解析子步骤
            apiScenarioSteps.addAll(getApiScenarioSteps(step, step.getChildren(), csvSteps));
        }
        return apiScenarioSteps;
    }

    /**
     * 补充
     * 部分引用的 detail
     * copyFromStepId 的 detail
     * isNew 的资源的 detail
     */
    public ApiScenarioCopyStepMap addSpecialStepDetails(List<ApiScenarioStepRequest> steps, Map<String, Object> stepDetails) {
        if (CollectionUtils.isEmpty(steps)) {
            return null;
        }

        // key 的 stepId，value 为 copyFrom 的步骤ID
        Map<String, String> copyFromStepIdMap = new HashMap<>();
        // key 的 stepId，value 为 copyFrom 的接口ID
        Map<String, String> isNewApiResourceMap = new HashMap<>();
        // key 的 stepId，value 为 copyFrom 的接口用例ID
        Map<String, String> isNewApiCaseResourceMap = new HashMap<>();

        // 遍历步骤树
        traversalStepTree(steps, (step) -> {
            ApiScenarioStepRequest stepRequest = (ApiScenarioStepRequest) step;

            // 补充部分引用的 detail
            addPartialRefStepDetail(stepDetails, step);

            // 如果该步骤没有步骤详情
            if (stepDetails.get(stepRequest.getId()) == null && hasDetail(step)) {
                if (StringUtils.isNotBlank(stepRequest.getCopyFromStepId())) {
                    // 处理 copyFromStep 的情况
                    if (stepDetails.containsKey(stepRequest.getCopyFromStepId())) {
                        // 如果有传 copyFromStepId 的详情，优先使用
                        stepDetails.put(step.getId(), stepDetails.get(stepRequest.getCopyFromStepId()));
                    } else {
                        // 没有传，则记录ID，后续统一查库
                        copyFromStepIdMap.put(stepRequest.getId(), stepRequest.getCopyFromStepId());
                    }
                } else if (isApiOrCase(step) && BooleanUtils.isTrue(((ApiScenarioStepRequest) step).getIsNew())) {
                    // 处理 isNew 的用例步骤
                    if (isApi(step.getStepType())) {
                        isNewApiResourceMap.put(step.getId(), step.getResourceId());
                    } else {
                        isNewApiCaseResourceMap.put(step.getId(), step.getResourceId());
                    }
                }
            }

            if (isRefOrPartialRef(step.getRefType())) {
                // 引用或者部分引用类型不解析子步骤
                return false;
            }
            return true;
        });

        // 处理新copy的接口定义的步骤详情
        putCopyStepDetails(stepDetails, isNewApiResourceMap, (subIds, copyFromBlobMap) -> {
            apiDefinitionService.getBlobByIds(subIds)
                    .forEach(apiDefinitionBlob -> copyFromBlobMap.put(apiDefinitionBlob.getId(), apiDefinitionBlob.getRequest()));
        });

        // 处理新copy的接口用例的步骤详情
        putCopyStepDetails(stepDetails, isNewApiCaseResourceMap, (subIds, copyFromBlobMap) -> {
            apiTestCaseService.getBlobByIds(subIds)
                    .forEach(apiTestCaseBlob -> copyFromBlobMap.put(apiTestCaseBlob.getId(), apiTestCaseBlob.getRequest()));
        });

        // 处理新copy的步骤的步骤详情
        putCopyStepDetails(stepDetails, copyFromStepIdMap, (subIds, copyFromBlobMap) -> {
            ApiScenarioStepBlobExample example = new ApiScenarioStepBlobExample();
            example.createCriteria().andIdIn(subIds);
            apiScenarioStepBlobMapper.selectByExampleWithBLOBs(example)
                    .forEach(scenarioStepBlob -> copyFromBlobMap.put(scenarioStepBlob.getId(), scenarioStepBlob.getContent()));
        });

        ApiScenarioCopyStepMap apiScenarioCopyStepMap = new ApiScenarioCopyStepMap();
        apiScenarioCopyStepMap.setCopyFromStepIdMap(copyFromStepIdMap);
        apiScenarioCopyStepMap.setIsNewApiResourceMap(isNewApiResourceMap);
        apiScenarioCopyStepMap.setIsNewApiCaseResourceMap(isNewApiCaseResourceMap);
        return apiScenarioCopyStepMap;
    }

    /**
     * @param stepDetails
     * @param copyFromIdMap        key 为 stepID，value 为复制的 resourceId 或者 stepId
     * @param handlePutBlobMapFunc
     */
    private void putCopyStepDetails(Map<String, Object> stepDetails, Map<String, String> copyFromIdMap, BiConsumer<List<String>, Map<String, byte[]>> handlePutBlobMapFunc) {
        if (MapUtils.isEmpty(copyFromIdMap)) {
            return;
        }
        // 处理新copy的步骤的步骤详情
        Map<String, byte[]> copyFromBlobMap = new HashMap<>();
        SubListUtils.dealForSubList(copyFromIdMap.values().stream().toList(), 50, (subIds) -> {
            handlePutBlobMapFunc.accept(subIds, copyFromBlobMap);
        });

        copyFromIdMap.forEach((stepId, copyFromId) -> {
            if (copyFromBlobMap.containsKey(copyFromId)) {
                stepDetails.put(stepId, copyFromBlobMap.get(copyFromId));
            }
        });
    }

    /**
     * 补充部分引用的 detail
     *
     * @param stepDetails
     * @param step
     */
    private void addPartialRefStepDetail(Map<String, Object> stepDetails, ApiScenarioStepCommonDTO step) {
        if (isPartialRef(step)) {
            // 如果是部分引用，blob表保存启用的子步骤ID
            stepDetails.put(step.getId(), getPartialRefStepDetail(step.getChildren()));
        }
    }

    private boolean isApiOrCase(ApiScenarioStepCommonDTO step) {
        return isApi(step.getStepType()) || isApiCase(step.getStepType());
    }

    /**
     * 遍历步骤树
     */
    public void traversalStepTree(List<? extends ApiScenarioStepCommonDTO> steps, Function<ApiScenarioStepCommonDTO, Boolean> handleStepFunc) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        for (ApiScenarioStepCommonDTO step : steps) {
            Boolean isParseChild = handleStepFunc.apply(step);
            if (BooleanUtils.isTrue(isParseChild)) {
                traversalStepTree(step.getChildren(), handleStepFunc);
            }
        }
    }

    /**
     * 获取步骤及子步骤中 enable 的步骤ID
     */
    private PartialRefStepDetail getPartialRefStepDetail(List<? extends ApiScenarioStepCommonDTO> steps) {
        PartialRefStepDetail partialRefStepDetail = new PartialRefStepDetail();
        if (CollectionUtils.isEmpty(steps)) {
            return partialRefStepDetail;
        }
        Set<String> enableSteps = partialRefStepDetail.getEnableStepIds();
        Set<String> disableStepIds = partialRefStepDetail.getDisableStepIds();
        for (ApiScenarioStepCommonDTO step : steps) {
            if (BooleanUtils.isTrue(step.getEnable())) {
                enableSteps.add(step.getId());
            } else {
                disableStepIds.add(step.getId());
            }
            // 完全引用和部分引用不解析子步骤
            if (!isRefOrPartialRef(step.getRefType())) {
                // 获取子步骤中 enable 的步骤
                PartialRefStepDetail childPartialRefStepDetail = getPartialRefStepDetail(step.getChildren());
                enableSteps.addAll(childPartialRefStepDetail.getEnableStepIds());
                disableStepIds.addAll(childPartialRefStepDetail.getDisableStepIds());
            }
        }
        return partialRefStepDetail;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.API_SCENARIO);
    }

    public long getNextOrder(String projectId) {
        Long pos = extApiScenarioMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
    }

    public void delete(String id, String operator) {
        checkResourceExist(id);
        ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(id);
        cascadeDelete(scenario, operator);
        apiScenarioMapper.deleteByPrimaryKey(id);
    }

    public ApiScenarioBatchOperationResponse delete(@NotEmpty List<String> idList, String projectId, String operator) {
        ApiScenarioBatchOperationResponse response = new ApiScenarioBatchOperationResponse();

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(idList).andDeletedEqualTo(true);
        List<ApiScenario> scenarioList = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(scenarioList)) {
            apiScenarioMapper.deleteByExample(example);
            cascadeDelete(scenarioList, projectId, operator);
        }

        //构建返回值
        for (ApiScenario scenario : scenarioList) {
            response.addSuccessData(scenario.getId(), scenario.getNum(), scenario.getName());
        }
        return response;
    }

    //级联删除
    public void cascadeDelete(ApiScenario scenario, String operator) {
        //删除blob
        apiScenarioBlobMapper.deleteByPrimaryKey(scenario.getId());
        //删除step
        deleteStepByScenarioId(scenario.getId());
        //删除step-blob
        deleteStepDetailByScenarioId(scenario.getId());
        //删除csv
        deleteCsvByScenarioId(scenario.getId());
        //删除文件
        String scenarioDir = DefaultRepositoryDir.getApiScenarioDir(scenario.getProjectId(), scenario.getId());
        try {
            apiFileResourceService.deleteByResourceId(scenarioDir, scenario.getId(), scenario.getProjectId(), operator, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO);
        } catch (Exception ignore) {
        }

        //删除定时任务
        scheduleService.deleteByResourceId(scenario.getId(), ApiScenarioScheduleJob.class.getName());

        //删除功能用例关联关系
        FunctionalCaseTestExample functionalCaseTestExample = new FunctionalCaseTestExample();
        functionalCaseTestExample.createCriteria().andSourceIdEqualTo(scenario.getId()).andSourceTypeEqualTo(SCENARIO);
        functionalCaseTestMapper.deleteByExample(functionalCaseTestExample);

    }

    private void deleteCsvByScenarioId(String id) {
        ApiScenarioCsvExample example = new ApiScenarioCsvExample();
        example.createCriteria().andScenarioIdEqualTo(id);
        apiScenarioCsvMapper.deleteByExample(example);

        ApiScenarioCsvStepExample stepExample = new ApiScenarioCsvStepExample();
        stepExample.createCriteria().andScenarioIdEqualTo(id);
        apiScenarioCsvStepMapper.deleteByExample(stepExample);
    }

    //级联删除
    public void cascadeDelete(@NotEmpty List<ApiScenario> scenarioList, String projectId, String operator) {
        List<String> scenarioIdList = scenarioList.stream().map(ApiScenario::getId).toList();
        ApiScenarioBlobExample example = new ApiScenarioBlobExample();
        example.createCriteria().andIdIn(scenarioIdList);
        //删除blob
        apiScenarioBlobMapper.deleteByExample(example);


        ApiScenarioStepExample stepExample = new ApiScenarioStepExample();
        stepExample.createCriteria().andScenarioIdIn(scenarioIdList);
        //删除step
        apiScenarioStepMapper.deleteByExample(stepExample);

        //删除step-blob
        ApiScenarioStepBlobExample blobExample = new ApiScenarioStepBlobExample();
        blobExample.createCriteria().andScenarioIdIn(scenarioIdList);
        apiScenarioStepBlobMapper.deleteByExample(blobExample);

        //删除文件
        String scenarioDir = DefaultRepositoryDir.getApiScenarioDir(projectId, StringUtils.EMPTY);
        apiFileResourceService.deleteByResourceIds(scenarioDir, scenarioIdList, projectId, operator, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO);
        //删除定时任务
        scheduleService.deleteByResourceIds(scenarioIdList, ApiScenarioScheduleJob.class.getName());

        //删除csv
        ApiScenarioCsvExample csvExample = new ApiScenarioCsvExample();
        csvExample.createCriteria().andScenarioIdIn(scenarioIdList);
        List<ApiScenarioCsv> apiScenarioCsv = apiScenarioCsvMapper.selectByExample(csvExample);
        if (CollectionUtils.isNotEmpty(apiScenarioCsv)) {
            List<String> fileIds = apiScenarioCsv.stream().map(ApiScenarioCsv::getFileId).toList();
            //删除关联关系
            ApiScenarioCsvStepExample csvStepExample = new ApiScenarioCsvStepExample();
            csvStepExample.createCriteria().andFileIdIn(fileIds);
            apiScenarioCsvStepMapper.deleteByExample(csvStepExample);
        }
        apiScenarioCsvMapper.deleteByExample(csvExample);

        //删除功能用例关联关系
        FunctionalCaseTestExample functionalCaseTestExample = new FunctionalCaseTestExample();
        functionalCaseTestExample.createCriteria().andSourceIdIn(scenarioIdList).andSourceTypeEqualTo(SCENARIO);
        functionalCaseTestMapper.deleteByExample(functionalCaseTestExample);

    }


    public void recover(String id) {
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId(id);
        apiScenario.setDeleted(false);
        apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);
    }

    public void deleteToGc(String id, String operator) {
        checkResourceExist(id);
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId(id);
        apiScenario.setDeleted(true);
        apiScenario.setDeleteUser(operator);
        apiScenario.setDeleteTime(System.currentTimeMillis());
        apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);

        //删除定时任务
        scheduleService.deleteByResourceId(id, ApiScenarioScheduleJob.class.getName());
    }

    private void checkAddExist(ApiScenarioAddRequest apiScenario) {
        ApiScenarioExample example = new ApiScenarioExample();
        // 统一模块下名称不能重复
        example.createCriteria()
                .andNameEqualTo(apiScenario.getName())
                .andModuleIdEqualTo(apiScenario.getModuleId())
                .andDeletedEqualTo(false)
                .andProjectIdEqualTo(apiScenario.getProjectId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            throw new MSException(API_SCENARIO_EXIST);
        }
    }

    private void checkUpdateExist(ApiScenarioUpdateRequest request) {
        if (StringUtils.isBlank(request.getName())) {
            return;
        }
        // 统一模块下名称不能重复
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria()
                .andIdNotEqualTo(request.getId())
                .andModuleIdEqualTo(request.getModuleId())
                .andNameEqualTo(request.getName())
                .andDeletedEqualTo(false)
                .andProjectIdEqualTo(request.getProjectId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            throw new MSException(API_SCENARIO_EXIST);
        }
    }

    public ApiScenario checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(apiScenarioMapper.selectByPrimaryKey(id), "permission.system_api_scenario.name");
    }

    public boolean isRequestStep(ApiScenarioStepCommonDTO step) {
        ApiScenarioStepType scenarioStepType = EnumValidator.validateEnum(ApiScenarioStepType.class, step.getStepType());
        return scenarioStepType == null ? false : scenarioStepType.isRequest();
    }

    public static boolean isScenarioStep(String stepType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API_SCENARIO.name());
    }

    /**
     * 设置单个部分引用的步骤的启用状态
     */
    public void setPartialRefStepEnable(ApiScenarioStepCommonDTO step, Map<String, String> stepDetailMap) {
        String stepDetail = stepDetailMap.get(step.getId());
        if (!isPartialRef(step) || StringUtils.isBlank(stepDetail)) {
            return;
        }
        setChildPartialRefEnable(step.getChildren(), JSON.parseObject(stepDetail, PartialRefStepDetail.class));
    }

    /**
     * 设置部分引用的步骤的启用状态
     */
    private void setChildPartialRefEnable(List<? extends ApiScenarioStepCommonDTO> steps, PartialRefStepDetail partialRefStepDetail) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        for (ApiScenarioStepCommonDTO step : steps) {
            if (StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.REF.name())) {
                // 引用的启用不修改
                continue;
            }
            // 非完全引用的步骤，使用当前场景配置的启用状态
            Set<String> enableStepIds = partialRefStepDetail.getEnableStepIds();
            Set<String> disableStepIds = partialRefStepDetail.getDisableStepIds();

            // 如果是新添加的步骤，则不改变启用状态
            if (enableStepIds.contains(step.getId()) || disableStepIds.contains(step.getId())) {
                step.setEnable(enableStepIds.contains(step.getId()));
            }

            if (isPartialRef(step)) {
                // 如果是部分引用的场景，不递归解析了，上层的递归会解析
                continue;
            }
            // 非完全引用和部分引用的步骤，递归设置子步骤
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                setChildPartialRefEnable(step.getChildren(), partialRefStepDetail);
            }
        }
    }

    private boolean isPartialRef(ApiScenarioStepCommonDTO step) {
        return isScenarioStep(step.getStepType()) &&
                StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    /**
     * 查询部分引用的步骤的详情
     *
     * @param steps
     * @return
     */

    public Map<String, String> getPartialRefStepDetailMap(List<? extends ApiScenarioStepCommonDTO> steps) {
        List<String> needBlobStepIds = steps.stream()
                .filter(this::isPartialRef)
                .map(ApiScenarioStepCommonDTO::getId)
                .toList();

        return getStepBlobByIds(needBlobStepIds).stream()
                .collect(Collectors.toMap(ApiScenarioStepBlob::getId, blob -> new String(blob.getContent())));
    }

    public Map<String, String> getStepDetailMap(List<? extends ApiScenarioStepCommonDTO> steps) {
        List<String> stepIdList = steps.stream()
                .map(ApiScenarioStepCommonDTO::getId).distinct()
                .toList();
        return getStepBlobByIds(stepIdList).stream()
                .collect(Collectors.toMap(ApiScenarioStepBlob::getId, blob -> new String(blob.getContent())));
    }

    public List<ApiScenarioStepBlob> getStepBlobByIds(List<String> stepIds) {
        if (CollectionUtils.isEmpty(stepIds)) {
            return Collections.emptyList();
        }
        ApiScenarioStepBlobExample example = new ApiScenarioStepBlobExample();
        example.createCriteria().andIdIn(stepIds);
        return apiScenarioStepBlobMapper.selectByExampleWithBLOBs(example);
    }

    public void updateStatus(String id, String status, String userId) {
        checkResourceExist(id);
        ApiScenario update = new ApiScenario();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateUser(userId);
        update.setUpdateTime(System.currentTimeMillis());
        apiScenarioMapper.updateByPrimaryKeySelective(update);
    }

    public void updatePriority(String id, String priority, String userId) {
        checkResourceExist(id);
        ApiScenario update = new ApiScenario();
        update.setId(id);
        update.setPriority(priority);
        update.setUpdateUser(userId);
        update.setUpdateTime(System.currentTimeMillis());
        apiScenarioMapper.updateByPrimaryKeySelective(update);
    }

    public ApiScenarioDetailDTO getApiScenarioDetailDTO(String scenarioId, String userId) {
        ApiScenarioDetail apiScenarioDetail = get(scenarioId);
        ApiScenarioDetailDTO apiScenarioDetailDTO = BeanUtils.copyBean(new ApiScenarioDetailDTO(), apiScenarioDetail);
        Map<String, String> userNameMap = userLoginService.getUserNameMap(List.of(apiScenarioDetail.getCreateUser(), apiScenarioDetail.getUpdateUser()));
        apiScenarioDetailDTO.setCreateUserName(userNameMap.get(apiScenarioDetail.getCreateUser()));
        apiScenarioDetailDTO.setUpdateUserName(userNameMap.get(apiScenarioDetail.getUpdateUser()));

        // 设置是否关注
        ApiScenarioFollowerExample followerExample = new ApiScenarioFollowerExample();
        followerExample.createCriteria().andApiScenarioIdEqualTo(scenarioId);
        followerExample.createCriteria().andUserIdEqualTo(userId);
        List<ApiScenarioFollower> followers = apiScenarioFollowerMapper.selectByExample(followerExample);
        apiScenarioDetailDTO.setFollow(CollectionUtils.isNotEmpty(followers));

        // 设置关联的文件的最新信息
        List<ApiFile> csvApiFiles = apiScenarioDetail.getScenarioConfig()
                .getVariable()
                .getCsvVariables()
                .stream()
                .map(CsvVariable::getFile)
                .toList();
        apiCommonService.setLinkFileInfo(apiScenarioDetail.getId(), csvApiFiles);
        return apiScenarioDetailDTO;
    }

    public ApiScenario checkResourceIsNoDeleted(String id) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiScenarios)) {
            throw new MSException(ApiResultCode.CASE_NOT_EXIST);
        }
        return apiScenarios.getFirst();
    }

    public ApiScenarioDetail get(String scenarioId) {
        ApiScenario apiScenario = checkResourceIsNoDeleted(scenarioId);
        ApiScenarioDetail apiScenarioDetail = BeanUtils.copyBean(new ApiScenarioDetail(), apiScenario);
        apiScenarioDetail.setSteps(List.of());
        ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(scenarioId);

        if (apiScenarioBlob != null) {
            apiScenarioDetail.setScenarioConfig(JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class));
        }

        //存放csv变量
        apiScenarioDetail.getScenarioConfig().getVariable().setCsvVariables(apiScenarioFileService.getCsvVariables(scenarioId));

        // 获取所有步骤
        List<ApiScenarioStepDTO> allSteps = getAllStepsByScenarioIds(List.of(scenarioId))
                .stream()
                .distinct() // 这里可能存在多次引用相同场景，步骤可能会重复，去重
                .collect(Collectors.toList());

        // 设置步骤的 csvIds
        setStepCsvIds(scenarioId, allSteps);

        // 构造 map，key 为场景ID，value 为步骤列表
        Map<String, List<ApiScenarioStepDTO>> scenarioStepMap = allSteps.stream()
                .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getScenarioId()).orElse(StringUtils.EMPTY)));

        // key 为父步骤ID，value 为子步骤列表
        if (MapUtils.isEmpty(scenarioStepMap)) {
            return apiScenarioDetail;
        }

        Map<String, List<ApiScenarioStepDTO>> currentScenarioParentStepMap = scenarioStepMap.get(scenarioId)
                .stream()
                .collect(Collectors.groupingBy(step -> {
                    if (StringUtils.equals(step.getParentId(), "NONE")) {
                        step.setParentId(StringUtils.EMPTY);
                    }
                    return Optional.ofNullable(step.getParentId()).orElse(StringUtils.EMPTY);
                }));

        List<ApiScenarioStepDTO> steps = buildStepTree(currentScenarioParentStepMap.get(StringUtils.EMPTY), currentScenarioParentStepMap, scenarioStepMap, new HashSet<>());

        // 查询步骤详情
        Map<String, String> stepDetailMap = getPartialRefStepDetailMap(allSteps);

        // 设置部分引用的步骤的启用状态
        setPartialRefStepsEnable(steps, stepDetailMap);

        apiScenarioDetail.setSteps(steps);

        return apiScenarioDetail;
    }


    public List<ApiScenarioDetail> list(List<String> scenarioIds) {
        List<ApiScenarioDetail> list = new LinkedList<>();

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(scenarioIds).andDeletedEqualTo(false);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);


        ApiScenarioBlobExample blobExample = new ApiScenarioBlobExample();
        blobExample.createCriteria().andIdIn(scenarioIds);
        List<ApiScenarioBlob> apiScenarioBlobs = apiScenarioBlobMapper.selectByExampleWithBLOBs(blobExample);
        Map<String, ApiScenarioBlob> scenarioMap = apiScenarioBlobs.stream()
                .collect(Collectors.toMap(ApiScenarioBlob::getId, item -> item));

        apiScenarios.forEach(apiScenario -> {
            ApiScenarioDetail apiScenarioDetail = BeanUtils.copyBean(new ApiScenarioDetail(), apiScenario);
            apiScenarioDetail.setSteps(List.of());
            ApiScenarioBlob apiScenarioBlob = scenarioMap.get(apiScenario.getId());

            if (apiScenarioBlob != null) {
                apiScenarioDetail.setScenarioConfig(JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class));
            }

            //存放csv变量
            apiScenarioDetail.getScenarioConfig().getVariable().setCsvVariables(apiScenarioFileService.getCsvVariables(apiScenario.getId()));

            // 获取所有步骤
            List<ApiScenarioStepDTO> allSteps = getAllStepsByScenarioIds(List.of(apiScenario.getId()))
                    .stream()
                    .distinct() // 这里可能存在多次引用相同场景，步骤可能会重复，去重
                    .collect(Collectors.toList());

            // 设置步骤的 csvIds
            setStepCsvIds(apiScenario.getId(), allSteps);

            // 构造 map，key 为场景ID，value 为步骤列表
            Map<String, List<ApiScenarioStepDTO>> scenarioStepMap = allSteps.stream()
                    .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getScenarioId()).orElse(StringUtils.EMPTY)));

            // key 为父步骤ID，value 为子步骤列表
            if (MapUtils.isEmpty(scenarioStepMap)) {
                list.add(apiScenarioDetail);
                return;
            }

            Map<String, List<ApiScenarioStepDTO>> currentScenarioParentStepMap = scenarioStepMap.get(apiScenario.getId())
                    .stream()
                    .collect(Collectors.groupingBy(step -> {
                        if (StringUtils.equals(step.getParentId(), "NONE")) {
                            step.setParentId(StringUtils.EMPTY);
                        }
                        return Optional.ofNullable(step.getParentId()).orElse(StringUtils.EMPTY);
                    }));

            List<ApiScenarioStepDTO> steps = buildStepTree(currentScenarioParentStepMap.get(StringUtils.EMPTY), currentScenarioParentStepMap, scenarioStepMap, new HashSet<>());

            // 查询步骤详情
            Map<String, String> stepDetailMap = getPartialRefStepDetailMap(allSteps);

            // 设置部分引用的步骤的启用状态
            setPartialRefStepsEnable(steps, stepDetailMap);

            apiScenarioDetail.setSteps(steps);

            list.add(apiScenarioDetail);
        });

        return list;
    }

    private void setStepCsvIds(String scenarioId, List<ApiScenarioStepDTO> allSteps) {
        List<String> refScenarioIds = allSteps.stream()
                .filter(this::isRefOrPartialScenario)
                .map(ApiScenarioStepCommonDTO::getResourceId)
                .collect(Collectors.toList());
        refScenarioIds.add(scenarioId);

        //获取所有步骤的csv的关联关系
        List<ApiScenarioCsvStep> csvSteps = extApiScenarioStepMapper.getCsvStepByScenarioIds(refScenarioIds);
        // 构造 map，key 为步骤ID，value 为csv文件ID列表
        Map<String, List<String>> stepsCsvMap = csvSteps.stream()
                .collect(Collectors.groupingBy(ApiScenarioCsvStep::getStepId, Collectors.mapping(ApiScenarioCsvStep::getFileId, Collectors.toList())));

        //将stepsCsvMap根据步骤id放入到allSteps中
        if (CollectionUtils.isNotEmpty(allSteps)) {
            allSteps.forEach(step -> step.setCsvIds(stepsCsvMap.get(step.getId())));
        }
    }


    /**
     * 设置部分引用的步骤的启用状态
     */
    public void setPartialRefStepsEnable(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, String> stepDetailMap) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        for (ApiScenarioStepCommonDTO step : steps) {
            setPartialRefStepEnable(step, stepDetailMap);
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                setPartialRefStepsEnable(step.getChildren(), stepDetailMap);
            }
        }
    }

    /**
     * 判断步骤是否是引用的接口定义
     * 引用的接口定义允许修改参数值，需要特殊处理
     */
    public boolean isRefApi(String stepType, String refType) {
        return isApi(stepType) && StringUtils.equals(refType, ApiScenarioStepRefType.REF.name());
    }

    /**
     * 判断步骤是否是引用的接口定义
     * 引用的接口定义允许修改参数值，需要特殊处理
     */
    public boolean isCopyApi(String stepType, String refType) {
        return isApi(stepType) && isCopy(refType);
    }

    public boolean isApi(String stepType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API.name());
    }

    public boolean isCopy(String refType) {
        return StringUtils.equals(refType, ApiScenarioStepRefType.COPY.name());
    }

    public boolean isApiCase(String stepType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API_CASE.name());
    }

    /**
     * 递归构造步骤树
     *
     * @param steps           当前场景下，当前层级的步骤
     * @param parentStepMap   当前场景所有的步骤，key 为父步骤ID，value 为子步骤列表
     * @param scenarioStepMap 所有场景步骤，key 为场景ID，value 为子步骤列表
     */
    public List<ApiScenarioStepDTO> buildStepTree(List<ApiScenarioStepDTO> steps,
                                                  Map<String, List<ApiScenarioStepDTO>> parentStepMap,
                                                  Map<String, List<ApiScenarioStepDTO>> scenarioStepMap,
                                                  Set<String> stepIdSet) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyList();
        }

        for (int i = 0; i < steps.size(); i++) {
            ApiScenarioStepDTO step = steps.get(i);
            if (stepIdSet.contains(step.getId())) {
                // 如果步骤ID已存在，说明引用了两个相同的场景，其子步骤ID可能会重复，导致引用的同一个对象
                // 这里重新new一个对象，避免执行时，处理为同一个步骤
                step = BeanUtils.copyBean(new ApiScenarioStepDTO(), step);
                steps.set(i, step);
            }
            stepIdSet.add(step.getId());

            // 获取当前步骤的子步骤
            List<ApiScenarioStepDTO> children = Optional.ofNullable(parentStepMap.get(step.getId())).orElse(new ArrayList<>(0));
            if (isRefOrPartialScenario(step)) {

                List<ApiScenarioStepDTO> scenarioSteps = scenarioStepMap.get(step.getResourceId());

                if (CollectionUtils.isEmpty(scenarioSteps)) {
                    continue;
                }

                scenarioSteps.forEach(item -> {
                    // 如果步骤的场景ID不等于当前场景的ID，说明是引用的步骤，如果 parentId 为空，说明是一级子步骤，重新挂载到对应的场景中
                    if (StringUtils.isEmpty(item.getParentId())) {
                        children.add(item);
                    }
                });

                if (CollectionUtils.isEmpty(children)) {
                    continue;
                }

                // 如果当前步骤是引用的场景，获取该场景的子步骤
                Map<String, List<ApiScenarioStepDTO>> childStepMap = scenarioSteps
                        .stream()
                        .collect(Collectors.groupingBy(item -> Optional.ofNullable(item.getParentId()).orElse(StringUtils.EMPTY)));
                step.setChildren(buildStepTree(children, childStepMap, scenarioStepMap, stepIdSet));
            } else {
                if (CollectionUtils.isEmpty(children)) {
                    continue;
                }
                step.setChildren(buildStepTree(children, parentStepMap, scenarioStepMap, stepIdSet));
            }
        }

        // 排序
        return steps.stream()
                .sorted(Comparator.comparing(ApiScenarioStepDTO::getSort))
                .collect(Collectors.toList());
    }

    /**
     * 判断步骤是否是引用的场景
     */
    public boolean isRefOrPartialScenario(ApiScenarioStepDTO step) {
        return isRefOrPartialRef(step.getRefType()) && isScenarioStep(step.getStepType());
    }

    /**
     * 递归获取所有的场景步骤
     */
    public List<ApiScenarioStepDTO> getAllStepsByScenarioIds(List<String> scenarioIds) {
        List<ApiScenarioStepDTO> steps = getStepDTOByScenarioIds(scenarioIds);
        if (CollectionUtils.isEmpty(steps)) {
            return steps;
        }

        // 将 config 转换成对象
        steps.forEach(step -> {
            if (step.getConfig() != null && StringUtils.isNotBlank(step.getConfig().toString())) {
                if (step.getConfig() instanceof String configVal) {
                    step.setConfig(JSON.parseObject(configVal));
                }
            }
        });

        // 获取步骤中引用的场景ID
        List<String> childScenarioIds = steps.stream()
                .filter(this::isRefOrPartialScenario)
                .map(ApiScenarioStepDTO::getResourceId)
                .collect(Collectors.toList());

        // 嵌套获取引用的场景步骤
        steps.addAll(getAllStepsByScenarioIds(childScenarioIds));
        return steps;
    }

    private List<ApiScenarioStepDTO> parseConfig(List<ApiScenarioStepDTO> steps, String parentId) {
        List<ApiScenarioStepDTO> returnList = new ArrayList<>();
        // 将 config 转换成对象
        steps.forEach(dto -> {
            ApiScenarioStepDTO returnDTO = new ApiScenarioStepDTO();
            BeanUtils.copyBean(returnDTO, dto);
            if (StringUtils.isNotBlank(parentId) && !StringUtils.equalsIgnoreCase(parentId, dto.getId())) {
                if (StringUtils.isBlank(returnDTO.getParentId())) {
                    returnDTO.setParentId(parentId);
                }
            }
            if (returnDTO.getConfig() != null && StringUtils.isNotBlank(returnDTO.getConfig().toString())) {
                if (returnDTO.getConfig() instanceof String configVal) {
                    returnDTO.setConfig(JSON.parseObject(configVal));
                }
            }
            returnList.add(returnDTO);
        });
        return returnList;
    }

    /**
     * 递归获取所有的场景步骤
     */
    public List<ApiScenarioStepDTO> buildScenarioSteps(List<String> scenarioIds) {
        List<ApiScenarioStepDTO> returnList = new ArrayList<>();

        List<ApiScenarioStepDTO> steps = getStepDTOByScenarioIds(scenarioIds);
        if (CollectionUtils.isEmpty(steps)) {
            return returnList;
        } else {
            returnList.addAll(this.parseConfig(steps, null));
        }

        List<ApiScenarioStepDTO> refScenarioSteps = steps.stream().filter(k -> isScenarioStep(k.getStepType())).toList();
        while (CollectionUtils.isNotEmpty(refScenarioSteps)) {
            List<ApiScenarioStepDTO> childStep = new ArrayList<>();
            for (ApiScenarioStepDTO step : refScenarioSteps) {
                childStep = getStepDTOByScenarioIds(Collections.singletonList(step.getResourceId()));
                if (CollectionUtils.isNotEmpty(childStep)) {
                    returnList.addAll(this.parseConfig(childStep, step.getId()));
                }
            }
            refScenarioSteps = childStep.stream().filter(k -> isScenarioStep(k.getStepType())).toList();
        }
        // 嵌套获取引用的场景步骤
        return returnList.stream().sorted(Comparator.comparing(ApiScenarioStepDTO::getSort)).collect(Collectors.toList());
    }

    private List<ApiScenarioStepDTO> getStepDTOByScenarioIds(List<String> scenarioIds) {
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return Collections.emptyList();
        }
        return extApiScenarioStepMapper.getStepDTOByScenarioIds(scenarioIds);
    }

    public Object getStepDetail(String stepId) {
        ApiScenarioStep step = apiScenarioStepMapper.selectByPrimaryKey(stepId);
        return getStepDetail(BeanUtils.copyBean(new ApiScenarioStepDetailRequest(), step));
    }

    private Object getStepDetail(ApiScenarioStepDetailRequest step) {
        StepParser stepParser = StepParserFactory.getStepParser(step.getStepType());
        Object stepDetail = stepParser.parseDetail(step);
        if (stepDetail instanceof MsScriptElement msScriptElement) {
            apiCommonService.setEnableCommonScriptProcessorInfo(msScriptElement);
        } else if (stepDetail instanceof AbstractMsTestElement msTestElement) {
            // 设置关联的文件的最新信息
            if (isRef(step.getRefType())) {
                if (isApi(step.getStepType())) {
                    ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(step.getResourceId());
                    apiCommonService.setApiDefinitionExecuteInfo(msTestElement, apiDefinition);
                } else if (isApiCase(step.getStepType())) {
                    ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(step.getResourceId());
                    ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
                    apiCommonService.setApiDefinitionExecuteInfo(msTestElement, apiDefinition);
                }
            }
            apiCommonService.setLinkFileInfo(step.getId(), msTestElement);
            apiCommonService.setEnableCommonScriptProcessorInfo(msTestElement);
        }
        return stepDetail;
    }

    private void checkTargetModule(String targetModuleId, String projectId) {
        if (!StringUtils.equals(targetModuleId, ModuleConstants.DEFAULT_NODE_ID)) {
            ApiScenarioModule module = apiScenarioModuleMapper.selectByPrimaryKey(targetModuleId);
            if (module == null || !StringUtils.equals(module.getProjectId(), projectId)) {
                throw new MSException(Translator.get("module.not.exist"));
            }
        }
    }

    public ApiScenarioBatchOperationResponse batchMove(ApiScenarioBatchCopyMoveRequest request, LogInsertModule logInsertModule) {
        this.checkTargetModule(request.getTargetModuleId(), request.getProjectId());

        List<String> scenarioIds = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return new ApiScenarioBatchOperationResponse();
        }
        request.setSelectIds(scenarioIds);
        long moveTime = System.currentTimeMillis();
        ApiScenarioBatchOperationResponse response =
                ApiScenarioBatchOperationUtils.executeWithBatchOperationResponse(scenarioIds, sublist -> move(sublist, request, moveTime, logInsertModule.getOperator()));
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(), OperationLogType.UPDATE.name(), logInsertModule, null, true);
        return response;
    }

    public ApiScenarioBatchOperationResponse batchCopy(ApiScenarioBatchCopyMoveRequest request, LogInsertModule logInsertModule) {
        this.checkTargetModule(request.getTargetModuleId(), request.getProjectId());

        List<String> scenarioIds = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return new ApiScenarioBatchOperationResponse();
        }
        request.setSelectIds(scenarioIds);
        AtomicLong nextScenarioPos = new AtomicLong(getNextOrder(request.getProjectId()));
        ApiScenarioBatchOperationResponse response =
                ApiScenarioBatchOperationUtils.executeWithBatchOperationResponse(scenarioIds, sublist -> copyAndInsert(sublist, nextScenarioPos, request, logInsertModule.getOperator()));
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(), OperationLogType.ADD.name(), logInsertModule, null, true);
        return response;
    }

    private String getScenarioCopyName(String oldName, long oldNum, long newNum, String moduleId) {
        String newScenarioName = oldName;
        if (!StringUtils.startsWith(newScenarioName, "copy_")) {
            newScenarioName = "copy_" + newScenarioName;
        }
        // 限制名称长度 （数据库里最大的长度是255，这里判断超过250时截取到200附近）
        if (newScenarioName.length() > 250) {
            newScenarioName = newScenarioName.substring(0, 200) + "...";
        }
        if (StringUtils.endsWith(newScenarioName, "_" + oldNum)) {
            newScenarioName = StringUtils.substringBeforeLast(newScenarioName, "_" + oldNum);
        }
        newScenarioName = newScenarioName + "_" + newNum;
        int indexLength = 1;
        while (true) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andModuleIdEqualTo(moduleId).andNameEqualTo(newScenarioName);
            if (apiScenarioMapper.countByExample(example) == 0) {
                break;
            } else {
                newScenarioName = newScenarioName + "_" + indexLength;
                indexLength++;
            }
        }
        return newScenarioName;
    }

    private ApiScenarioBatchOperationResponse move(List<String> scenarioIds, ApiScenarioBatchCopyMoveRequest request, long moveTime, String operator) {
        ApiScenarioBatchOperationResponse response = new ApiScenarioBatchOperationResponse();

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(scenarioIds);
        List<ApiScenario> operationList = apiScenarioMapper.selectByExample(example);
        List<String> updateScenarioIds = new ArrayList<>();
        operationList.forEach(apiScenario -> {

            if (StringUtils.equals(apiScenario.getModuleId(), request.getTargetModuleId())) {
                response.addErrorData(Translator.get("api_scenario_exist"), apiScenario.getId(), apiScenario.getNum(), apiScenario.getName());
            } else {
                ApiScenarioExample checkExample = new ApiScenarioExample();
                // 统一模块下名称不能重复
                checkExample.createCriteria()
                        .andNameEqualTo(apiScenario.getName())
                        .andDeletedEqualTo(false)
                        .andProjectIdEqualTo(apiScenario.getProjectId())
                        .andModuleIdEqualTo(request.getTargetModuleId());
                if (apiScenarioMapper.countByExample(checkExample) > 0) {
                    response.addErrorData(Translator.get("api_scenario.name.repeat"), apiScenario.getId(), apiScenario.getNum(), apiScenario.getName());
                } else {
                    updateScenarioIds.add(apiScenario.getId());
                    response.addSuccessData(apiScenario.getId(), apiScenario.getNum(), apiScenario.getName());
                }
            }
        });
        if (CollectionUtils.isNotEmpty(updateScenarioIds)) {
            ApiScenario moveModule = new ApiScenario();
            moveModule.setModuleId(request.getTargetModuleId());
            moveModule.setUpdateTime(moveTime);
            moveModule.setUpdateUser(operator);
            example.clear();
            example.createCriteria().andIdIn(updateScenarioIds);
            apiScenarioMapper.updateByExampleSelective(moveModule, example);
        }

        return response;
    }

    private ApiScenarioBatchOperationResponse copyAndInsert(List<String> scenarioIds, AtomicLong nextScenarioPos, ApiScenarioBatchCopyMoveRequest request, String operator) {
        ApiScenarioBatchOperationResponse response = new ApiScenarioBatchOperationResponse();

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(scenarioIds);
        List<ApiScenario> operationList = apiScenarioMapper.selectByExample(example);
        List<String> operationIdList = operationList.stream().map(ApiScenario::getId).collect(Collectors.toList());

        Map<String, ApiScenarioBlob> apiScenarioBlobMap = this.selectBlobByScenarioIds(operationIdList).stream().collect(Collectors.toMap(ApiScenarioBlob::getId, Function.identity()));
        Map<String, List<ApiFileResource>> apiFileResourceMap = this.selectApiFileResourceByScenarioIds(operationIdList).stream().collect(Collectors.groupingBy(ApiFileResource::getResourceId));
        Map<String, List<ApiScenarioStep>> apiScenarioStepMap = this.selectStepByScenarioIds(operationIdList).stream().collect(Collectors.groupingBy(ApiScenarioStep::getScenarioId));
        Map<String, ApiScenarioStepBlob> apiScenarioStepBlobMap = this.selectStepBlobByScenarioIds(operationIdList).stream().collect(Collectors.toMap(ApiScenarioStepBlob::getId, Function.identity()));

        List<ApiScenario> insertApiScenarioList = new ArrayList<>();
        List<ApiScenarioBlob> insertApiScenarioBlobList = new ArrayList<>();
        List<ApiScenarioStep> insertApiScenarioStepList = new ArrayList<>();
        List<ApiScenarioStepBlob> insertApiScenarioStepBlobList = new ArrayList<>();
        List<ApiFileResource> insertApiFileResourceList = new ArrayList<>();

        MinioRepository minioRepository = CommonBeanFactory.getBean(MinioRepository.class);

        for (ApiScenario apiScenario : operationList) {
            ApiScenario copyScenario = new ApiScenario();
            BeanUtils.copyBean(copyScenario, apiScenario);
            copyScenario.setId(IDGenerator.nextStr());
            copyScenario.setNum(getNextNum(copyScenario.getProjectId()));
            copyScenario.setPos(nextScenarioPos.getAndAdd(DEFAULT_NODE_INTERVAL_POS));
            copyScenario.setModuleId(request.getTargetModuleId());
            copyScenario.setName(this.getScenarioCopyName(copyScenario.getName(), apiScenario.getNum(), copyScenario.getNum(), request.getTargetModuleId()));
            copyScenario.setCreateUser(operator);
            copyScenario.setUpdateUser(operator);
            copyScenario.setCreateTime(System.currentTimeMillis());
            copyScenario.setUpdateTime(System.currentTimeMillis());
            copyScenario.setRefId(copyScenario.getId());
            copyScenario.setLastReportStatus(StringUtils.EMPTY);
            copyScenario.setRequestPassRate("0");

            insertApiScenarioList.add(copyScenario);

            ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMap.get(apiScenario.getId());
            if (apiScenarioBlob != null) {
                ApiScenarioBlob copyApiScenarioBlob = new ApiScenarioBlob();
                BeanUtils.copyBean(copyApiScenarioBlob, apiScenarioBlob);
                copyApiScenarioBlob.setId(copyScenario.getId());
                insertApiScenarioBlobList.add(copyApiScenarioBlob);
            }

            List<ApiFileResource> apiFileResourceList = apiFileResourceMap.get(apiScenario.getId());
            if (CollectionUtils.isNotEmpty(apiFileResourceList)) {
                apiFileResourceList.forEach(apiFileResource -> {
                    ApiFileResource copyApiFileResource = new ApiFileResource();
                    BeanUtils.copyBean(copyApiFileResource, apiFileResource);
                    copyApiFileResource.setResourceId(copyScenario.getId());
                    insertApiFileResourceList.add(copyApiFileResource);

                    try {
                        FileCopyRequest fileCopyRequest = new FileCopyRequest();
                        fileCopyRequest.setCopyFolder(DefaultRepositoryDir.getApiScenarioDir(apiScenario.getProjectId(), apiScenario.getId()));
                        fileCopyRequest.setCopyfileName(copyApiFileResource.getFileId());
                        fileCopyRequest.setFileName(copyApiFileResource.getFileId());
                        fileCopyRequest.setFolder(DefaultRepositoryDir.getApiScenarioDir(copyScenario.getProjectId(), copyScenario.getId()));
                        assert minioRepository != null;
                        minioRepository.copyFile(fileCopyRequest);
                    } catch (Exception ignore) {
                    }

                });
            }

            List<ApiScenarioStep> stepList = apiScenarioStepMap.get(apiScenario.getId());
            Map<String, String> originStepIdMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(stepList)) {
                stepList.forEach(step -> {
                    String originStepId = step.getId();
                    ApiScenarioStep copyStep = new ApiScenarioStep();
                    BeanUtils.copyBean(copyStep, step);
                    copyStep.setId(IDGenerator.nextStr());
                    copyStep.setScenarioId(copyScenario.getId());
                    originStepIdMap.put(originStepId, copyStep.getId());
                    if (StringUtils.isNotBlank(step.getParentId()) && originStepIdMap.containsKey(step.getParentId())) {
                        copyStep.setParentId(originStepIdMap.get(step.getParentId()));
                    }
                    insertApiScenarioStepList.add(copyStep);

                    //这块的批量复制不处理csv文件和场景的配置信息

                    ApiScenarioStepBlob stepBlob = apiScenarioStepBlobMap.get(step.getId());
                    if (stepBlob != null) {
                        ApiScenarioStepBlob copyStepBlob = new ApiScenarioStepBlob();
                        BeanUtils.copyBean(copyStepBlob, stepBlob);
                        copyStepBlob.setId(copyStep.getId());
                        copyStepBlob.setScenarioId(copyScenario.getId());
                        insertApiScenarioStepBlobList.add(copyStepBlob);
                    }
                });
            }
            response.addSuccessData(copyScenario.getId(), copyScenario.getNum(), copyScenario.getName());
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper mapper = sqlSession.getMapper(ApiScenarioMapper.class);

        insertApiScenarioList.forEach(mapper::insertSelective);

        if (CollectionUtils.isNotEmpty(insertApiScenarioBlobList)) {
            ApiScenarioBlobMapper blobMapper = sqlSession.getMapper(ApiScenarioBlobMapper.class);
            insertApiScenarioBlobList.forEach(blobMapper::insertSelective);
        }
        if (CollectionUtils.isNotEmpty(insertApiScenarioStepList)) {
            ApiScenarioStepMapper stepMapper = sqlSession.getMapper(ApiScenarioStepMapper.class);
            insertApiScenarioStepList.forEach(stepMapper::insertSelective);
        }
        if (CollectionUtils.isNotEmpty(insertApiScenarioStepBlobList)) {
            ApiScenarioStepBlobMapper stepBlobMapper = sqlSession.getMapper(ApiScenarioStepBlobMapper.class);
            insertApiScenarioStepBlobList.forEach(stepBlobMapper::insertSelective);

        }
        if (CollectionUtils.isNotEmpty(insertApiFileResourceList)) {
            apiFileResourceService.batchInsert(insertApiFileResourceList);
        }

        sqlSession.flushStatements();
        if (sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

        response.setSuccess(insertApiScenarioList.size());
        return response;
    }


    public List<ApiScenarioBlob> selectBlobByScenarioIds(@NotEmpty List<String> scenarioIds) {
        ApiScenarioBlobExample example = new ApiScenarioBlobExample();
        example.createCriteria().andIdIn(scenarioIds);
        return apiScenarioBlobMapper.selectByExampleWithBLOBs(example);
    }

    public List<ApiScenarioStep> selectStepByScenarioIds(@NotEmpty List<String> scenarioIds) {
        ApiScenarioStepExample example = new ApiScenarioStepExample();
        example.createCriteria().andScenarioIdIn(scenarioIds);
        return apiScenarioStepMapper.selectByExample(example);
    }

    public List<ApiFileResource> selectApiFileResourceByScenarioIds(@NotEmpty List<String> scenarioIds) {
        return apiFileResourceService.selectByApiScenarioId(scenarioIds);
    }

    public List<ApiScenarioStepBlob> selectStepBlobByScenarioIds(@NotEmpty List<String> scenarioIds) {
        ApiScenarioStepBlobExample example = new ApiScenarioStepBlobExample();
        example.createCriteria().andScenarioIdIn(scenarioIds);
        return apiScenarioStepBlobMapper.selectByExampleWithBLOBs(example);
    }

    public ApiScenarioBatchOperationResponse batchGCOperation(ApiScenarioBatchRequest request, boolean isDeleteOperation, LogInsertModule logInsertModule) {
        List<String> scenarioIds = doSelectIds(request, !isDeleteOperation);
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return new ApiScenarioBatchOperationResponse();
        }

        long deleteTime = System.currentTimeMillis();

        ApiScenarioBatchOperationResponse response = ApiScenarioBatchOperationUtils.executeWithBatchOperationResponse(
                scenarioIds,
                sublist -> operationGC(sublist, isDeleteOperation, deleteTime, logInsertModule.getOperator()));
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(),
                isDeleteOperation ? OperationLogType.DELETE.name() : OperationLogType.RECOVER.name(), logInsertModule, OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO, false);
        apiScenarioNoticeService.batchSendNotice(scenarioIds, logInsertModule.getOperator(), request.getProjectId(), NoticeConstants.Event.DELETE);
        return response;
    }

    private ApiScenarioBatchOperationResponse operationGC(List<String> scenarioIds, boolean isDeleteOperation, long deleteTime, String operator) {
        ApiScenarioBatchOperationResponse response = new ApiScenarioBatchOperationResponse();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(scenarioIds).andDeletedEqualTo(!isDeleteOperation);

        List<ApiScenario> apiScenarioList = apiScenarioMapper.selectByExample(example);

        ApiScenario updateScenarioModel = new ApiScenario();
        updateScenarioModel.setDeleted(isDeleteOperation);
        updateScenarioModel.setDeleteUser(operator);
        updateScenarioModel.setDeleteTime(deleteTime);
        apiScenarioMapper.updateByExampleSelective(updateScenarioModel, example);

        for (ApiScenario scenario : apiScenarioList) {
            response.addSuccessData(scenario.getId(), scenario.getNum(), scenario.getName());

            //删除定时任务
            scheduleService.deleteByResourceId(scenario.getId(), ApiScenarioScheduleJob.class.getName());
        }
        return response;
    }

    public ApiScenarioBatchOperationResponse batchDelete(ApiScenarioBatchRequest request, LogInsertModule logInsertModule) {
        List<String> scenarioIds = doSelectIds(request, true);
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return new ApiScenarioBatchOperationResponse();
        }

        ApiScenarioBatchOperationResponse response = ApiScenarioBatchOperationUtils.executeWithBatchOperationResponse(
                scenarioIds,
                sublist -> delete(sublist, request.getProjectId(), logInsertModule.getOperator()));
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(), OperationLogType.DELETE.name(), logInsertModule, OperationLogModule.API_TEST_SCENARIO_RECYCLE, false);
        return response;
    }


    public void deleteScheduleConfig(String scenarioId) {
        scheduleService.deleteByResourceId(scenarioId, ApiScenarioScheduleJob.getJobKey(scenarioId), ApiScenarioScheduleJob.getTriggerKey(scenarioId));
    }

    public String scheduleConfig(ApiScenarioScheduleConfigRequest scheduleRequest, String operator) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(scheduleRequest.getScenarioId());
        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId(apiScenario.getId())
                .key(apiScenario.getId())
                .projectId(apiScenario.getProjectId())
                .name(apiScenario.getName())
                .enable(scheduleRequest.isEnable())
                .cron(scheduleRequest.getCron())
                .resourceType(ScheduleResourceType.API_SCENARIO.name())
                .config(JSON.toJSONString(scheduleRequest.getConfig()))
                .build();

        return scheduleService.scheduleConfig(
                scheduleConfig,
                ApiScenarioScheduleJob.getJobKey(apiScenario.getId()),
                ApiScenarioScheduleJob.getTriggerKey(apiScenario.getId()),
                ApiScenarioScheduleJob.class,
                operator);
    }

    public List<ApiScenarioAssociationDTO> getAssociationPage(ApiScenarioAssociationPageRequest request) {
        List<ApiScenarioAssociationDTO> list = extApiScenarioMapper.getAssociationPage(request);
        if (CollectionUtils.isNotEmpty(list)) {
            //获取所有的项目ID
            List<String> projectIds = list.stream().map(ApiScenarioAssociationDTO::getProjectId).distinct().toList();
            ProjectExample example = new ProjectExample();
            example.createCriteria().andIdIn(projectIds);
            List<Project> projects = projectMapper.selectByExample(example);
            Map<String, Project> projectMap = projects.stream().collect(Collectors.toMap(Project::getId, Function.identity()));
            list.forEach(apiScenarioAssociationDTO -> {
                apiScenarioAssociationDTO.setProjectName(projectMap.get(apiScenarioAssociationDTO.getProjectId()).getName());
            });
        }

        return list;
    }

    public List<ApiScenarioStepDTO> getSystemRequest(ApiScenarioSystemRequest request) {
        //分批处理  如果是api的处理api的  构造步骤数据
        List<ApiScenarioStepDTO> steps = new ArrayList<>();
        ScenarioSystemRequest apiRequest = request.getApiRequest();
        ScenarioSystemRequest caseRequest = request.getCaseRequest();
        ScenarioSystemRequest scenarioRequest = request.getScenarioRequest();

        if (ObjectUtils.isNotEmpty(apiRequest)) {
            getApiStep(request, apiRequest, steps);
        }
        if (ObjectUtils.isNotEmpty(caseRequest)) {
            getCaseStep(request, caseRequest, steps);
        }
        if (ObjectUtils.isNotEmpty(scenarioRequest)) {
            getScenarioStep(request, scenarioRequest, steps);
        }
        return steps;
    }

    private void getScenarioStep(ApiScenarioSystemRequest request, ScenarioSystemRequest scenarioRequest, List<ApiScenarioStepDTO> steps) {
        if (CollectionUtils.isNotEmpty(scenarioRequest.getModuleIds())) {
            scenarioRequest.getSelectedIds().addAll(extApiScenarioMapper.getIdsByModules(scenarioRequest));
        }
        scenarioRequest.getSelectedIds().removeAll(scenarioRequest.getUnselectedIds());
        List<@NotBlank String> allScenario = scenarioRequest.getSelectedIds().stream().distinct().toList();
        allScenario.forEach(item -> {
            ApiScenarioDetail apiScenarioDetail = get(item);
            ApiScenarioStepDTO step = new ApiScenarioStepDTO();
            step.setStepType(ApiScenarioStepType.API_SCENARIO.name());
            step.setName(apiScenarioDetail.getName());
            step.setResourceId(apiScenarioDetail.getId());
            step.setRefType(request.getRefType());
            step.setProjectId(apiScenarioDetail.getProjectId());
            step.setResourceNum(apiScenarioDetail.getNum().toString());
            step.setVersionId(apiScenarioDetail.getVersionId());
            step.setChildren(apiScenarioDetail.getSteps());
            steps.add(step);
        });
    }

    private void getCaseStep(ApiScenarioSystemRequest request, ScenarioSystemRequest caseRequest, List<ApiScenarioStepDTO> steps) {
        if (CollectionUtils.isNotEmpty(caseRequest.getModuleIds()) && CollectionUtils.isNotEmpty(caseRequest.getProtocols())) {
            caseRequest.getSelectedIds().addAll(extApiTestCaseMapper.getIdsByModules(caseRequest));
        }
        caseRequest.getSelectedIds().removeAll(caseRequest.getUnselectedIds());
        List<@NotBlank String> allCaseIds = caseRequest.getSelectedIds().stream().distinct().toList();
        SubListUtils.dealForSubList(allCaseIds, 100, sublist -> {
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(sublist);
            List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
            apiTestCases.forEach(item -> {
                ApiScenarioStepDTO step = new ApiScenarioStepDTO();
                step.setStepType(ApiScenarioStepType.API_CASE.name());
                step.setName(item.getName());
                step.setResourceId(item.getId());
                step.setRefType(request.getRefType());
                step.setProjectId(item.getProjectId());
                step.setResourceNum(item.getNum().toString());
                step.setVersionId(item.getVersionId());
                steps.add(step);
            });
        });
    }

    private void getApiStep(ApiScenarioSystemRequest request, ScenarioSystemRequest apiRequest, List<ApiScenarioStepDTO> steps) {
        if (CollectionUtils.isNotEmpty(apiRequest.getModuleIds()) && CollectionUtils.isNotEmpty(apiRequest.getProtocols())) {
            apiRequest.getSelectedIds().addAll(extApiDefinitionMapper.getIdsByModules(apiRequest));
        }
        apiRequest.getSelectedIds().removeAll(apiRequest.getUnselectedIds());
        List<@NotBlank String> allApiIds = apiRequest.getSelectedIds().stream().distinct().toList();
        SubListUtils.dealForSubList(allApiIds, 100, sublist -> {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(sublist);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            apiDefinitions.forEach(item -> {
                ApiScenarioStepDTO step = new ApiScenarioStepDTO();
                step.setStepType(ApiScenarioStepType.API.name());
                step.setName(item.getName());
                step.setResourceId(item.getId());
                step.setRefType(request.getRefType());
                step.setProjectId(item.getProjectId());
                step.setResourceNum(item.getNum().toString());
                step.setVersionId(item.getVersionId());
                steps.add(step);
            });
        });
    }

    @Override
    public void updatePos(String id, long pos) {
        extApiScenarioMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> posIds = extApiScenarioMapper.selectIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtApiScenarioMapper batchUpdateMapper = sqlSession.getMapper(ExtApiScenarioMapper.class);
        for (int i = 0; i < posIds.size(); i++) {
            batchUpdateMapper.updatePos(posIds.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public void moveNode(PosRequest posRequest) {
        NodeMoveRequest request = super.getNodeMoveRequest(posRequest, true);
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                posRequest.getProjectId(),
                request,
                extApiScenarioMapper::selectDragInfoById,
                extApiScenarioMapper::selectNodeByPosOperator
        );
        this.sort(sortDTO);
    }

    public List<ExecuteReportDTO> getExecuteList(ExecutePageRequest request) {
        return apiTestCaseService.getExecuteList(request);
    }

    public List<OperationHistoryDTO> operationHistoryList(OperationHistoryRequest request) {
        return operationHistoryService.listWidthTable(request, SCENARIO_TABLE);
    }

    public void handleStepFileAssociationUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        // 查询步骤详情
        ApiScenarioStepBlob apiScenarioStepBlob = apiScenarioStepBlobMapper.selectByPrimaryKey(originFileAssociation.getSourceId());

        if (apiScenarioStepBlob == null || apiScenarioStepBlob.getContent() == null) {
            return;
        }

        AbstractMsTestElement msTestElement = apiCommonService.getAbstractMsTestElement(apiScenarioStepBlob.getContent());
        if (msTestElement != null && msTestElement instanceof MsHTTPElement msHTTPElement) {
            List<ApiFile> updateFiles = apiCommonService.getApiFilesByFileId(originFileAssociation.getFileId(), msHTTPElement);
            // 替换文件的Id和name
            apiCommonService.replaceApiFileInfo(updateFiles, newFileMetadata);
            // 如果有需要更新的文件，则更新步骤详情
            if (CollectionUtils.isNotEmpty(updateFiles)) {
                apiScenarioStepBlob.setContent(ApiDataUtils.toJSONString(msTestElement).getBytes());
                apiScenarioStepBlobMapper.updateByPrimaryKeySelective(apiScenarioStepBlob);
            }
        }
    }

    public void handleScenarioFileAssociationUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        String scenarioId = originFileAssociation.getSourceId();
        // 查询步骤详情
        ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(originFileAssociation.getSourceId());

        if (apiScenarioBlob == null || apiScenarioBlob.getConfig() == null) {
            return;
        }

        List<CsvVariable> csvVariables = apiScenarioFileService.getCsvVariables(scenarioId);
        List<ApiFile> updateFiles = new ArrayList<>(csvVariables.size());
        for (CsvVariable csvVariable : csvVariables) {
            ApiFile apiFile = csvVariable.getFile();
            if (apiFile != null && StringUtils.equals(originFileAssociation.getFileId(), apiFile.getFileId())) {
                updateFiles.add(apiFile);
            }
        }
        if (CollectionUtils.isNotEmpty(updateFiles)) {
            // 替换文件的Id和name
            apiCommonService.replaceApiFileInfo(updateFiles, newFileMetadata);
            List<String> dbCsvIds = csvVariables.stream().map(CsvVariable::getId).toList();
            apiScenarioFileService.handleCsvDataUpdate(csvVariables, apiScenarioMapper.selectByPrimaryKey(originFileAssociation.getSourceId()), dbCsvIds);
        }
    }

    public String scenarioTransfer(ApiTransferRequest request, String userId) {
        String apiScenarioStepDir = DefaultRepositoryDir.getApiScenarioDir(request.getProjectId(), request.getSourceId());
        return apiFileResourceService.transfer(request, userId, apiScenarioStepDir);
    }

    public String stepTransfer(ApiTransferRequest request, String userId) {
        ApiScenarioStep apiScenarioStep = apiScenarioStepMapper.selectByPrimaryKey(request.getSourceId());
        if (apiScenarioStep == null) {
            return apiFileResourceService.transfer(request, userId, StringUtils.EMPTY);
        } else {
            String apiScenarioStepDir = DefaultRepositoryDir.getApiScenarioStepDir(request.getProjectId(), apiScenarioStep.getScenarioId(), request.getSourceId());
            return apiFileResourceService.transfer(request, userId, apiScenarioStepDir);
        }
    }

    public List<ReferenceDTO> getReference(ReferenceRequest request) {
        return extApiDefinitionMapper.getReference(request);
    }

    public ApiStepResourceInfo getStepResourceInfo(String resourceId, String resourceType) {
        ApiResourceType apiResourceType = EnumValidator.validateEnum(ApiResourceType.class, resourceType);
        ApiStepResourceInfo apiStepResourceInfo = null;
        switch (apiResourceType) {
            case API_SCENARIO ->
                    apiStepResourceInfo = getApiStepResourceInfo(apiScenarioMapper::selectByPrimaryKey, resourceId, (resourceInfo, apiScenario) -> {
                        resourceInfo.setNum(apiScenario.getNum());
                        resourceInfo.setName(apiScenario.getName());
                        resourceInfo.setDelete(apiScenario.getDeleted());
                        resourceInfo.setProjectId(apiScenario.getProjectId());
                    });
            case API ->
                    apiStepResourceInfo = getApiStepResourceInfo(apiDefinitionMapper::selectByPrimaryKey, resourceId, (resourceInfo, apiDefinition) -> {
                        resourceInfo.setNum(apiDefinition.getNum());
                        resourceInfo.setName(apiDefinition.getName());
                        resourceInfo.setDelete(apiDefinition.getDeleted());
                        resourceInfo.setProjectId(apiDefinition.getProjectId());
                    });
            case API_CASE ->
                    apiStepResourceInfo = getApiStepResourceInfo(apiTestCaseMapper::selectByPrimaryKey, resourceId, (resourceInfo, apiTestCase) -> {
                        resourceInfo.setNum(apiTestCase.getNum());
                        resourceInfo.setName(apiTestCase.getName());
                        resourceInfo.setDelete(apiTestCase.getDeleted());
                        resourceInfo.setProjectId(apiTestCase.getProjectId());
                    });
            default -> {
            }
        }
        Optional.ofNullable(apiStepResourceInfo).ifPresent(resourceInfo -> {
            Project project = projectMapper.selectByPrimaryKey(resourceInfo.getProjectId());
            resourceInfo.setProjectName(project.getName());
        });
        return apiStepResourceInfo;
    }

    private <T> ApiStepResourceInfo getApiStepResourceInfo(Function<String, T> getResourceFunc, String resourceId, BiConsumer<ApiStepResourceInfo, T> setParamFunc) {
        T resource = getResourceFunc.apply(resourceId);
        if (resource == null) {
            return null;
        }
        ApiStepResourceInfo apiStepResourceInfo = new ApiStepResourceInfo();
        apiStepResourceInfo.setId(resourceId);
        setParamFunc.accept(apiStepResourceInfo, resource);
        return apiStepResourceInfo;
    }

    public MetersphereApiScenarioExportResponse selectAndSortScenarioDetailWithIds(@Valid List<@NotBlank(message = "{id must not be blank}") String> scenarioIds, Map<String, String> moduleMap) {
        MetersphereApiScenarioExportResponse response = new MetersphereApiScenarioExportResponse();

        // 数据准备
        {
            ApiScenarioExample scenarioExample = new ApiScenarioExample();
            scenarioExample.createCriteria().andIdIn(scenarioIds);
            List<ApiScenario> exportApiScenarios = apiScenarioMapper.selectByExample(scenarioExample);

            // 获取所有步骤（包含引用的场景）
            List<ApiScenarioStepDTO> allSteps = buildScenarioSteps(scenarioIds)
                    .stream()
                    .distinct() // 这里可能存在多次引用相同场景，步骤可能会重复，去重
                    .collect(Collectors.toList());
            response.setScenarioStepList(allSteps);

            //查询引用的场景ID
            List<String> stepScenarioIds = allSteps.stream().filter(step -> isScenarioStep(step.getStepType())).map(ApiScenarioStepDTO::getResourceId).toList();

            // 查询所有场景的blob
            List<String> allScenarioIds = new ArrayList<>(scenarioIds);
            stepScenarioIds.forEach(stepScenarioId -> {
                if (!allScenarioIds.contains(stepScenarioId)) {
                    allScenarioIds.add(stepScenarioId);
                }
            });
            ApiScenarioBlobExample scenarioBlobExample = new ApiScenarioBlobExample();
            scenarioBlobExample.createCriteria().andIdIn(allScenarioIds);
            Map<String, ApiScenarioBlob> scenarioBlobMap = apiScenarioBlobMapper.selectByExampleWithBLOBs(scenarioBlobExample).stream().collect(Collectors.toMap(ApiScenarioBlob::getId, Function.identity()));
            // 场景CSV相关的信息
            ApiScenarioCsvExample apiScenarioCsvExample = new ApiScenarioCsvExample();
            apiScenarioCsvExample.createCriteria().andScenarioIdIn(allScenarioIds);
            response.setApiScenarioCsvList(apiScenarioCsvMapper.selectByExample(apiScenarioCsvExample));

            // 导出的场景
            response.setExportScenarioList(ApiScenarioUtils.parseApiScenarioDetail(exportApiScenarios, scenarioBlobMap, moduleMap));

            //步骤引用的场景
            if (CollectionUtils.isNotEmpty(stepScenarioIds)) {
                scenarioExample.clear();
                scenarioExample.createCriteria().andIdIn(stepScenarioIds);
                List<ApiScenario> otherScenarios = apiScenarioMapper.selectByExample(scenarioExample);
                response.setRelatedScenarioList(ApiScenarioUtils.parseApiScenarioDetail(otherScenarios, scenarioBlobMap, moduleMap));
            }

            response.setScenarioStepBlobMap(getStepDetailMap(allSteps));
        }

        return response;
    }

    public void batchScheduleConfig(ApiScenarioBatchScheduleConfigRequest request, String operator) {
        List<String> scenarioIds = doSelectIds(request, false);
        if (CollectionUtils.isNotEmpty(scenarioIds)) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(scenarioIds).andDeletedEqualTo(false);
            List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);

            if (CollectionUtils.isNotEmpty(apiScenarios)) {
                if (StringUtils.isBlank(request.getCron()) && request.getConfig() == null) {
                    List<String> operationIds = this.batchUpdateSchedule(apiScenarios, request.isEnable(), operator);

                    if (CollectionUtils.isNotEmpty(operationIds)) {
                        example.clear();
                        example.createCriteria().andIdIn(operationIds).andDeletedEqualTo(false);
                        apiScenarios = apiScenarioMapper.selectByExample(example);
                    }
                } else {
                    if (StringUtils.isBlank(request.getCron())) {
                        throw new MSException("Cron can not be null");
                    }
                    apiScenarios.forEach(apiScenario -> {
                        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                                .resourceId(apiScenario.getId())
                                .key(apiScenario.getId())
                                .projectId(apiScenario.getProjectId())
                                .name(apiScenario.getName())
                                .enable(request.isEnable())
                                .cron(request.getCron())
                                .resourceType(ScheduleResourceType.API_SCENARIO.name())
                                .config(JSON.toJSONString(request.getConfig()))
                                .build();

                        scheduleService.scheduleConfig(
                                scheduleConfig,
                                ApiScenarioScheduleJob.getJobKey(apiScenario.getId()),
                                ApiScenarioScheduleJob.getTriggerKey(apiScenario.getId()),
                                ApiScenarioScheduleJob.class,
                                operator);
                    });
                }
                apiScenarioLogService.batchScheduleConfigLog(request.getProjectId(), apiScenarios, operator);
            }
        }
    }

    private List<String> batchUpdateSchedule(List<ApiScenario> apiScenarioList, boolean isScheudleOpen, String userId) {
        //批量编辑定时任务
        List<String> scenarioIds = new ArrayList<>();
        for (ApiScenario apiScenario : apiScenarioList) {
            scenarioIds.addAll(
                    scheduleService.updateIfExist(apiScenario.getId(), isScheudleOpen, ApiScenarioScheduleJob.getJobKey(apiScenario.getId()),
                            ApiScenarioScheduleJob.getTriggerKey(apiScenario.getId()), ApiScenarioScheduleJob.class, userId)
            );
        }
        return scenarioIds;
    }

    // 场景统计相关
    public List<ApiScenarioDTO> calculateRate(List<String> ids) {
        List<ApiScenarioDTO> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<ApiExecResultDTO> scenarioExecResult = extApiScenarioReportMapper.selectExecResultByScenarioIds(ids);
            Map<String, List<ApiExecResultDTO>> scenarioReportMap = scenarioExecResult.stream().collect(Collectors.groupingBy(ApiExecResultDTO::getResourceId));

            for (String scenarioId : ids) {
                ApiScenarioDTO dto = new ApiScenarioDTO();
                dto.setId(scenarioId);

                List<ApiExecResultDTO> execResultDTOs = scenarioReportMap.get(scenarioId);
                if (CollectionUtils.isNotEmpty(execResultDTOs)) {
                    int all = execResultDTOs.size();
                    int passCount = 0;
                    for (ApiExecResultDTO execResultDTO : execResultDTOs) {
                        if (StringUtils.equals(execResultDTO.getExecResult(), ResultStatus.SUCCESS.name())) {
                            passCount++;
                        }
                    }
                    dto.setExecPassRate(CalculateUtils.reportPercentage(passCount, all));
                } else {
                    dto.setExecPassRate("0%");
                }
                result.add(dto);
            }
        }
        return result;
    }

    public List<String> selectApiIdInCustomRequest(String projectId, List<ApiDefinition> apiDefinitions) {
        if (apiDefinitions == null || apiDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取项目中所有的自定义请求 ID
        List<String> requestIdList = extApiScenarioStepMapper.selectCustomRequestConfigByProjectId(projectId);
        if (requestIdList.isEmpty()) {
            return Collections.emptyList();
        }

        // 分批处理配置请求 ID 列表
        Map<String, Set<String>> methodPathMap = new HashMap<>();
        SubListUtils.dealForSubList(requestIdList, 200, batchIds -> {
            // 查询当前批次的 Blob 数据
            ApiScenarioStepBlobExample scenarioStepBlobExample = new ApiScenarioStepBlobExample();
            scenarioStepBlobExample.createCriteria().andIdIn(batchIds);
            List<ApiScenarioStepBlob> blobList = apiScenarioStepBlobMapper.selectByExampleWithBLOBs(scenarioStepBlobExample);

            // 解析并构建方法与路径映射，处理完一个批次后释放内存
            blobList.stream()
                    .map(ApiScenarioStepBlob::getContent)
                    .filter(Objects::nonNull)
                    .forEach(content -> {
                        try {
                            AbstractMsProtocolTestElement protocolTestElement = ApiDataUtils.parseObject(new String(content), AbstractMsProtocolTestElement.class);
                            if (protocolTestElement instanceof MsHTTPElement msHTTPElement) {
                                methodPathMap.computeIfAbsent(msHTTPElement.getMethod(), k -> new HashSet<>())
                                        .add(msHTTPElement.getPath());
                            }
                        } catch (Exception e) {
                            LogUtils.error(e);
                        }
                    });
            blobList.clear(); // 清空当前批次，释放内存
        });

        // 遍历 API 定义，匹配路径
        return apiDefinitions.stream()
                .filter(apiDefinition -> {
                    Set<String> customUrls = methodPathMap.get(apiDefinition.getMethod());
                    return customUrls != null && ApiDefinitionUtils.isUrlInList(apiDefinition.getPath(), customUrls);
                })
                .map(ApiDefinition::getId)
                .toList();
    }

    /**
     * 获取未保存过的步骤的详情
     * @param request
     * @return
     */
    public Object getUnsavedStepDetail(ApiScenarioStepDetailRequest request) {
        ApiScenarioStep step = apiScenarioStepMapper.selectByPrimaryKey(request.getId());
        if (step == null) {
            ApiScenarioStepDetailRequest getDetailRequest = BeanUtils.copyBean(new ApiScenarioStepDetailRequest(), request);
            if (StringUtils.isNotBlank(request.getCopyFromStepId())) {
                // 从已有步骤复制，则获取复制步骤的详情
                ApiScenarioStep copyFromStep = apiScenarioStepMapper.selectByPrimaryKey(request.getCopyFromStepId());
                getDetailRequest = BeanUtils.copyBean(new ApiScenarioStepDetailRequest(), copyFromStep);
            }
            return getStepDetail(getDetailRequest);
        }
        throw new MSException("步骤已存在，请调用 /api/scenario/step/get/{stepId} 接口获取详情");
    }

    public Map<String, String> copyStepFile(ApiScenarioStepFileCopyRequest request) {
        String sourceDir;
        if (BooleanUtils.isTrue(request.getIsTempFile())) {
            sourceDir = DefaultRepositoryDir.getSystemTempDir();
        } else {
            String stepType = request.getStepType();
            String resourceId = request.getResourceId();
            if (StringUtils.isNotBlank(request.getCopyFromStepId())) {
                // 从已有步骤复制
                ApiScenarioStep copyFromStep = apiScenarioStepMapper.selectByPrimaryKey(request.getCopyFromStepId());
                sourceDir = DefaultRepositoryDir.getApiScenarioStepDir(copyFromStep.getProjectId(),
                        copyFromStep.getScenarioId(), copyFromStep.getId());
            } else if (StringUtils.equals(stepType, ApiScenarioStepType.API.name())) {
                // 从接口定义复制
                ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(resourceId);
                sourceDir = DefaultRepositoryDir.getApiDefinitionDir(apiDefinition.getProjectId(),
                        apiDefinition.getId());
            } else if (StringUtils.equals(stepType, ApiScenarioStepType.API_CASE.name())) {
                // 从接口用例复制
                ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(resourceId);
                sourceDir = DefaultRepositoryDir.getApiCaseDir(apiTestCase.getProjectId(),
                        apiTestCase.getId());
            } else {
                throw new MSException("不支持的步骤类型");
            }
        }

        return apiCommonService.copyFiles2TempDir(request.getFileIds(), sourceDir);
    }
}
