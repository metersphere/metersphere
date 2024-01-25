package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.step.StepParser;
import io.metersphere.api.parser.step.StepParserFactory;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiScenarioBatchOperationUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApiExecuteRunMode;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.EnvironmentGroupExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.metersphere.api.controller.result.ApiResultCode.API_SCENARIO_EXIST;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioService {
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
    private ProjectService projectService;
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
    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String TAGS = "Tags";
    public static final String ENVIRONMENT = "Environment";


    public List<ApiScenarioDTO> getScenarioPage(ApiScenarioPageRequest request) {
        //CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request, userId);
        //TODO  场景的自定义字段 等设计 不一定会有
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
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
        scenarioLists.forEach(item -> {
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));
            item.setModulePath(StringUtils.isNotBlank(moduleMap.get(item.getModuleId())) ? moduleMap.get(item.getModuleId()) : Translator.get("api_unplanned_scenario"));
            if (!item.getGrouped() && envMap.containsKey(item.getEnvironmentId())) {
                item.setEnvironmentName(envMap.get(item.getEnvironmentId()));
            } else if (item.getGrouped() && groupMap.containsKey(item.getId())) {
                item.setEnvironmentName(groupMap.get(item.getEnvironmentId()));
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
        if (request.isAppendTag()) {
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
            // 获取待添加的步骤详情
            List<ApiScenarioStepBlob> apiScenarioStepsDetails = getPartialRefStepDetails(request.getSteps());
            apiScenarioStepsDetails.addAll(getUpdateStepDetails(steps, request.getStepDetails()));
            apiScenarioStepsDetails.forEach(step -> step.setScenarioId(scenario.getId()));

            if (CollectionUtils.isNotEmpty(steps)) {
                apiScenarioStepMapper.batchInsert(steps);
            }
            if (CollectionUtils.isNotEmpty(apiScenarioStepsDetails)) {
                apiScenarioStepBlobMapper.batchInsert(apiScenarioStepsDetails);
            }

            saveStepCsv(steps, csvSteps);
        }

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(scenario.getId(), scenario.getProjectId(), creator);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);
        //处理csv变量
        if (request.getScenarioConfig() != null
                && request.getScenarioConfig().getVariable() != null) {
            saveCsv(request.getScenarioConfig().getVariable().getCsvVariables(), resourceUpdateRequest);
        }
        return scenario;
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
        resourceUpdateRequest.setUnLinkRefIds(deleteRefs);
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
                    apiFileResourceService.createFileLogRecord(resourceUpdateRequest.getOperator(), resourceUpdateRequest.getProjectId(), OperationLogModule.API_SCENARIO));
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
        List<String> unLinkRefIds = resourceUpdateRequest.getUnLinkRefIds();
        // 处理关联文件
        if (CollectionUtils.isNotEmpty(unLinkRefIds)) {
            fileAssociationService.deleteBySourceIdAndFileIds(resourceUpdateRequest.getResourceId(), unLinkRefIds,
                    apiFileResourceService.createFileLogRecord(resourceUpdateRequest.getOperator(), resourceUpdateRequest.getProjectId(), resourceUpdateRequest.getLogModule()));
            example.clear();
            example.createCriteria()
                    .andScenarioIdEqualTo(resourceUpdateRequest.getResourceId())
                    .andFileIdIn(unLinkRefIds);
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
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);

        if (request.getScenarioConfig() != null) {
            // 更新场景配置
            ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
            apiScenarioBlob.setId(scenario.getId());
            apiScenarioBlob.setConfig(JSON.toJSONString(request.getScenarioConfig()).getBytes());
            apiScenarioBlobMapper.updateByPrimaryKeyWithBLOBs(apiScenarioBlob);
        }

        // 更新场景步骤
        updateApiScenarioStep(request, scenario);

        ApiScenario originScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(scenario.getId(), originScenario.getProjectId(), updater);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        resourceUpdateRequest.setUnLinkRefIds(request.getUnLinkRefIds());
        resourceUpdateRequest.setDeleteFileIds(request.getDeleteFileIds());
        apiFileResourceService.updateFileResource(resourceUpdateRequest);

        //处理csv变量
        if (request.getScenarioConfig() != null
                && request.getScenarioConfig().getVariable() != null) {
            saveCsv(request.getScenarioConfig().getVariable().getCsvVariables(), resourceUpdateRequest);
        } else {
            saveCsv(new ArrayList<>(), resourceUpdateRequest);
        }
        return scenario;
    }

    /**
     * 更新场景步骤
     *
     * @param request
     * @param scenario
     */
    private void updateApiScenarioStep(ApiScenarioUpdateRequest request, ApiScenario scenario) {
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
            List<ApiScenarioStepBlob> apiScenarioStepsDetails = getPartialRefStepDetails(request.getSteps());
            apiScenarioStepsDetails.addAll(getUpdateStepDetails(apiScenarioSteps, request.getStepDetails()));
            apiScenarioStepsDetails.forEach(step -> step.setScenarioId(scenario.getId()));

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
            });

            // 查询原有的步骤详情
            Set<String> originStepDetailIds = extApiScenarioStepBlobMapper.getStepIdsByScenarioId(scenario.getId())
                    .stream().collect(Collectors.toSet());

            // 添加新增的步骤详情
            List<ApiScenarioStepBlob> addApiScenarioStepsDetails = apiScenarioStepsDetails.stream()
                    .filter(step -> !originStepDetailIds.contains(step.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addApiScenarioStepsDetails)) {
                apiScenarioStepBlobMapper.batchInsert(addApiScenarioStepsDetails);
            }
            // 更新原有的步骤详情
            apiScenarioStepsDetails.stream()
                    .filter(step -> originStepDetailIds.contains(step.getId()))
                    .forEach(apiScenarioStepBlobMapper::updateByPrimaryKeySelective);
        } else if (MapUtils.isNotEmpty(request.getStepDetails())) {
            // steps 为 null，stepDetails 不为 null，则只更新详情
            // 查询原有的步骤详情
            Set<String> originStepDetailIds = extApiScenarioStepBlobMapper.getStepIdsByScenarioId(scenario.getId())
                    .stream().collect(Collectors.toSet());
            // 更新原有的步骤详情
            request.getStepDetails().forEach((stepId, stepDetail) -> {
                if (originStepDetailIds.contains(stepId)) {
                    ApiScenarioStepBlob apiScenarioStepBlob = new ApiScenarioStepBlob();
                    apiScenarioStepBlob.setId(stepId);
                    apiScenarioStepBlob.setContent(JSON.toJSONString(stepDetail).getBytes());
                    apiScenarioStepBlobMapper.updateByPrimaryKeySelective(apiScenarioStepBlob);
                }
            });
        }
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
     *
     * @param apiScenarioSteps
     * @param stepDetails
     * @return
     */
    private List<ApiScenarioStepBlob> getUpdateStepDetails(List<ApiScenarioStep> apiScenarioSteps, Map<String, Object> stepDetails) {
        if (MapUtils.isEmpty(stepDetails)) {
            return Collections.emptyList();
        }

        Map<String, ApiScenarioStep> scenarioStepMap = apiScenarioSteps.stream()
                .collect(Collectors.toMap(ApiScenarioStep::getId, Function.identity()));

        List<ApiScenarioStepBlob> apiScenarioStepsDetails = new ArrayList<>();
        stepDetails.forEach((stepId, stepDetail) -> {
            ApiScenarioStep step = scenarioStepMap.get(stepId);
            if (step == null) {
                return;
            }
            if (!isRef(step.getRefType()) || isRefApi(step.getStepType(), step.getRefType())) {
                // 非引用的步骤，如果有编辑内容，保存到blob表
                // 如果引用的是接口定义，也保存详情，因为应用接口定义允许修改参数值
                ApiScenarioStepBlob apiScenarioStepBlob = new ApiScenarioStepBlob();
                apiScenarioStepBlob.setId(stepId);
                apiScenarioStepBlob.setContent(JSON.toJSONString(stepDetail).getBytes());
                apiScenarioStepsDetails.add(apiScenarioStepBlob);
            }
        });
        return apiScenarioStepsDetails;
    }

    private boolean isRef(String refType) {
        return StringUtils.equalsAny(refType, ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    /**
     * 解析步骤树结构
     * 获取待更新的 ApiScenarioStep 列表
     *
     * @param parent
     * @param steps
     * @return
     */
    private List<ApiScenarioStep> getApiScenarioSteps(ApiScenarioStepCommonDTO parent,
                                                      List<? extends ApiScenarioStepCommonDTO> steps, List<ApiScenarioCsvStep> csvSteps) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyList();
        }
        List<ApiScenarioStep> apiScenarioSteps = new ArrayList<>();
        long sort = 1;
        for (ApiScenarioStepCommonDTO step : steps) {
            ApiScenarioStep apiScenarioStep = new ApiScenarioStep();
            BeanUtils.copyBean(apiScenarioStep, step);
            apiScenarioStep.setSort(sort++);
            if (parent != null) {
                apiScenarioStep.setParentId(parent.getId());
            }
            if (step.getConfig() != null) {
                apiScenarioStep.setConfig(JSON.toJSONString(step.getConfig()));
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
            apiScenarioSteps.add(apiScenarioStep);

            if (StringUtils.equalsAny(step.getRefType(), ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name())) {
                // 引用的步骤不解析子步骤
                continue;
            }
            // 解析子步骤
            apiScenarioSteps.addAll(getApiScenarioSteps(step, step.getChildren(), csvSteps));
        }
        return apiScenarioSteps;
    }

    /**
     * 解析步骤树结构
     * 获取待更新的 ApiScenarioStep 列表
     *
     * @param steps
     * @return
     */
    private List<ApiScenarioStepBlob> getPartialRefStepDetails(List<? extends ApiScenarioStepCommonDTO> steps) {
        if (CollectionUtils.isEmpty(steps)) {
            return Collections.emptyList();
        }
        List<ApiScenarioStepBlob> apiScenarioStepsDetails = new ArrayList<>();

        for (ApiScenarioStepCommonDTO step : steps) {
            if (StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.REF.name())) {
                // 引用的步骤不解析子步骤
                continue;
            }
            if (StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.PARTIAL_REF.name())) {
                // 如果是部分引用，blob表保存启用的子步骤ID
                Set<String> enableStepSet = getEnableStepSet(step.getChildren());
                PartialRefStepDetail stepDetail = new PartialRefStepDetail();
                stepDetail.setEnableStepIds(enableStepSet);
                ApiScenarioStepBlob apiScenarioStepBlob = new ApiScenarioStepBlob();
                apiScenarioStepBlob.setId(step.getId());
                apiScenarioStepBlob.setContent(JSON.toJSONString(stepDetail).getBytes());
                apiScenarioStepsDetails.add(apiScenarioStepBlob);
            }
            apiScenarioStepsDetails.addAll(getPartialRefStepDetails(step.getChildren()));
        }
        return apiScenarioStepsDetails;
    }

    /**
     * 获取步骤及子步骤中 enable 的步骤ID
     *
     * @param steps
     * @return
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
            if (!isRef(step.getRefType())) {
                // 获取子步骤中 enable 的步骤
                enableSteps.addAll(getEnableStepSet(step.getChildren()));
            }
        }
        return enableSteps;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiScenarioDir = DefaultRepositoryDir.getApiScenarioDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiScenarioDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_SCENARIO);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_SCENARIO);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_DEBUG);
        return resourceUpdateRequest;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.API_SCENARIO);
    }

    public Long getNextOrder(String projectId) {
        return projectService.getNextOrder(extApiScenarioMapper::getLastPos, projectId);
    }

    public void delete(String id) {
        checkResourceExist(id);
        apiScenarioMapper.deleteByPrimaryKey(id);
        apiScenarioBlobMapper.deleteByPrimaryKey(id);
        deleteStepByScenarioId(id);
        deleteStepDetailByScenarioId(id);
    }


    public void restore(String id) {
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
    }

    private void checkAddExist(ApiScenarioAddRequest apiScenario) {
        ApiScenarioExample example = new ApiScenarioExample();
        // 统一模块下名称不能重复
        example.createCriteria()
                .andNameEqualTo(apiScenario.getName())
                .andModuleIdEqualTo(apiScenario.getModuleId());
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
                .andNameEqualTo(request.getName());
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

    public String debug(ApiScenarioDebugRequest request) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        boolean hasSave = apiScenario != null;
        String reportId = IDGenerator.nextStr();

        List<ApiScenarioStepRequest> steps = request.getSteps();

        // 记录引用的资源ID
        Map<String, List<String>> refResourceMap = new HashMap<>();
        buildRefResourceIdMap(steps, refResourceMap);

        // 查询引用的资源详情
        Map<String, String> resourceBlobMap = getResourceBlobMap(refResourceMap);

        // 查询复制的步骤详情
        Map<String, String> detailMap = getStepDetailMap(steps, request.getStepDetails());

        // 解析生成待执行的场景树
        MsScenario msScenario = new MsScenario();
        msScenario.setScenarioConfig(getScenarioConfig(request, hasSave));
        parseStep2MsElement(msScenario, steps, resourceBlobMap, detailMap);

        ApiResourceRunRequest runRequest = BeanUtils.copyBean(new ApiResourceRunRequest(), request);
        runRequest.setProjectId(request.getProjectId());
        runRequest.setTestId(request.getId());
        runRequest.setReportId(reportId);
        runRequest.setResourceType(ApiResourceType.API_SCENARIO.name());
        runRequest.setRunMode(ApiExecuteRunMode.BACKEND_DEBUG.name());
        runRequest.setTempFileIds(request.getTempFileIds());
        runRequest.setGrouped(request.getGrouped());
        runRequest.setEnvironmentId(request.getEnvironmentId());
        runRequest.setTestElement(msScenario);

        apiExecuteService.debug(runRequest);

        return reportId;
    }

    /**
     * 将步骤解析成 MsTestElement 树结构
     *
     * @param parentElement
     * @param steps
     * @param resourceBlobMap
     * @param stepDetailMap
     */
    private void parseStep2MsElement(AbstractMsTestElement parentElement,
                                     List<? extends ApiScenarioStepCommonDTO> steps,
                                     Map<String, String> resourceBlobMap,
                                     Map<String, String> stepDetailMap) {
        if (CollectionUtils.isNotEmpty(steps)) {
            parentElement.setChildren(new LinkedList<>());
        }
        for (ApiScenarioStepCommonDTO step : steps) {
            StepParser stepParser = StepParserFactory.getStepParser(step.getStepType());
            if (BooleanUtils.isFalse(step.getEnable())) {
                continue;
            }
            setPartialRefStepEnable(step, stepDetailMap);

            // 将步骤详情解析生成对应的MsTestElement
            AbstractMsTestElement msTestElement = stepParser.parseTestElement(step, resourceBlobMap.get(step.getResourceId()), stepDetailMap.get(step.getId()));
            if (msTestElement != null) {
                parentElement.getChildren().add(msTestElement);
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                parseStep2MsElement(msTestElement, step.getChildren(), resourceBlobMap, stepDetailMap);
            }
        }
    }

    /**
     * 设置单个部分引用的步骤的启用状态
     *
     * @param step
     * @param stepDetailMap
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
     *
     * @param steps
     * @param enableStepIds
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
        return StringUtils.equals(step.getStepType(), ApiScenarioStepType.API_SCENARIO.name()) &&
                StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    private Map<String, String> getStepDetailMap(List<ApiScenarioStepRequest> steps, Map<String, Object> stepDetailsParam) {
        List<String> needBlobStepIds = new ArrayList<>();
        for (ApiScenarioStepRequest step : steps) {
            if (BooleanUtils.isFalse(step.getEnable())) {
                continue;
            }
            if (StringUtils.equalsAny(step.getStepType(), ApiScenarioStepRefType.REF.name())
                    && !StringUtils.equals(step.getRefType(), ApiScenarioStepType.API.name())) {
                // 非完全引用的步骤和接口定义的步骤，才需要查blob
                continue;
            }
            if (stepDetailsParam != null && stepDetailsParam.keySet().contains(step.getId())) {
                // 前端传了blob，不需要再查
                continue;
            }
            needBlobStepIds.add(step.getId());
        }

        Map<String, String> stepDetails = getStepBlobByIds(needBlobStepIds).stream()
                .collect(Collectors.toMap(ApiScenarioStepBlob::getId, blob -> new String(blob.getContent())));
        // 前端有传，就用前端传的
        if (stepDetailsParam != null) {
            stepDetailsParam.forEach((stepId, detail) -> stepDetails.put(stepId, JSON.toJSONString(detail)));
        }
        return stepDetails;
    }

    private Map<String, String> getResourceBlobMap(Map<String, List<String>> refResourceMap) {
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
            if (isRef(step.getRefType()) && BooleanUtils.isTrue(step.getEnable())) {
                // 记录引用的步骤ID
                List<String> resourceIds = refResourceIdMap.get(step.getStepType());
                if (resourceIds == null) {
                    resourceIds = new ArrayList<>();
                    refResourceIdMap.put(step.getStepType(), resourceIds);
                }
                resourceIds.add(step.getResourceId());
            }

            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                buildRefResourceIdMap(step.getChildren(), refResourceIdMap);
            }
        }
    }

    private ScenarioConfig getScenarioConfig(ApiScenarioDebugRequest request, boolean hasSave) {
        ScenarioConfig scenarioConfig = null;
        if (request.getScenarioConfig() != null) {
            // 优先使用前端传的配置
            scenarioConfig = request.getScenarioConfig();
        } else if (hasSave) {
            // 没传并且保存过，则从数据库获取
            ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(request.getId());
            if (apiScenarioBlob != null) {
                scenarioConfig = JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class);
            }
        }
        return scenarioConfig;
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

    public ApiScenarioDetail get(String scenarioId) {
        checkResourceExist(scenarioId);
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(scenarioId);
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
        List<ApiScenarioStepDTO> allSteps = getAllStepsByScenarioIds(List.of(scenarioId));
        //获取所有步骤的csv的关联关系
        List<String> stepIds = allSteps.stream().map(ApiScenarioStepDTO::getId).toList();
        List<ApiScenarioCsvStep> csvSteps = extApiScenarioStepMapper.getCsvStepByStepIds(stepIds);
        // 构造 map，key 为步骤ID，value 为csv文件ID列表
        Map<String, List<String>> stepsCsvMap = csvSteps.stream()
                .collect(Collectors.groupingBy(ApiScenarioCsvStep::getStepId, Collectors.mapping(ApiScenarioCsvStep::getFileId, Collectors.toList())));
        //将stepsCsvMap根据步骤id放入到allSteps中
        if (CollectionUtils.isNotEmpty(allSteps)) {
            allSteps.forEach(step -> step.setCsvFileIds(stepsCsvMap.get(step.getId())));
        }

        // 构造 map，key 为场景ID，value 为步骤列表
        Map<String, List<ApiScenarioStepDTO>> scenarioStepMap = allSteps.stream()
                .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getScenarioId()).orElse(StringUtils.EMPTY)));

        // 查询部分引用的步骤详情和接口定义的步骤详情
        Map<String, String> stepDetailMap = getStepDetailMap(allSteps);

        // key 为父步骤ID，value 为子步骤列表
        Map<String, List<ApiScenarioStepDTO>> parentStepMap = scenarioStepMap.get(scenarioId)
                .stream()
                .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getParentId()).orElse(StringUtils.EMPTY)));
        List<ApiScenarioStepDTO> steps = buildStepTree(parentStepMap.get(StringUtils.EMPTY), scenarioStepMap, scenarioStepMap);
        // 设置部分引用的步骤的启用状态
        setPartialRefStepsEnable(steps, stepDetailMap);

        apiScenarioDetail.setSteps(steps);

        return apiScenarioDetail;
    }

    /**
     * 设置部分引用的步骤的启用状态
     *
     * @param steps
     * @param stepDetailMap
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
     * 查询部分引用的步骤详情 和 接口定义的步骤详情
     *
     * @param allSteps
     */
    private Map<String, String> getStepDetailMap(List<ApiScenarioStepDTO> allSteps) {
        List<String> stepDetailIds = allSteps.stream().filter(step -> isRefApi(step.getStepType(), step.getRefType())
                        || StringUtils.equals(step.getRefType(), ApiScenarioStepRefType.PARTIAL_REF.name()))
                .map(ApiScenarioStepDTO::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(stepDetailIds)) {
            return new HashMap<>(0);
        }
        ApiScenarioBlobExample example = new ApiScenarioBlobExample();
        example.createCriteria().andIdIn(stepDetailIds);
        return apiScenarioBlobMapper.selectByExample(example).stream()
                .collect(Collectors.toMap(ApiScenarioBlob::getId, blob -> new String(blob.getConfig())));
    }

    /**
     * 判断步骤是否是引用的接口定义
     * 引用的接口定义允许修改参数值，需要特殊处理
     *
     * @param stepType
     * @param refType
     * @return
     */
    private boolean isRefApi(String stepType, String refType) {
        return StringUtils.equals(stepType, ApiScenarioStepType.API.name()) && StringUtils.equals(refType, ApiScenarioStepRefType.REF.name());
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
            List<ApiScenarioStepDTO> children = parentStepMap.get(step.getId());
            if (CollectionUtils.isEmpty(children)) {
                return;
            }
            if (isRefApiScenario(step)) {
                // 如果当前步骤是引用的场景，获取该场景的子步骤
                Map<String, List<ApiScenarioStepDTO>> childStepMap = scenarioStepMap.get(step.getResourceId())
                        .stream()
                        .collect(Collectors.groupingBy(ApiScenarioStepDTO::getParentId));
                step.setChildren(buildStepTree(children, childStepMap, scenarioStepMap));
            } else {
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
     *
     * @param step
     * @return
     */
    private boolean isRefApiScenario(ApiScenarioStepDTO step) {
        return isRef(step.getRefType()) && StringUtils.equals(step.getStepType(), ApiScenarioStepType.API_SCENARIO.name());
    }

    /**
     * 递归获取所有的场景步骤
     *
     * @param scenarioIds
     * @return
     */
    private List<ApiScenarioStepDTO> getAllStepsByScenarioIds(List<String> scenarioIds) {
        List<ApiScenarioStepDTO> steps = getStepDTOByScenarioIds(scenarioIds);
        if (CollectionUtils.isEmpty(steps)) {
            return steps;
        }

        // 将 config 转换成对象
        steps.stream().forEach(step -> {
            if (step.getConfig() != null && StringUtils.isNotBlank(step.getConfig().toString())) {
                step.setConfig(JSON.parseObject(step.getConfig().toString()));
            }
        });

        // 获取步骤中引用的场景ID
        List<String> childScenarioIds = steps.stream()
                .filter(step -> isRefApiScenario(step))
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
        ApiScenarioStep step = checkStepExist(stepId);
        StepParser stepParser = StepParserFactory.getStepParser(step.getStepType());
        return stepParser.parseDetail(step);
    }

    private ApiScenarioStep checkStepExist(String id) {
        return ServiceUtils.checkResourceExist(apiScenarioStepMapper.selectByPrimaryKey(id), "permission.api_step.name");
    }

    public ApiScenarioBatchOperationResponse batchCopy(ApiScenarioBatchCopyRequest request, LogInsertModule logInsertModule) {
        if (!StringUtils.equals(request.getTargetModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
            ApiScenarioModule module = apiScenarioModuleMapper.selectByPrimaryKey(request.getTargetModuleId());
            if (module == null || !StringUtils.equals(module.getProjectId(), request.getProjectId())) {
                throw new MSException(Translator.get("module.not.exist"));
            }
        }

        List<String> scenarioIds = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(scenarioIds)) {
            return new ApiScenarioBatchOperationResponse();
        }
        request.setSelectIds(scenarioIds);
        ApiScenarioBatchOperationResponse response =
                ApiScenarioBatchOperationUtils.executeWithBatchOperationResponse(scenarioIds, sublist -> copyAndInsert(sublist, request, logInsertModule.getOperator()));
        apiScenarioLogService.saveBatchCopyLog(response, request.getProjectId(), logInsertModule);
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

    private ApiScenarioBatchOperationResponse copyAndInsert(List<String> scenarioIds, ApiScenarioBatchCopyRequest request, String operator) {
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

                    //todo 刚の没提交的CSV关联表

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

}