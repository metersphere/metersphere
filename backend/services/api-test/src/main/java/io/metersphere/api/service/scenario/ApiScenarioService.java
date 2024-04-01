package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.definition.ExecutePageRequest;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.step.StepParser;
import io.metersphere.api.parser.step.StepParserFactory;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiScenarioBatchOperationUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.http.HttpConfigModuleMatchRule;
import io.metersphere.project.dto.environment.http.SelectModule;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.*;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.EnvironmentGroupExample;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRequest;
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
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.ApiPluginService;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
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
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private ApiScenarioCsvMapper apiScenarioCsvMapper;
    @Resource
    private ApiScenarioCsvStepMapper apiScenarioCsvStepMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private EnvironmentGroupService environmentGroupService;
    @Resource
    private ApiPluginService apiPluginService;
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
    private ApiDefinitionModuleService apiDefinitionModuleService;
    @Resource
    private OperationHistoryService operationHistoryService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;

    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String TAGS = "Tags";
    public static final String ENVIRONMENT = "Environment";
    private static final String SCENARIO_TABLE = "api_scenario";


    public List<ApiScenarioDTO> getScenarioPage(ApiScenarioPageRequest request) {
        //CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request, userId);
        //TODO  场景的自定义字段 等设计 不一定会有
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
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
                    item.setNextTriggerTime(getNextTriggerTime(schedule.getValue()));
                }
            }
        });
    }

    /**
     * 获取下次执行时间（getFireTimeAfter，也可以下下次...）
     *
     * @param cron cron表达式
     * @return 下次执行时间
     */
    private static Long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return null;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
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
        if (CollectionUtils.isEmpty(request.getTags())) {
            throw new MSException(Translator.get("tags_is_null"));
        }
        if (request.isAppend()) {
            Map<String, ApiScenario> scenarioMap = extApiScenarioMapper.getTagsByIds(ids, false)
                    .stream()
                    .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));
            if (MapUtils.isNotEmpty(scenarioMap)) {
                scenarioMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v.getTags())) {
                        List<String> orgTags = v.getTags();
                        orgTags.addAll(request.getTags());
                        v.setTags(orgTags.stream().distinct().toList());
                    } else {
                        v.setTags(request.getTags());
                    }
                    v.setUpdateTime(updateScenario.getUpdateTime());
                    v.setUpdateUser(updateScenario.getUpdateUser());
                    mapper.updateByPrimaryKeySelective(v);
                });
            }
        } else {
            updateScenario.setTags(request.getTags());
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
            if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
                ids.addAll(request.getSelectIds());
            }
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
        ApiScenario scenario = getAddApiScenario(request, creator);
        scenario.setStepTotal(request.getSteps().size());
        apiScenarioMapper.insert(scenario);

        // 更新场景配置
        ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
        apiScenarioBlob.setId(scenario.getId());
        apiScenarioBlob.setConfig(JSON.toJSONString(request.getScenarioConfig()).getBytes());
        apiScenarioBlobMapper.insert(apiScenarioBlob);

        // 插入步骤
        if (CollectionUtils.isNotEmpty(request.getSteps())) {
            // 获取待添加的步骤
            List<ApiScenarioCsvStep> csvSteps = new ArrayList<>();
            List<ApiScenarioStep> steps = getApiScenarioSteps(null, request.getSteps(), csvSteps);
            steps.forEach(step -> step.setScenarioId(scenario.getId()));
            // 处理特殊的步骤详情
            addSpecialStepDetails(request.getSteps(), request.getStepDetails());
            List<ApiScenarioStepBlob> apiScenarioStepBlobs = getUpdateStepBlobs(steps, request.getStepDetails());
            apiScenarioStepBlobs.forEach(step -> step.setScenarioId(scenario.getId()));

            if (CollectionUtils.isNotEmpty(steps)) {
                apiScenarioStepMapper.batchInsert(steps);
            }

            if (CollectionUtils.isNotEmpty(apiScenarioStepBlobs)) {
                apiScenarioStepBlobMapper.batchInsert(apiScenarioStepBlobs);
            }

            saveStepCsv(steps, csvSteps);
        }

        // 处理步骤文件
        handleStepFiles(request, creator, scenario);

        // 处理场景文件，csv等
        handScenarioFiles(request, creator, scenario);
        return scenario;
    }

    private void handScenarioFiles(ApiScenarioAddRequest request, String creator, ApiScenario scenario) {
        ResourceAddFileParam fileParam = request.getFileParam();
        if (fileParam == null) {
            return;
        }
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(scenario.getId(), scenario.getProjectId(), creator);
        resourceUpdateRequest.setUploadFileIds(fileParam.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(fileParam.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);
        if (request.getScenarioConfig() != null
                && request.getScenarioConfig().getVariable() != null) {
            saveCsv(request.getScenarioConfig().getVariable().getCsvVariables(), resourceUpdateRequest);
        }
    }

    private void handleStepFiles(ApiScenarioAddRequest request, String creator, ApiScenario scenario) {
        Map<String, ResourceAddFileParam> stepFileParam = request.getStepFileParam();
        if (MapUtils.isNotEmpty(stepFileParam)) {
            stepFileParam.forEach((stepId, fileParam) -> {
                // 处理步骤文件
                ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(stepId, scenario.getProjectId(), creator);
                resourceUpdateRequest.setUploadFileIds(fileParam.getUploadFileIds());
                resourceUpdateRequest.setLinkFileIds(fileParam.getLinkFileIds());
                apiFileResourceService.addFileResource(resourceUpdateRequest);
            });
        }
    }

    private void saveStepCsv(List<ApiScenarioStep> steps, List<ApiScenarioCsvStep> csvSteps) {
        //获取所有的步骤id  然后删掉历史的关联关系
        List<String> stepIds = steps.stream().map(ApiScenarioStep::getId).toList();
        SubListUtils.dealForSubList(stepIds, 500, subList -> {
            ApiScenarioCsvStepExample csvStepExample = new ApiScenarioCsvStepExample();
            csvStepExample.createCriteria().andStepIdIn(subList);
            apiScenarioCsvStepMapper.deleteByExample(csvStepExample);
        });
        //插入csv步骤
        if (CollectionUtils.isNotEmpty(csvSteps)) {
            SubListUtils.dealForSubList(csvSteps, 100, subList -> apiScenarioCsvStepMapper.batchInsert(subList));
        }
    }

    private void saveCsv(List<CsvVariable> csvVariables, ApiFileResourceUpdateRequest resourceUpdateRequest) {
        //进行比较一下  哪些是已存在的  那些是新上传的
        //查询已经存在的fileId   这里直接过滤所有的数据  拿到新上传的本地文件id  和已经存在的文件id   关联的文件id  关于新的  需要删除的 已经存在的
        ApiScenarioCsvExample apiScenarioCsvExample = new ApiScenarioCsvExample();
        apiScenarioCsvExample.createCriteria().andScenarioIdEqualTo(resourceUpdateRequest.getResourceId());
        List<ApiScenarioCsv> dbFileIds = apiScenarioCsvMapper.selectByExample(apiScenarioCsvExample);
        //取出所有的fileId Association为false的数据
        List<String> dbLocalFileIds = dbFileIds.stream().filter(c -> BooleanUtils.isFalse(c.getAssociation())).map(ApiScenarioCsv::getFileId).toList();
        //取出所有的fileId Association为true的数据
        List<String> dbRefFileIds = dbFileIds.stream().filter(c -> BooleanUtils.isTrue(c.getAssociation())).map(ApiScenarioCsv::getFileId).toList();

        //获取传的所有Association为false的数据
        List<String> localFileIds = csvVariables.stream().filter(c -> BooleanUtils.isFalse(c.getAssociation())).map(CsvVariable::getFileId).toList();
        //获取传的所有Association为true的数据
        List<String> refFileIds = csvVariables.stream().filter(c -> BooleanUtils.isTrue(c.getAssociation())).map(CsvVariable::getFileId).toList();

        //取交集  交集数据是已存在的  不需要重新上传 和处理关联关系  但是需要更新apiScenarioCsv表的数据
        List<String> intersectionLocal = ListUtils.intersection(dbLocalFileIds, localFileIds);
        //取差集 dbFileIds和 intersection的差集是需要删除的数据  本地数据需要删除的数据
        List<String> deleteLocals = ListUtils.subtract(dbLocalFileIds, intersectionLocal);
        resourceUpdateRequest.setDeleteFileIds(deleteLocals);
        List<String> addLocal = ListUtils.subtract(localFileIds, intersectionLocal);
        resourceUpdateRequest.setUploadFileIds(addLocal);
        //获取  关联文件的交集
        List<String> intersectionRef = ListUtils.intersection(dbRefFileIds, refFileIds);
        //获取删除的
        List<String> deleteRefs = ListUtils.subtract(dbRefFileIds, intersectionRef);
        List<String> addRef = ListUtils.subtract(refFileIds, intersectionRef);
        resourceUpdateRequest.setLinkFileIds(addRef);
        resourceUpdateRequest.setUnLinkFileIds(deleteRefs);
        //删除不存在的数据
        deleteCsvResource(resourceUpdateRequest);

        addCsvResource(resourceUpdateRequest, csvVariables);
    }

    private void addCsvResource(ApiFileResourceUpdateRequest resourceUpdateRequest,
                                List<CsvVariable> csvVariables) {
        List<ApiScenarioCsv> addData = new ArrayList<>();
        List<ApiScenarioCsv> updateData = new ArrayList<>();
        List<String> addFileIds = resourceUpdateRequest.getUploadFileIds();
        List<String> linkFileIds = resourceUpdateRequest.getLinkFileIds();
        Map<String, String> refFilesMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(linkFileIds)) {
            //根据fileId查询文件名
            List<FileMetadata> fileList = fileMetadataService.selectByList(linkFileIds);
            if (CollectionUtils.isNotEmpty(fileList)) {
                //生成map  key为文件id  值为文件名称 文件名称为name.类型
                refFilesMap = fileList.stream().collect(Collectors.toMap(FileMetadata::getId, f -> f.getName() + "." + f.getType()));
            }
            fileAssociationService.association(resourceUpdateRequest.getResourceId(), FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO, linkFileIds,
                    apiFileResourceService.createFileLogRecord(resourceUpdateRequest.getOperator(), resourceUpdateRequest.getProjectId(), OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO));
        }
        Map<String, String> finalRefFilesMap = refFilesMap;
        // 添加文件与接口的关联关系
        Map<String, String> addFileMap = new HashMap<>();
        csvVariables.forEach(item -> {
            ApiScenarioCsv scenarioCsv = new ApiScenarioCsv();
            BeanUtils.copyBean(scenarioCsv, item);
            scenarioCsv.setScenarioId(resourceUpdateRequest.getResourceId());
            scenarioCsv.setProjectId(resourceUpdateRequest.getProjectId());
            // uploadFileIds里包含的数据  全部需要上传到minio上或者需要重新建立关联关系
            if (BooleanUtils.isFalse(item.getAssociation())
                    && CollectionUtils.isNotEmpty(addFileIds)
                    && addFileIds.contains(item.getFileId())) {
                scenarioCsv.setFileName(apiFileResourceService.getTempFileNameByFileId(item.getFileId()));
                addFileMap.put(item.getFileId(), scenarioCsv.getId());
            } else if (BooleanUtils.isTrue(item.getAssociation())
                    && finalRefFilesMap.containsKey(item.getFileId())
                    && CollectionUtils.isNotEmpty(linkFileIds)
                    && linkFileIds.contains(item.getFileId())) {
                scenarioCsv.setFileName(finalRefFilesMap.get(item.getFileId()));
            }
            if (StringUtils.isBlank(item.getId())) {
                scenarioCsv.setId(IDGenerator.nextStr());
                addData.add(scenarioCsv);
            } else {
                updateData.add(scenarioCsv);
            }
        });
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioCsvMapper mapper = sqlSession.getMapper(ApiScenarioCsvMapper.class);
        if (CollectionUtils.isNotEmpty(updateData)) {
            //更新apiScenarioCsv表
            updateData.forEach(mapper::updateByPrimaryKeySelective);
        }
        if (CollectionUtils.isNotEmpty(addData)) {
            //插入apiScenarioCsv表
            addData.forEach(mapper::insertSelective);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        if (MapUtils.isNotEmpty(addFileMap)) {
            // 上传文件到对象存储
            apiFileResourceService.uploadFileResource(resourceUpdateRequest.getFolder(), addFileMap);
        }
    }

    private void deleteCsvResource(ApiFileResourceUpdateRequest resourceUpdateRequest) {
        // 处理本地上传文件
        List<String> deleteFileIds = resourceUpdateRequest.getDeleteFileIds();
        ApiScenarioCsvExample example = new ApiScenarioCsvExample();
        ApiScenarioCsvStepExample stepExample = new ApiScenarioCsvStepExample();
        if (CollectionUtils.isNotEmpty(deleteFileIds)) {
            // 删除关联关系
            deleteFileIds.forEach(fileId -> {
                FileRequest request = new FileRequest();
                // 删除文件所在目录
                request.setFolder(resourceUpdateRequest.getFolder() + "/" + fileId);
                try {
                    FileCenter.getDefaultRepository().deleteFolder(request);
                } catch (Exception e) {
                    LogUtils.error(e);
                }
            });

            example.createCriteria()
                    .andScenarioIdEqualTo(resourceUpdateRequest.getResourceId())
                    .andFileIdIn(deleteFileIds);
            apiScenarioCsvMapper.deleteByExample(example);
            stepExample.createCriteria().andFileIdIn(deleteFileIds);
            apiScenarioCsvStepMapper.deleteByExample(stepExample);

        }
        List<String> unLinkFileIds = resourceUpdateRequest.getUnLinkFileIds();
        // 处理关联文件
        if (CollectionUtils.isNotEmpty(unLinkFileIds)) {
            fileAssociationService.deleteBySourceIdAndFileIds(resourceUpdateRequest.getResourceId(), unLinkFileIds,
                    apiFileResourceService.createFileLogRecord(resourceUpdateRequest.getOperator(), resourceUpdateRequest.getProjectId(), resourceUpdateRequest.getLogModule()));
            example.clear();
            example.createCriteria()
                    .andScenarioIdEqualTo(resourceUpdateRequest.getResourceId())
                    .andFileIdIn(unLinkFileIds);
            apiScenarioCsvMapper.deleteByExample(example);
            stepExample.clear();
            stepExample.createCriteria().andFileIdIn(deleteFileIds);
            apiScenarioCsvStepMapper.deleteByExample(stepExample);
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

        // 处理步骤文件
        handleStepFiles(request, updater, originScenario);

        // 处理场景文件
        handleScenarioFiles(request, updater, originScenario);

        // 更新场景步骤
        updateApiScenarioStep(request, originScenario, updater);

        return scenario;
    }

    private void handleScenarioFiles(ApiScenarioUpdateRequest request, String updater, ApiScenario scenario) {
        // 处理场景文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(scenario.getId(), scenario.getProjectId(), updater);
        ResourceAddFileParam fileParam = request.getFileParam();
        if (fileParam != null) {
            resourceUpdateRequest = BeanUtils.copyBean(resourceUpdateRequest, fileParam);
            apiFileResourceService.updateFileResource(resourceUpdateRequest);
        }

        //处理csv变量
        if (request.getScenarioConfig() != null
                && request.getScenarioConfig().getVariable() != null) {
            saveCsv(request.getScenarioConfig().getVariable().getCsvVariables(), resourceUpdateRequest);
        } else {
            saveCsv(new ArrayList<>(), resourceUpdateRequest);
        }
    }

    private void handleStepFiles(ApiScenarioUpdateRequest request, String updater, ApiScenario scenario) {
        Map<String, ResourceUpdateFileParam> stepFileParam = request.getStepFileParam();
        if (MapUtils.isNotEmpty(stepFileParam)) {
            stepFileParam.forEach((stepId, fileParam) -> {
                // 处理步骤文件
                ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(stepId, scenario.getProjectId(), updater);
                resourceUpdateRequest = BeanUtils.copyBean(resourceUpdateRequest, fileParam);
                apiFileResourceService.addFileResource(resourceUpdateRequest);
            });
        }
    }

    /**
     * 更新场景步骤
     */
    private void updateApiScenarioStep(ApiScenarioUpdateRequest request, ApiScenario scenario, String userId) {
        // steps 不为 null 则修改
        if (request.getSteps() != null) {
            if (CollectionUtils.isEmpty(request.getSteps())) {
                // 如果是空数组，则删除所有步骤
                deleteStepByScenarioId(scenario.getId());
                deleteStepDetailByScenarioId(scenario.getId());
                return;
            }
            List<ApiScenarioCsvStep> scenarioCsvSteps = new ArrayList<>();
            // 获取待更新的步骤
            List<ApiScenarioStep> apiScenarioSteps = getApiScenarioSteps(null, request.getSteps(), scenarioCsvSteps);
            apiScenarioSteps.forEach(step -> step.setScenarioId(scenario.getId()));

            saveStepCsv(apiScenarioSteps, scenarioCsvSteps);
            // 获取待更新的步骤详情
            addSpecialStepDetails(request.getSteps(), request.getStepDetails());
            List<ApiScenarioStepBlob> updateStepBlobs = getUpdateStepBlobs(apiScenarioSteps, request.getStepDetails());
            updateStepBlobs.forEach(step -> step.setScenarioId(scenario.getId()));

            List<String> stepIds = apiScenarioSteps.stream().map(ApiScenarioStep::getId).collect(Collectors.toList());
            List<String> originStepIds = extApiScenarioStepMapper.getStepIdsByScenarioId(scenario.getId());
            List<String> deleteStepIds = ListUtils.subtract(originStepIds, stepIds);

            // 步骤表-全部先删除再插入
            deleteStepByScenarioId(scenario.getId());
            apiScenarioStepMapper.batchInsert(apiScenarioSteps);

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

    private ApiScenarioStepBlob getApiScenarioStepBlob(String stepId, Object stepDetail) {
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
    private List<ApiScenarioStepBlob> getUpdateStepBlobs(List<ApiScenarioStep> apiScenarioSteps, Map<String, Object> stepDetails) {
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

    private boolean isRefOrPartialRef(String refType) {
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
    private List<ApiScenarioStep> getApiScenarioSteps(ApiScenarioStepCommonDTO parent,
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

            if (CollectionUtils.isNotEmpty(step.getCsvFileIds())) {
                //如果是csv文件  需要保存到apiScenarioCsvStep表中
                step.getCsvFileIds().forEach(fileId -> {
                    ApiScenarioCsvStep csvStep = new ApiScenarioCsvStep();
                    csvStep.setId(IDGenerator.nextStr());
                    csvStep.setStepId(apiScenarioStep.getId());
                    csvStep.setFileId(fileId);
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
    private void addSpecialStepDetails(List<ApiScenarioStepRequest> steps, Map<String, Object> stepDetails) {
        if (CollectionUtils.isEmpty(steps)) {
            return;
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
                if (isApiOrCase(step) && BooleanUtils.isTrue(((ApiScenarioStepRequest) step).getIsNew())) {
                    // 处理 isNew 的用例步骤
                    if (isApi(step.getStepType())) {
                        isNewApiResourceMap.put(step.getId(), step.getResourceId());
                    } else {
                        isNewApiCaseResourceMap.put(step.getId(), step.getResourceId());
                    }
                } else if (StringUtils.isNotBlank(stepRequest.getCopyFromStepId())) {
                    // 处理 copyFromStep 的情况
                    if (stepDetails.containsKey(stepRequest.getCopyFromStepId())) {
                        // 如果有传 copyFromStepId 的详情，优先使用
                        stepDetails.put(step.getId(), stepDetails.get(stepRequest.getCopyFromStepId()));
                    } else {
                        // 没有传，则记录ID，后续统一查库
                        copyFromStepIdMap.put(stepRequest.getId(), stepRequest.getCopyFromStepId());
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
            Set<String> enableStepSet = getEnableStepSet(step.getChildren());
            PartialRefStepDetail stepDetail = new PartialRefStepDetail();
            stepDetail.setEnableStepIds(enableStepSet);
            stepDetails.put(step.getId(), stepDetail);
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
    private Set<String> getEnableStepSet(List<? extends ApiScenarioStepCommonDTO> steps) {
        Set<String> enableSteps = new HashSet<>();
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptySet();
        }
        for (ApiScenarioStepCommonDTO step : steps) {
            if (BooleanUtils.isTrue(step.getEnable())) {
                enableSteps.add(step.getId());
            }
            // 完全引用和部分引用不解析子步骤
            if (!isRefOrPartialRef(step.getRefType())) {
                // 获取子步骤中 enable 的步骤
                enableSteps.addAll(getEnableStepSet(step.getChildren()));
            }
        }
        return enableSteps;
    }

    private ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiScenarioDir = DefaultRepositoryDir.getApiScenarioDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiScenarioDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_SCENARIO);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_SCENARIO_MANAGEMENT_SCENARIO);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_SCENARIO);
        return resourceUpdateRequest;
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
    }

    private void deleteCsvByScenarioId(String id) {
        ApiScenarioCsvExample example = new ApiScenarioCsvExample();
        example.createCriteria().andScenarioIdEqualTo(id);
        List<ApiScenarioCsv> apiScenarioCsv = apiScenarioCsvMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiScenarioCsv)) {
            List<String> fileIds = apiScenarioCsv.stream().map(ApiScenarioCsv::getFileId).toList();
            //删除关联关系
            ApiScenarioCsvStepExample stepExample = new ApiScenarioCsvStepExample();
            stepExample.createCriteria().andFileIdIn(fileIds);
            apiScenarioCsvStepMapper.deleteByExample(stepExample);
        }
        apiScenarioCsvMapper.deleteByExample(example);

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

    }


    public void recover(String id) {
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId(id);
        apiScenario.setDeleted(false);
        apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);
    }

    public void deleteToGc(String id) {
        checkResourceExist(id);
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId(id);
        apiScenario.setDeleted(true);
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
                .andProjectIdEqualTo(request.getProjectId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            throw new MSException(API_SCENARIO_EXIST);
        }
    }

    private ApiScenario checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(apiScenarioMapper.selectByPrimaryKey(id), "permission.system_api_scenario.name");
    }

    public String uploadTempFile(MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
    }

    public TaskRequestDTO debug(ApiScenarioDebugRequest request) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        boolean hasSave = apiScenario != null;

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(getScenarioConfig(request, hasSave));
        msScenario.setProjectId(request.getProjectId());

        // 处理特殊的步骤详情
        addSpecialStepDetails(request.getSteps(), request.getStepDetails());

        ApiScenarioParseTmpParam tmpParam = parse(msScenario, request.getSteps(), request);

        ApiResourceRunRequest runRequest = getApiResourceRunRequest(msScenario, tmpParam);

        TaskRequestDTO taskRequest = getTaskRequest(request.getReportId(), request.getId(), request.getProjectId(),
                apiExecuteService.getDebugRunModule(request.getFrontendDebug()));
        taskRequest.setSaveResult(false);
        taskRequest.setRealTime(true);
        taskRequest.setRequestCount(tmpParam.getRequestCount().get());

        ApiScenarioParamConfig parseConfig = getApiScenarioParamConfig(request, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setReportId(request.getReportId());

        return apiExecuteService.execute(runRequest, taskRequest, parseConfig);
    }

    public TaskRequestDTO run(String id, String reportId, String userId) {
        ApiScenarioDetail apiScenarioDetail = get(id);

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(apiScenarioDetail.getScenarioConfig());
        msScenario.setProjectId(apiScenarioDetail.getProjectId());

        ApiScenarioParseParam parseParam = new ApiScenarioParseParam();
        parseParam.setScenarioConfig(apiScenarioDetail.getScenarioConfig());
        parseParam.setStepDetails(Map.of());
        parseParam.setEnvironmentId(apiScenarioDetail.getEnvironmentId());
        parseParam.setGrouped(apiScenarioDetail.getGrouped());

        return executeRun(apiScenarioDetail, msScenario, apiScenarioDetail.getSteps(), parseParam, reportId, userId);
    }

    public TaskRequestDTO run(ApiScenarioDebugRequest request, String userId) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setRefType(ApiScenarioStepRefType.DIRECT.name());
        msScenario.setScenarioConfig(getScenarioConfig(request, true));
        msScenario.setProjectId(request.getProjectId());

        return executeRun(apiScenario, msScenario, request.getSteps(), request, request.getReportId(), userId);
    }

    public TaskRequestDTO executeRun(ApiScenario apiScenario,
                                     MsScenario msScenario,
                                     List<? extends ApiScenarioStepCommonDTO> steps,
                                     ApiScenarioParseParam parseParam,
                                     String reportId, String userId) {

        // 解析生成场景树，并保存临时变量
        ApiScenarioParseTmpParam tmpParam = parse(msScenario, steps, parseParam);

        ApiResourceRunRequest runRequest = getApiResourceRunRequest(msScenario, tmpParam);

        String poolId = apiExecuteService.getProjectApiResourcePoolId(apiScenario.getProjectId());

        TaskRequestDTO taskRequest = getTaskRequest(reportId, apiScenario.getId(), apiScenario.getProjectId(), ApiExecuteRunMode.RUN.name());
        taskRequest.getRunModeConfig().setPoolId(poolId);
        taskRequest.setSaveResult(true);
        taskRequest.getRunModeConfig().setEnvironmentId(parseParam.getEnvironmentId());
        taskRequest.setRequestCount(tmpParam.getRequestCount().get());

        if (StringUtils.isEmpty(taskRequest.getReportId())) {
            taskRequest.setRealTime(false);
            reportId = IDGenerator.nextStr();
            taskRequest.setReportId(reportId);
        } else {
            // 如果传了报告ID，则实时获取结果
            taskRequest.setRealTime(true);
        }

        ApiScenarioParamConfig parseConfig = getApiScenarioParamConfig(parseParam, tmpParam.getScenarioParseEnvInfo());
        parseConfig.setReportId(reportId);

        // 初始化报告
        initApiReport(apiScenario, parseParam.getEnvironmentId(), reportId, poolId, userId);

        // 初始化报告步骤
        initScenarioReportSteps(steps, taskRequest.getReportId());

        return apiExecuteService.execute(runRequest, taskRequest, parseConfig);
    }

    public boolean isRequestStep(ApiScenarioStepCommonDTO step) {
        return StringUtils.equalsAny(step.getStepType(), ApiScenarioStepType.API.name(), ApiScenarioStepType.API_CASE.name(), ApiScenarioStepType.CUSTOM_REQUEST.name());
    }

    /**
     * 预生成用例的执行报告
     *
     * @param apiScenario
     * @param poolId
     * @param userId
     * @return
     */
    public ApiScenarioRecord initApiReport(ApiScenario apiScenario, String envId, String reportId, String poolId, String userId) {
        // 初始化报告
        ApiScenarioReport scenarioReport = getScenarioReport(userId);
        scenarioReport.setId(reportId);
        scenarioReport.setTriggerMode(TaskTriggerMode.MANUAL.name());
        scenarioReport.setName(apiScenario.getName() + "_" + DateUtils.getTimeString(System.currentTimeMillis()));
        scenarioReport.setRunMode(ApiBatchRunMode.PARALLEL.name());
        scenarioReport.setPoolId(poolId);
        scenarioReport.setEnvironmentId(apiScenario.getEnvironmentId());
        scenarioReport.setEnvironmentId(envId);
        scenarioReport.setProjectId(apiScenario.getProjectId());

        // 创建报告和用例的关联关系
        ApiScenarioRecord scenarioRecord = getApiScenarioRecord(apiScenario, scenarioReport);

        apiScenarioReportService.insertApiScenarioReport(List.of(scenarioReport), List.of(scenarioRecord));
        return scenarioRecord;
    }

    /**
     * 初始化场景报告步骤
     *
     * @param steps
     * @param reportId
     */
    public void initScenarioReportSteps(List<? extends ApiScenarioStepCommonDTO> steps, String reportId) {
        List<ApiScenarioReportStep> scenarioReportSteps = getScenarioReportSteps(null, steps, reportId);
        apiScenarioReportService.insertApiScenarioReportStep(scenarioReportSteps);
    }

    /**
     * 获取场景报告步骤
     *
     * @param steps
     * @param reportId
     */
    public List<ApiScenarioReportStep> getScenarioReportSteps(String parentId, List<? extends ApiScenarioStepCommonDTO> steps, String reportId) {
        AtomicLong sort = new AtomicLong(1);
        List<ApiScenarioReportStep> scenarioReportSteps = new ArrayList<>();
        for (ApiScenarioStepCommonDTO step : steps) {
            ApiScenarioReportStep scenarioReportStep = getScenarioReportStep(step, reportId, sort.getAndIncrement());
            scenarioReportStep.setParentId(parentId);
            scenarioReportSteps.add(scenarioReportStep);
            List<? extends ApiScenarioStepCommonDTO> children = step.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                scenarioReportSteps.addAll(getScenarioReportSteps(step.getId(), children, reportId));
            }
        }
        return scenarioReportSteps;
    }

    private ApiScenarioReportStep getScenarioReportStep(ApiScenarioStepCommonDTO step, String reportId, long sort) {
        ApiScenarioReportStep scenarioReportStep = new ApiScenarioReportStep();
        scenarioReportStep.setReportId(reportId);
        scenarioReportStep.setStepId(step.getId());
        scenarioReportStep.setSort(sort);
        scenarioReportStep.setName(step.getName());
        scenarioReportStep.setStepType(step.getStepType());
        return scenarioReportStep;
    }

    public ApiScenarioRecord getApiScenarioRecord(ApiScenario apiScenario, ApiScenarioReport scenarioReport) {
        ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
        scenarioRecord.setApiScenarioId(apiScenario.getId());
        scenarioRecord.setApiScenarioReportId(scenarioReport.getId());
        return scenarioRecord;
    }

    public ApiScenarioReport getScenarioReport(String userId) {
        ApiScenarioReport scenarioReport = new ApiScenarioReport();
        scenarioReport.setId(IDGenerator.nextStr());
        scenarioReport.setDeleted(false);
        scenarioReport.setIntegrated(false);
        scenarioReport.setStatus(ApiReportStatus.PENDING.name());
        scenarioReport.setStartTime(System.currentTimeMillis());
        scenarioReport.setUpdateTime(System.currentTimeMillis());
        scenarioReport.setUpdateUser(userId);
        scenarioReport.setCreateUser(userId);
        return scenarioReport;
    }

    public ApiScenarioParamConfig getApiScenarioParamConfig(ApiScenarioParseParam request, ApiScenarioParseEnvInfo scenarioParseEnvInfo) {
        ApiScenarioParamConfig parseConfig = new ApiScenarioParamConfig();
        parseConfig.setTestElementClassPluginIdMap(apiPluginService.getTestElementPluginMap());
        parseConfig.setTestElementClassProtocalMap(apiPluginService.getTestElementProtocolMap());
        parseConfig.setGrouped(request.getGrouped());
        if (BooleanUtils.isTrue(request.getGrouped())) {
            // 设置环境组 map
            parseConfig.setProjectEnvMap(getProjectEnvMap(scenarioParseEnvInfo, request.getEnvironmentId()));
        } else {
            // 设置环境
            parseConfig.setEnvConfig(scenarioParseEnvInfo.getEnvMap().get(request.getEnvironmentId()));
        }
        return parseConfig;
    }

    public ApiResourceRunRequest getApiResourceRunRequest(MsScenario msScenario, ApiScenarioParseTmpParam tmpParam) {
        ApiResourceRunRequest runRequest = new ApiResourceRunRequest();
        runRequest.setRefResourceIds(tmpParam.getRefResourceIds());
        runRequest.setRefProjectIds(tmpParam.getRefProjectIds());
        runRequest.setTestElement(msScenario);
        return runRequest;
    }

    /**
     * 将步骤转换成场景树
     * 并保存临时变量
     *
     * @param msScenario
     * @param steps
     * @param parseParam
     * @return
     */
    public ApiScenarioParseTmpParam parse(MsScenario msScenario,
                                          List<? extends ApiScenarioStepCommonDTO> steps,
                                          ApiScenarioParseParam parseParam) {
        // 记录引用的资源ID
        Map<String, List<String>> refResourceMap = new HashMap<>();
        buildRefResourceIdMap(steps, refResourceMap);

        ApiScenarioParseTmpParam tmpParam = new ApiScenarioParseTmpParam();

        // 查询引用的资源详情
        tmpParam.setResourceDetailMap(getResourceDetailMap(refResourceMap));

        // 查询复制的步骤详情
        tmpParam.setStepDetailMap(getStepDetailMap(steps, parseParam.getStepDetails()));

        // 获取场景环境相关配置
        tmpParam.setScenarioParseEnvInfo(getScenarioParseEnvInfo(refResourceMap, parseParam.getEnvironmentId(), parseParam.getGrouped()));
        parseStep2MsElement(msScenario, steps, tmpParam);

        // 设置 HttpElement 的模块信息
        setApiDefinitionExecuteInfo(tmpParam.getStepTypeHttpElementMap());

        // 设置使用脚本前后置的公共脚本信息
        apiCommonService.setEnableCommonScriptProcessorInfo(tmpParam.getCommonElements());

        return tmpParam;
    }

    public TaskRequestDTO getTaskRequest(String reportId, String resourceId, String projectId, String runModule) {
        TaskRequestDTO taskRequest = apiExecuteService.getTaskRequest(reportId, resourceId, projectId);
        taskRequest.setResourceType(ApiResourceType.API_SCENARIO.name());
        taskRequest.setRunMode(runModule);
        return taskRequest;
    }

    /**
     * 设置 HttpElement 的模块信息
     * 用户环境中的模块过滤
     *
     * @param stepTypeHttpElementMap
     */
    private void setApiDefinitionExecuteInfo(Map<String, List<MsHTTPElement>> stepTypeHttpElementMap) {
        apiCommonService.setApiDefinitionExecuteInfo(stepTypeHttpElementMap.get(ApiScenarioStepType.API.name()), apiDefinitionService::getModuleInfoByIds);
        apiCommonService.setApiDefinitionExecuteInfo(stepTypeHttpElementMap.get(ApiScenarioStepType.API_CASE.name()), apiTestCaseService::getModuleInfoByIds);
    }

    /**
     * 设置脚本解析-环境相关参数
     */
    private ApiScenarioParseEnvInfo getScenarioParseEnvInfo(Map<String, List<String>> refResourceMap, String currentEnvId, Boolean isCurrentEnvGrouped) {
        List<String> apiScenarioIds = refResourceMap.get(ApiScenarioStepType.API_SCENARIO.name());
        List<String> envIds = new ArrayList<>();
        List<String> envGroupIds = new ArrayList<>();
        ApiScenarioParseEnvInfo envInfo = new ApiScenarioParseEnvInfo();

        if (BooleanUtils.isTrue(isCurrentEnvGrouped)) {
            envGroupIds.add(currentEnvId);
        } else {
            envIds.add(currentEnvId);
        }

        if (CollectionUtils.isNotEmpty(apiScenarioIds)) {
            Map<String, EnvironmentModeDTO> refScenarioEnvMap = new HashMap<>();
            List<ApiScenario> apiScenarios = getApiScenarioByIds(apiScenarioIds);
            for (ApiScenario scenario : apiScenarios) {
                EnvironmentModeDTO envMode = new EnvironmentModeDTO();
                envMode.setEnvironmentId(scenario.getEnvironmentId());
                envMode.setGrouped(scenario.getGrouped());
                if (BooleanUtils.isTrue(scenario.getGrouped())) {
                    // 记录环境组ID
                    envGroupIds.add(scenario.getEnvironmentId());
                } else {
                    // 记录环境ID
                    envIds.add(scenario.getEnvironmentId());
                }
                // 保存场景的环境配置信息
                refScenarioEnvMap.put(scenario.getId(), envMode);
            }
            envInfo.setRefScenarioEnvMap(refScenarioEnvMap);
        }

        // 查询环境组中的环境ID列表
        Map<String, List<String>> envGroupMap = new HashMap<>();
        environmentGroupService.getEnvironmentGroupRelations(envGroupIds).forEach(environmentGroupRelation -> {
            envGroupMap.putIfAbsent(environmentGroupRelation.getEnvironmentGroupId(), new ArrayList<>());
            envGroupMap.get(environmentGroupRelation.getEnvironmentGroupId()).add(environmentGroupRelation.getEnvironmentId());
            envIds.add(environmentGroupRelation.getEnvironmentId());
        });

        // 获取环境的配置信息
        List<String> distinctEnvIds = envIds.stream().distinct().toList();
        Map<String, EnvironmentInfoDTO> envMap = environmentService.getByIds(distinctEnvIds)
                .stream()
                .collect(Collectors.toMap(EnvironmentInfoDTO::getId, Function.identity()));

        envInfo.setEnvGroupMap(envGroupMap);
        envInfo.setEnvMap(envMap);

        envMap.forEach((envId, envInfoDTO) -> handleHttpModuleMatchRule(envInfoDTO));

        return envInfo;
    }

    /**
     * 处理环境的 HTTP 配置模块匹配规则
     * 查询新增子模块
     *
     * @param envInfoDTO
     */
    private void handleHttpModuleMatchRule(EnvironmentInfoDTO envInfoDTO) {
        List<HttpConfig> httpConfigs = envInfoDTO.getConfig().getHttpConfig();
        for (HttpConfig httpConfig : httpConfigs) {
            if (!httpConfig.isModuleMatchRule()) {
                continue;
            }
            // 获取勾选了包含子模块的模块ID
            HttpConfigModuleMatchRule moduleMatchRule = httpConfig.getModuleMatchRule();
            List<SelectModule> selectModules = moduleMatchRule.getModules();
            List<String> containChildModuleIds = selectModules.stream()
                    .filter(SelectModule::getContainChildModule)
                    .map(SelectModule::getModuleId)
                    .toList();

            // 查询子模块ID, 并去重
            Set<String> moduleIds = apiDefinitionModuleService.getModuleIdsByParentIds(containChildModuleIds)
                    .stream()
                    .collect(Collectors.toSet());
            selectModules.forEach(selectModule -> moduleIds.add(selectModule.getModuleId()));

            // 重新设置选中的模块ID
            moduleMatchRule.setModules(null);
            List<SelectModule> allSelectModules = moduleIds.stream().map(moduleId -> {
                SelectModule module = new SelectModule();
                module.setModuleId(moduleId);
                return module;
            }).collect(Collectors.toList());
            moduleMatchRule.setModules(allSelectModules);
        }
    }

    private List<ApiScenario> getApiScenarioByIds(List<String> apiScenarioIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(apiScenarioIds);
        return apiScenarioMapper.selectByExample(example);
    }

    /**
     * 将步骤解析成 MsTestElement 树结构
     */
    private void parseStep2MsElement(AbstractMsTestElement parentElement,
                                     List<? extends ApiScenarioStepCommonDTO> steps,
                                     ApiScenarioParseTmpParam parseParam) {
        if (CollectionUtils.isNotEmpty(steps)) {
            parentElement.setChildren(new LinkedList<>());
        }

        Map<String, String> stepDetailMap = parseParam.getStepDetailMap();
        Map<String, String> resourceDetailMap = parseParam.getResourceDetailMap();
        Map<String, List<MsHTTPElement>> stepTypeHttpElementMap = parseParam.getStepTypeHttpElementMap();
        for (ApiScenarioStepCommonDTO step : steps) {
            StepParser stepParser = StepParserFactory.getStepParser(step.getStepType());
            if (BooleanUtils.isFalse(step.getEnable())) {
                continue;
            }
            setPartialRefStepEnable(step, stepDetailMap);

            if (isRequestStep(step) && BooleanUtils.isTrue(step.getEnable())) {
                // 记录待执行的请求总数
                parseParam.getRequestCount().getAndIncrement();
            }

            // 将步骤详情解析生成对应的MsTestElement
            AbstractMsTestElement msTestElement = stepParser.parseTestElement(step,
                    MapUtils.isNotEmpty(resourceDetailMap) ? resourceDetailMap.getOrDefault(step.getResourceId(), StringUtils.EMPTY) : StringUtils.EMPTY, stepDetailMap.get(step.getId()));
            if (msTestElement != null) {
                if (msTestElement instanceof MsHTTPElement msHTTPElement) {
                    // 暂存http类型的步骤
                    stepTypeHttpElementMap.putIfAbsent(step.getStepType(), new LinkedList<>());
                    stepTypeHttpElementMap.get(step.getStepType()).add(msHTTPElement);
                }
                msTestElement.setProjectId(step.getProjectId());
                msTestElement.setResourceId(step.getResourceId());
                msTestElement.setStepId(step.getId());
                msTestElement.setName(step.getName());

                // 记录引用的资源ID和项目ID，下载执行文件时需要使用
                parseParam.getRefProjectIds().add(step.getProjectId());
                parseParam.getRefResourceIds().add(step.getResourceId());

                // 设置环境等，运行时场景参数
                setMsScenarioParam(parseParam.getScenarioParseEnvInfo(), step, msTestElement);

                // 记录 msCommonElement
                Optional.ofNullable(apiCommonService.getMsCommonElement(msTestElement))
                        .ifPresent(msCommonElement -> parseParam.getCommonElements().add(msCommonElement));
                // 组装树结构
                parentElement.getChildren().add(msTestElement);

                if (CollectionUtils.isNotEmpty(step.getChildren())) {
                    parseStep2MsElement(msTestElement, step.getChildren(), parseParam);
                }
            }
        }
    }

    /**
     * 设置运行时场景参数
     *
     * @param scenarioParseEnvInfo
     * @param step
     * @param msTestElement
     */
    private void setMsScenarioParam(ApiScenarioParseEnvInfo scenarioParseEnvInfo,
                                    ApiScenarioStepCommonDTO step,
                                    AbstractMsTestElement msTestElement) {
        // 引用的场景设置场景参数
        if (!isScenarioStep(step.getStepType()) || !isRefOrPartialRef(step.getRefType()) || !(msTestElement instanceof MsScenario msScenario)) {
            return;
        }

        if (step.getConfig() != null) {
            // 设置场景步骤的运行参数
            msScenario.setScenarioStepConfig(JSON.parseObject(JSON.toJSONString(step.getConfig()), ScenarioStepConfig.class));
        }

        // 获取当前场景配置的环境信息
        EnvironmentModeDTO environmentModeDTO = scenarioParseEnvInfo.getRefScenarioEnvMap().get(step.getResourceId());
        String environmentId = environmentModeDTO.getEnvironmentId();

        // 设置是否是环境组
        Boolean isGrouped = environmentModeDTO.getGrouped();
        msScenario.setGrouped(isGrouped);
        Map<String, EnvironmentInfoDTO> envMap = scenarioParseEnvInfo.getEnvMap();

        if (BooleanUtils.isTrue(isGrouped)) {
            // 设置环境组 map
            msScenario.setProjectEnvMap(getProjectEnvMap(scenarioParseEnvInfo, environmentId));
        } else {
            // 设置环境
            msScenario.setEnvironmentInfo(envMap.get(environmentId));
        }
    }

    /**
     * 从 scenarioParseEnvInfo 获取对应环境组的 projectEnvMap
     *
     * @param scenarioParseEnvInfo 环境信息
     * @param environmentId        环境ID
     * @return projectEnvMap
     */
    private Map<String, EnvironmentInfoDTO> getProjectEnvMap(ApiScenarioParseEnvInfo scenarioParseEnvInfo, String environmentId) {
        Map<String, List<String>> envGroupMap = scenarioParseEnvInfo.getEnvGroupMap();
        List<String> envIds = envGroupMap.get(environmentId);
        Map<String, EnvironmentInfoDTO> projectEnvMap = new HashMap<>();
        for (String envId : envIds) {
            EnvironmentInfoDTO environmentInfoDTO = scenarioParseEnvInfo.getEnvMap().get(envId);
            projectEnvMap.put(environmentInfoDTO.getProjectId(), environmentInfoDTO);
        }
        return projectEnvMap;
    }

    private static boolean isScenarioStep(String stepType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API_SCENARIO.name());
    }

    /**
     * 设置单个部分引用的步骤的启用状态
     */
    private void setPartialRefStepEnable(ApiScenarioStepCommonDTO step, Map<String, String> stepDetailMap) {
        String stepDetail = stepDetailMap.get(step.getId());
        if (!isPartialRef(step) || StringUtils.isBlank(stepDetail)) {
            return;
        }
        PartialRefStepDetail partialRefStepDetail = JSON.parseObject(stepDetail, PartialRefStepDetail.class);
        setChildPartialRefEnable(step.getChildren(), partialRefStepDetail.getEnableStepIds());
    }

    /**
     * 设置部分引用的步骤的启用状态
     */
    private void setChildPartialRefEnable(List<? extends ApiScenarioStepCommonDTO> steps, Set<String> enableStepIds) {
        for (ApiScenarioStepCommonDTO step : steps) {
            if (StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.REF.name())) {
                // 引用的启用不修改
                continue;
            }
            // 非完全引用的步骤，使用当前场景配置的启用状态
            step.setEnable(enableStepIds.contains(step.getId()));
            if (isPartialRef(step)) {
                // 如果是部分引用的场景，不递归解析了，上层的递归会解析
                continue;
            }
            // 非完全引用和部分引用的步骤，递归设置子步骤
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                setChildPartialRefEnable(step.getChildren(), enableStepIds);
            }
        }
    }

    private boolean isPartialRef(ApiScenarioStepCommonDTO step) {
        return isScenarioStep(step.getStepType()) &&
                StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    private Map<String, String> getStepDetailMap(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, Object> stepDetailsParam) {
        List<String> needBlobStepIds = getHasDetailStepIds(steps, stepDetailsParam);
        Map<String, String> stepDetails = getStepBlobByIds(needBlobStepIds).stream()
                .collect(Collectors.toMap(ApiScenarioStepBlob::getId, blob -> new String(blob.getContent())));
        // 前端有传，就用前端传的
        if (stepDetailsParam != null) {
            stepDetailsParam.forEach((stepId, detail) -> stepDetails.put(stepId, detail instanceof byte[] bytes ? new String(bytes) : JSON.toJSONString(detail)));
        }
        return stepDetails;
    }

    private List<String> getHasDetailStepIds(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, Object> stepDetailsParam) {
        List<String> needBlobStepIds = new ArrayList<>();
        for (ApiScenarioStepCommonDTO step : steps) {
            if (BooleanUtils.isFalse(step.getEnable())) {
                continue;
            }
            if (!hasStepDetail(step.getStepType())) {
                continue;
            }
            if (stepDetailsParam != null && stepDetailsParam.containsKey(step.getId())) {
                // 前端传了blob，不需要再查
                continue;
            }
            needBlobStepIds.add(step.getId());
            List<? extends ApiScenarioStepCommonDTO> children = step.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                needBlobStepIds.addAll(getHasDetailStepIds(children, stepDetailsParam));
            }
        }
        return needBlobStepIds;
    }

    /**
     * 非完全引用的步骤和接口定义的步骤，才需要查 blob
     *
     * @param stepType
     * @return
     */
    private boolean hasStepDetail(String stepType) {
        return !StringUtils.equals(stepType, ApiScenarioStepRefType.REF.name())
                || isApi(stepType);
    }

    private Map<String, String> getResourceDetailMap(Map<String, List<String>> refResourceMap) {
        Map<String, String> resourceBlobMap = new HashMap<>();
        List<String> apiIds = refResourceMap.get(ApiScenarioStepType.API.name());
        List<ApiDefinitionBlob> apiDefinitionBlobs = apiDefinitionService.getBlobByIds(apiIds);
        apiDefinitionBlobs.forEach(blob -> resourceBlobMap.put(blob.getId(), new String(blob.getRequest())));

        List<String> apiCaseIds = refResourceMap.get(ApiScenarioStepType.API_CASE.name());
        List<ApiTestCaseBlob> apiTestCaseBlobs = apiTestCaseService.getBlobByIds(apiCaseIds);
        apiTestCaseBlobs.forEach(blob -> resourceBlobMap.put(blob.getId(), new String(blob.getRequest())));

        List<String> apiScenarioIds = refResourceMap.get(ApiScenarioStepType.API_SCENARIO.name());
        List<ApiScenarioBlob> apiScenarioBlobs = getBlobByIds(apiScenarioIds);
        apiScenarioBlobs.forEach(blob -> resourceBlobMap.put(blob.getId(), new String(blob.getConfig())));
        return resourceBlobMap;
    }

    private List<ApiScenarioStepBlob> getStepBlobByIds(List<String> stepIds) {
        if (CollectionUtils.isEmpty(stepIds)) {
            return Collections.emptyList();
        }
        ApiScenarioStepBlobExample example = new ApiScenarioStepBlobExample();
        example.createCriteria().andIdIn(stepIds);
        return apiScenarioStepBlobMapper.selectByExampleWithBLOBs(example);
    }

    private List<ApiScenarioBlob> getBlobByIds(List<String> apiScenarioIds) {
        if (CollectionUtils.isEmpty(apiScenarioIds)) {
            return Collections.emptyList();
        }
        ApiScenarioBlobExample example = new ApiScenarioBlobExample();
        example.createCriteria().andIdIn(apiScenarioIds);
        return apiScenarioBlobMapper.selectByExampleWithBLOBs(example);
    }

    private void buildRefResourceIdMap(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, List<String>> refResourceIdMap) {
        for (ApiScenarioStepCommonDTO step : steps) {
            if (isRefOrPartialRef(step.getRefType()) && BooleanUtils.isTrue(step.getEnable())) {
                // 记录引用的步骤ID
                List<String> resourceIds = refResourceIdMap.computeIfAbsent(step.getStepType(), k -> new ArrayList<>());
                resourceIds.add(step.getResourceId());
            }

            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                buildRefResourceIdMap(step.getChildren(), refResourceIdMap);
            }
        }
    }

    private ScenarioConfig getScenarioConfig(ApiScenarioDebugRequest request, boolean hasSave) {
        if (request.getScenarioConfig() != null) {
            // 优先使用前端传的配置
            return request.getScenarioConfig();
        } else if (hasSave) {
            // 没传并且保存过，则从数据库获取
            ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(request.getId());
            if (apiScenarioBlob != null) {
                return JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class);
            }
        }
        return new ScenarioConfig();
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
        return apiScenarioDetailDTO;
    }

    public ApiScenarioDetail get(String scenarioId) {
        ApiScenario apiScenario = checkResourceExist(scenarioId);
        ApiScenarioDetail apiScenarioDetail = BeanUtils.copyBean(new ApiScenarioDetail(), apiScenario);
        ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(scenarioId);
        if (apiScenarioBlob != null) {
            apiScenarioDetail.setScenarioConfig(JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class));
        }
        //存放csv变量
        List<CsvVariable> csvVariables = extApiScenarioStepMapper.getCsvVariableByScenarioId(scenarioId);
        if (CollectionUtils.isNotEmpty(csvVariables) && apiScenarioDetail.getScenarioConfig() != null
                && apiScenarioDetail.getScenarioConfig().getVariable() != null) {
            apiScenarioDetail.getScenarioConfig().getVariable().setCsvVariables(csvVariables);
        }

        // 获取所有步骤
        List<ApiScenarioStepDTO> allSteps = getAllStepsByScenarioIds(List.of(scenarioId))
                .stream()
                .distinct() // 这里可能存在多次引用相同场景，步骤可能会重复，去重
                .collect(Collectors.toList());

        //获取所有步骤的csv的关联关系
        List<String> stepIds = allSteps.stream().map(ApiScenarioStepDTO::getId).toList();
        if (CollectionUtils.isNotEmpty(stepIds)) {
            List<ApiScenarioCsvStep> csvSteps = extApiScenarioStepMapper.getCsvStepByStepIds(stepIds);
            // 构造 map，key 为步骤ID，value 为csv文件ID列表
            Map<String, List<String>> stepsCsvMap = csvSteps.stream()
                    .collect(Collectors.groupingBy(ApiScenarioCsvStep::getStepId, Collectors.mapping(ApiScenarioCsvStep::getFileId, Collectors.toList())));
            //将stepsCsvMap根据步骤id放入到allSteps中
            if (CollectionUtils.isNotEmpty(allSteps)) {
                allSteps.forEach(step -> step.setCsvFileIds(stepsCsvMap.get(step.getId())));
            }
        }

        // 构造 map，key 为场景ID，value 为步骤列表
        Map<String, List<ApiScenarioStepDTO>> scenarioStepMap = allSteps.stream()
                .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getScenarioId()).orElse(StringUtils.EMPTY)));

        // 查询步骤详情
        Map<String, String> stepDetailMap = getStepDetailMap(allSteps, Map.of());

        // key 为父步骤ID，value 为子步骤列表
        if (MapUtils.isEmpty(scenarioStepMap)) {
            return apiScenarioDetail;
        }

        Map<String, List<ApiScenarioStepDTO>> currentScenarioParentStepMap = scenarioStepMap.get(scenarioId)
                .stream()
                .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getParentId()).orElse(StringUtils.EMPTY)));

        List<ApiScenarioStepDTO> steps = buildStepTree(currentScenarioParentStepMap.get(StringUtils.EMPTY), currentScenarioParentStepMap, scenarioStepMap);

        // 设置部分引用的步骤的启用状态
        setPartialRefStepsEnable(steps, stepDetailMap);

        apiScenarioDetail.setSteps(steps);

        return apiScenarioDetail;
    }

    /**
     * 设置部分引用的步骤的启用状态
     */
    private void setPartialRefStepsEnable(List<? extends ApiScenarioStepCommonDTO> steps, Map<String, String> stepDetailMap) {
        if (CollectionUtils.isNotEmpty(steps)) {
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
    private boolean isRefApi(String stepType, String refType) {
        return isApi(stepType) && StringUtils.equals(refType, ApiScenarioStepRefType.REF.name());
    }

    private boolean isApi(String stepType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API.name());
    }

    private boolean isApiCase(String stepType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API_CASE.name());
    }

    /**
     * 递归构造步骤树
     *
     * @param steps           当前场景下，当前层级的步骤
     * @param parentStepMap   当前场景所有的步骤，key 为父步骤ID，value 为子步骤列表
     * @param scenarioStepMap 所有场景步骤，key 为场景ID，value 为子步骤列表
     */
    private List<ApiScenarioStepDTO> buildStepTree(List<ApiScenarioStepDTO> steps,
                                                   Map<String, List<ApiScenarioStepDTO>> parentStepMap,
                                                   Map<String, List<ApiScenarioStepDTO>> scenarioStepMap) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyList();
        }
        steps.forEach(step -> {
            // 获取当前步骤的子步骤
            List<ApiScenarioStepDTO> children = Optional.ofNullable(parentStepMap.get(step.getId())).orElse(new ArrayList<>(0));
            if (isRefOrPartialScenario(step)) {

                List<ApiScenarioStepDTO> scenarioSteps = scenarioStepMap.get(step.getResourceId());
                scenarioSteps.forEach(item -> {
                    // 如果步骤的场景ID不等于当前场景的ID，说明是引用的步骤，如果 parentId 为空，说明是一级子步骤，重新挂载到对应的场景中
                    if (StringUtils.isEmpty(item.getParentId())) {
                        children.add(item);
                    }
                });

                if (CollectionUtils.isEmpty(children)) {
                    return;
                }

                // 如果当前步骤是引用的场景，获取该场景的子步骤
                Map<String, List<ApiScenarioStepDTO>> childStepMap = scenarioSteps
                        .stream()
                        .collect(Collectors.groupingBy(item -> Optional.ofNullable(item.getParentId()).orElse(StringUtils.EMPTY)));
                step.setChildren(buildStepTree(children, childStepMap, scenarioStepMap));
            } else {
                if (CollectionUtils.isEmpty(children)) {
                    return;
                }
                step.setChildren(buildStepTree(children, parentStepMap, scenarioStepMap));
            }
        });
        // 排序
        return steps.stream()
                .sorted(Comparator.comparing(ApiScenarioStepDTO::getSort))
                .collect(Collectors.toList());
    }

    /**
     * 判断步骤是否是引用的场景
     */
    private boolean isRefOrPartialScenario(ApiScenarioStepDTO step) {
        return isRefOrPartialRef(step.getRefType()) && isScenarioStep(step.getStepType());
    }

    /**
     * 递归获取所有的场景步骤
     */
    private List<ApiScenarioStepDTO> getAllStepsByScenarioIds(List<String> scenarioIds) {
        List<ApiScenarioStepDTO> steps = getStepDTOByScenarioIds(scenarioIds);
        if (CollectionUtils.isEmpty(steps)) {
            return steps;
        }

        // 将 config 转换成对象
        steps.forEach(step -> {
            if (step.getConfig() != null && StringUtils.isNotBlank(step.getConfig().toString())) {
                step.setConfig(JSON.parseObject(step.getConfig().toString()));
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

    private List<ApiScenarioStepDTO> getStepDTOByScenarioIds(List<String> scenarioIds) {
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return Collections.emptyList();
        }
        return extApiScenarioStepMapper.getStepDTOByScenarioIds(scenarioIds);
    }

    public Object getStepDetail(String stepId) {
        ApiScenarioStep step = apiScenarioStepMapper.selectByPrimaryKey(stepId);
        StepParser stepParser = StepParserFactory.getStepParser(step.getStepType());
        Object stepDetail = stepParser.parseDetail(step);
        if (stepDetail instanceof AbstractMsTestElement msTestElement) {
            // 设置关联的文件的最新信息
            if (isRef(step.getRefType())) {
                apiCommonService.setLinkFileInfo(step.getResourceId(), msTestElement);
                if (isApi(step.getStepType())) {
                    ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(step.getResourceId());
                    apiCommonService.setApiDefinitionExecuteInfo(msTestElement, apiDefinition);
                } else if (isApiCase(step.getStepType())) {
                    ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(step.getResourceId());
                    ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
                    apiCommonService.setApiDefinitionExecuteInfo(msTestElement, apiDefinition);
                }
            } else {
                apiCommonService.setLinkFileInfo(step.getScenarioId(), msTestElement);
            }
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
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(), OperationLogType.UPDATE.name(), logInsertModule);
        return response;
    }

    public ApiScenarioBatchOperationResponse batchCopy(ApiScenarioBatchCopyMoveRequest request, LogInsertModule logInsertModule) {
        this.checkTargetModule(request.getTargetModuleId(), request.getProjectId());

        List<String> scenarioIds = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return new ApiScenarioBatchOperationResponse();
        }
        request.setSelectIds(scenarioIds);
        ApiScenarioBatchOperationResponse response =
                ApiScenarioBatchOperationUtils.executeWithBatchOperationResponse(scenarioIds, sublist -> copyAndInsert(sublist, request, logInsertModule.getOperator()));
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(), OperationLogType.COPY.name(), logInsertModule);
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

    private ApiScenarioBatchOperationResponse copyAndInsert(List<String> scenarioIds, ApiScenarioBatchCopyMoveRequest request, String operator) {
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

        operationList.forEach(apiScenario -> {
            ApiScenario copyScenario = new ApiScenario();
            BeanUtils.copyBean(copyScenario, apiScenario);
            copyScenario.setId(IDGenerator.nextStr());
            copyScenario.setNum(getNextNum(copyScenario.getProjectId()));
            copyScenario.setPos(getNextOrder(copyScenario.getProjectId()));
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
            if (CollectionUtils.isNotEmpty(stepList)) {
                stepList.forEach(step -> {
                    ApiScenarioStep copyStep = new ApiScenarioStep();
                    BeanUtils.copyBean(copyStep, step);
                    copyStep.setId(IDGenerator.nextStr());
                    copyStep.setScenarioId(copyScenario.getId());
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
        });

        response.setSuccess(apiScenarioMapper.batchInsert(insertApiScenarioList));
        if (CollectionUtils.isNotEmpty(insertApiScenarioBlobList)) {
            apiScenarioBlobMapper.batchInsert(insertApiScenarioBlobList);
        }
        if (CollectionUtils.isNotEmpty(insertApiScenarioStepList)) {
            apiScenarioStepMapper.batchInsert(insertApiScenarioStepList);
        }
        if (CollectionUtils.isNotEmpty(insertApiScenarioStepBlobList)) {
            apiScenarioStepBlobMapper.batchInsert(insertApiScenarioStepBlobList);
        }
        if (CollectionUtils.isNotEmpty(insertApiFileResourceList)) {
            apiFileResourceService.batchInsert(insertApiFileResourceList);
        }
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
                isDeleteOperation ? OperationLogType.DELETE.name() : OperationLogType.RECOVER.name(), logInsertModule);
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
        apiScenarioLogService.saveBatchOperationLog(response, request.getProjectId(), OperationLogType.DELETE.name(), logInsertModule);
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
            if (CollectionUtils.isNotEmpty(apiRequest.getModuleIds())) {
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
        if (ObjectUtils.isNotEmpty(caseRequest)) {
            if (CollectionUtils.isNotEmpty(caseRequest.getModuleIds())) {
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
        if (ObjectUtils.isNotEmpty(scenarioRequest)) {
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
        return steps;
    }

    @Override
    public void updatePos(String id, long pos) {
        extApiScenarioMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> posIds = extApiScenarioMapper.selectIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtApiTestCaseMapper batchUpdateMapper = sqlSession.getMapper(ExtApiTestCaseMapper.class);
        for (int i = 0; i < posIds.size(); i++) {
            batchUpdateMapper.updatePos(posIds.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public void moveNode(PosRequest posRequest) {
        NodeMoveRequest request = super.getNodeMoveRequest(posRequest);
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                posRequest.getProjectId(),
                request,
                extApiScenarioMapper::selectDragInfoById,
                extApiScenarioMapper::selectNodeByPosOperator
        );
        this.sort(sortDTO);
    }

    public List<ExecuteReportDTO> getExecuteList(ExecutePageRequest request) {
        List<ExecuteReportDTO> executeList = extApiScenarioMapper.getExecuteList(request);
        if (CollectionUtils.isEmpty(executeList)) {
            return new ArrayList<>();
        }
        Set<String> userSet = executeList.stream()
                .flatMap(apiReport -> Stream.of(apiReport.getCreateUser()))
                .collect(Collectors.toSet());
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        executeList.forEach(apiReport -> {
            apiReport.setOperationUser(userMap.get(apiReport.getCreateUser()));
            Date date = new Date(apiReport.getStartTime());
            apiReport.setNum(sdf.format(date));
        });
        return executeList;
    }

    public List<OperationHistoryDTO> operationHistoryList(OperationHistoryRequest request) {
        return operationHistoryService.listWidthTable(request, SCENARIO_TABLE);
    }

    public void handleFileAssociationUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        // 查询有步骤详情的请求类型的步骤
        List<String> stepIds = extApiScenarioStepMapper.getHasBlobRequestStepIds(originFileAssociation.getSourceId());
        // 查询步骤详情
        ApiScenarioStepBlobExample blobExample = new ApiScenarioStepBlobExample();
        blobExample.createCriteria().andIdIn(stepIds);
        List<ApiScenarioStepBlob> apiScenarioStepBlobs = apiScenarioStepBlobMapper.selectByExampleWithBLOBs(blobExample);

        for (ApiScenarioStepBlob apiScenarioStepBlob : apiScenarioStepBlobs) {
            AbstractMsTestElement msTestElement = null;
            try {
                msTestElement = ApiDataUtils.parseObject(new String(apiScenarioStepBlob.getContent()), AbstractMsTestElement.class);
                // 如果插件删除，会转换异常
            } catch (Exception e) {
                LogUtils.error(e);
            }
            if (msTestElement instanceof MsHTTPElement msHTTPElement) {
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
    }

    public String transfer(ApiTransferRequest request, String userId) {
        return apiFileResourceService.transfer(request, userId, ApiResourceType.API_SCENARIO.name());
    }

    public List<ReferenceDTO> getReference(ReferenceRequest request) {
        return extApiDefinitionMapper.getReference(request);
    }

    public Project getStepResourceProjectInfo(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return null;
        }
        Project projectInfo = new Project();
        projectInfo.setId(project.getId());
        projectInfo.setName(project.getName());
        return projectInfo;
    }
}
