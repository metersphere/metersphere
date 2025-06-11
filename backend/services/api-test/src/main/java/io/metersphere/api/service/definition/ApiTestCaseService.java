package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.scenario.ApiFileCopyRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.HttpRequestParamDiffUtils;
import io.metersphere.functional.domain.FunctionalCaseTestExample;
import io.metersphere.functional.mapper.FunctionalCaseTestMapper;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.MoveNodeService;
import io.metersphere.sdk.constants.ApiFileResourceType;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseService extends MoveNodeService {

    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String TAGS = "Tags";
    public static final String ENVIRONMENT = "Environment";
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestCaseFollowerMapper apiTestCaseFollowerMapper;
    @Resource
    private UserLoginService userLoginService;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiTestCaseLogService apiTestCaseLogService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private OperationHistoryService operationHistoryService;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiTestCaseNoticeService apiTestCaseNoticeService;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private FunctionalCaseTestMapper functionalCaseTestMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    private static final String CASE_TABLE = "api_test_case";
    @Resource
    private ApiReportRelateTaskMapper apiReportRelateTaskMapper;

    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    public ApiDefinition getApiDefinition(String apiDefinitionId) {
        //判断是否存在
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(apiDefinitionId).andDeletedEqualTo(false);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiDefinitions)) {
            throw new MSException(Translator.get("api_definition_not_exist"));
        }
        return apiDefinitions.getFirst();
    }

    public void checkNameExist(ApiTestCase apiTestCase) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(apiTestCase.getProjectId())
                .andApiDefinitionIdEqualTo(apiTestCase.getApiDefinitionId())
                .andNameEqualTo(apiTestCase.getName()).andIdNotEqualTo(apiTestCase.getId()).andDeletedEqualTo(false);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            throw new MSException(Translator.get("api_test_case_name_exist"));
        }
    }

    public long getNextOrder(String projectId) {
        Long pos = extApiTestCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiDebugDir = DefaultRepositoryDir.getApiCaseDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDebugDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiFileResourceType.API_CASE);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_TEST_MANAGEMENT_CASE);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_TEST_CASE);
        return resourceUpdateRequest;
    }

    public ApiTestCase addCase(ApiTestCaseAddRequest request, String userId) {
        ApiTestCase testCase = new ApiTestCase();
        testCase.setId(IDGenerator.nextStr());
        BeanUtils.copyBean(testCase, request);
        ApiDefinition apiDefinition = getApiDefinition(request.getApiDefinitionId());
        testCase.setNum(NumGenerator.nextNum(request.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_TEST_CASE));
        testCase.setApiDefinitionId(request.getApiDefinitionId());
        testCase.setName(request.getName());
        testCase.setPos(getNextOrder(request.getProjectId()));
        testCase.setProjectId(request.getProjectId());
        checkProjectExist(testCase.getProjectId());
        checkNameExist(testCase);
        testCase.setVersionId(apiDefinition.getVersionId());
        testCase.setPriority(request.getPriority());
        testCase.setStatus(request.getStatus());
        testCase.setCreateUser(userId);
        testCase.setUpdateUser(userId);
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCase.setAiCreate(request.getAiCreate());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            testCase.setTags(ServiceUtils.parseTags(request.getTags()));
        }
        apiTestCaseMapper.insertSelective(testCase);

        ApiTestCaseBlob caseBlob = new ApiTestCaseBlob();
        caseBlob.setId(testCase.getId());
        caseBlob.setRequest(getMsTestElementStr(request.getRequest()).getBytes());
        apiTestCaseBlobMapper.insert(caseBlob);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(testCase.getId(), testCase.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);
        return testCase;
    }

    public String getMsTestElementStr(Object request) {
        String requestStr = JSON.toJSONString(request);
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(requestStr, AbstractMsTestElement.class);
        // 手动校验参数
        ServiceUtils.validateParam(msTestElement);
        return requestStr;
    }

    public ApiTestCase checkResourceExist(String id) {
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(id);
        if (testCase == null) {
            throw new MSException(Translator.get("api_test_case_not_exist"));
        }
        return testCase;
    }

    private ApiTestCase checkResourceNoDeleted(String id) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
        List<ApiTestCase> testCase = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(testCase)) {
            throw new MSException(Translator.get("api_test_case_not_exist"));
        }
        return testCase.getFirst();
    }

    public ApiTestCaseDTO get(String id, String userId) {
        ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
        ApiTestCase testCase = checkResourceNoDeleted(id);
        ApiTestCaseBlob testCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        BeanUtils.copyBean(apiTestCaseDTO, testCase);
        if (CollectionUtils.isNotEmpty(testCase.getTags())) {
            apiTestCaseDTO.setTags(testCase.getTags());
        } else {
            apiTestCaseDTO.setTags(new ArrayList<>());
        }
        ApiDefinition apiDefinition = getApiDefinition(testCase.getApiDefinitionId());
        apiTestCaseDTO.setMethod(apiDefinition.getMethod());
        apiTestCaseDTO.setPath(apiDefinition.getPath());
        apiTestCaseDTO.setProtocol(apiDefinition.getProtocol());
        apiTestCaseDTO.setApiDefinitionNum(apiDefinition.getNum());
        apiTestCaseDTO.setApiDefinitionName(apiDefinition.getName());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        apiTestCaseDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        AbstractMsTestElement msTestElement = getTestElement(testCaseBlob);
        apiCommonService.setLinkFileInfo(id, msTestElement);
        apiCommonService.setEnableCommonScriptProcessorInfo(msTestElement);
        apiCommonService.setApiDefinitionExecuteInfo(msTestElement, apiDefinition);
        apiTestCaseDTO.setRequest(msTestElement);

        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        AbstractMsTestElement apiMsTestElement = getApiMsTestElement(apiDefinitionBlob);
        apiTestCaseDTO.setInconsistentWithApi(HttpRequestParamDiffUtils.isRequestParamDiff(apiMsTestElement, msTestElement));
        return apiTestCaseDTO;
    }

    public void deleteToGc(String id, String userId) {
        checkResourceExist(id);
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId(id);
        apiTestCase.setDeleted(true);
        apiTestCase.setDeleteUser(userId);
        apiTestCase.setDeleteTime(System.currentTimeMillis());
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
    }

    public void follow(String id, String userId) {
        checkResourceExist(id);
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(followers)) {
            ApiTestCaseFollower follower = new ApiTestCaseFollower();
            follower.setCaseId(id);
            follower.setUserId(userId);
            apiTestCaseFollowerMapper.insert(follower);
        }
    }

    public void unfollow(String id, String userId) {
        checkResourceExist(id);
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        apiTestCaseFollowerMapper.deleteByExample(example);
    }

    public ApiTestCase update(ApiTestCaseUpdateRequest request, String userId) {
        ApiTestCase testCase = checkResourceExist(request.getId());
        BeanUtils.copyBean(testCase, request);
        checkNameExist(testCase);
        testCase.setUpdateUser(userId);
        testCase.setUpdateTime(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            testCase.setTags(ServiceUtils.parseTags(request.getTags()));
        } else {
            testCase.setTags(null);
        }
        apiTestCaseMapper.updateByPrimaryKey(testCase);
        ApiTestCaseBlob apiTestCaseBlob = new ApiTestCaseBlob();
        apiTestCaseBlob.setId(request.getId());
        apiTestCaseBlob.setRequest(getMsTestElementStr(request.getRequest()).getBytes());
        apiTestCaseBlobMapper.updateByPrimaryKeySelective(apiTestCaseBlob);

        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(testCase.getId(), testCase.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        resourceUpdateRequest.setUnLinkFileIds(request.getUnLinkFileIds());
        resourceUpdateRequest.setDeleteFileIds(request.getDeleteFileIds());
        apiFileResourceService.updateFileResource(resourceUpdateRequest);
        return testCase;
    }

    public void updateStatus(String id, String status, String userId) {
        checkResourceExist(id);
        ApiTestCase update = new ApiTestCase();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateUser(userId);
        update.setUpdateTime(System.currentTimeMillis());
        apiTestCaseMapper.updateByPrimaryKeySelective(update);
    }

    public List<ApiTestCaseDTO> page(ApiTestCasePageRequest request, boolean deleted, boolean isRepeat, String testPlanId) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        List<ApiTestCaseDTO> apiCaseLists = extApiTestCaseMapper.listByRequest(request, deleted, isRepeat, testPlanId);
        buildApiTestCaseDTO(apiCaseLists);
        return apiCaseLists;
    }

    public List<ApiTestCaseDTO> calculate(List<String> ids) {
        List<ApiTestCaseDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<CasePassDTO> passRateList = extApiTestCaseMapper.findPassRateByIds(ids);
            Map<String, String> passRates = passRateList.stream().collect(Collectors.toMap(CasePassDTO::getId, CasePassDTO::getValue));
            for (String id : ids) {
                ApiTestCaseDTO dto = new ApiTestCaseDTO();
                dto.setId(id);
                dto.setPassRate(passRates.getOrDefault(id, "0%"));
                returnList.add(dto);
            }
        }
        return returnList;
    }

    private void buildApiTestCaseDTO(List<ApiTestCaseDTO> apiCaseLists) {
        if (CollectionUtils.isNotEmpty(apiCaseLists)) {
            List<String> userIds = new ArrayList<>();
            userIds.addAll(apiCaseLists.stream().map(ApiTestCaseDTO::getCreateUser).toList());
            userIds.addAll(apiCaseLists.stream().map(ApiTestCaseDTO::getUpdateUser).toList());
            userIds.addAll(apiCaseLists.stream().map(ApiTestCaseDTO::getDeleteUser).toList());
            Map<String, String> userMap = userLoginService.getUserNameMap(userIds.stream().filter(StringUtils::isNotBlank).distinct().toList());
            List<String> envIds = apiCaseLists.stream().map(ApiTestCaseDTO::getEnvironmentId).toList();
            EnvironmentExample environmentExample = new EnvironmentExample();
            environmentExample.createCriteria().andIdIn(envIds);
            List<Environment> environments = environmentMapper.selectByExample(environmentExample);
            Map<String, String> envMap = environments.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));
            List<String> ids = apiCaseLists.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
            List<CasePassDTO> passRateList = extApiTestCaseMapper.findPassRateByIds(ids);
            Map<String, String> passRates = passRateList.stream().collect(Collectors.toMap(CasePassDTO::getId, CasePassDTO::getValue));
            //取模块id为新的set
            List<String> moduleIds = apiCaseLists.stream().map(ApiTestCaseDTO::getModuleId).distinct().toList();
            ApiDefinitionModuleExample moduleExample = new ApiDefinitionModuleExample();
            moduleExample.createCriteria().andIdIn(moduleIds);
            List<ApiDefinitionModule> modules = apiDefinitionModuleMapper.selectByExample(moduleExample);
            //生成map key为id value为name
            Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(ApiDefinitionModule::getId, ApiDefinitionModule::getName));
            apiCaseLists.forEach(apiCase -> {
                apiCase.setPassRate(passRates.get(apiCase.getId()));
                apiCase.setCreateName(userMap.get(apiCase.getCreateUser()));
                apiCase.setUpdateName(userMap.get(apiCase.getUpdateUser()));
                apiCase.setDeleteName(userMap.get(apiCase.getDeleteUser()));
                apiCase.setModulePath(StringUtils.isNotBlank(moduleMap.get(apiCase.getModuleId())) ? moduleMap.get(apiCase.getModuleId()) : Translator.get("api_unplanned_request"));
                if (StringUtils.isNotBlank(apiCase.getEnvironmentId())
                        && MapUtils.isNotEmpty(envMap)
                        && envMap.containsKey(apiCase.getEnvironmentId())) {
                    apiCase.setEnvironmentName(envMap.get(apiCase.getEnvironmentId()));
                }
            });
        }
    }

    public void batchRecover(List<ApiTestCase> apiTestCases, String userId, String projectId) {
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            apiTestCases.forEach(apiTestCase -> {
                checkResourceExist(apiTestCase.getId());
                checkNameExist(apiTestCase);
                apiTestCase.setDeleted(false);
                apiTestCase.setDeleteUser(null);
                apiTestCase.setDeleteTime(null);
                apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
            });
            //记录恢复日志
            apiTestCaseLogService.batchRecoverLog(apiTestCases, userId, projectId);
        }
    }

    public void delete(String id, String userId) {
        ApiTestCase apiCase = checkResourceExist(id);
        deleteResourceByIds(List.of(id), apiCase.getProjectId(), userId);
    }

    public void batchDelete(ApiTestCaseBatchRequest request, String userId) {

        List<String> ids = doSelectIds(request, true);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        SubListUtils.dealForSubList(ids, 200, subList -> deleteResourceByIds(subList, request.getProjectId(), userId));
    }

    public void deleteResourceByIds(List<String> ids, String projectId, String userId) {
        deleteFollows(ids);
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByIds(ids, true);

        // 批量删除关联文件
        String apiCaseDirPrefix = DefaultRepositoryDir.getApiCaseDir(projectId, StringUtils.EMPTY);
        apiFileResourceService.deleteByResourceIds(apiCaseDirPrefix, ids, projectId, userId, OperationLogModule.API_TEST_MANAGEMENT_CASE);

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        apiTestCaseMapper.deleteByExample(example);
        ApiTestCaseBlobExample blobExample = new ApiTestCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        apiTestCaseBlobMapper.deleteByExample(blobExample);
        //记录删除日志
        apiTestCaseLogService.deleteBatchLog(caseLists, userId, projectId);
        //TODO 需要删除测试计划与用例的中间表 功能用例的关联表等
        FunctionalCaseTestExample functionalCaseTestExample = new FunctionalCaseTestExample();
        functionalCaseTestExample.createCriteria().andSourceIdIn(ids).andSourceTypeEqualTo("API");
        functionalCaseTestMapper.deleteByExample(functionalCaseTestExample);

        //TODO 删除附件关系    不需要删除报告
        //extFileAssociationService.deleteByResourceIds(ids);
    }

    private void deleteFollows(List<String> caseIds) {
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdIn(caseIds);
        apiTestCaseFollowerMapper.deleteByExample(example);
    }

    public List<String> doSelectIds(ApiTestCaseBatchRequest request, boolean deleted) {
        if (request.isSelectAll() && CollectionUtils.isNotEmpty(request.getProtocols())) {
            List<String> ids = extApiTestCaseMapper.getIds(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return new ArrayList<>(ids.stream().distinct().toList());
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

    public void batchMoveGc(ApiTestCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request, false);
        batchDeleteToGc(ids, userId, request.getProjectId(), true);
    }

    public void batchDeleteToGc(List<String> ids, String userId, String projectId, boolean saveLog) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        long deleteTime = System.currentTimeMillis();
        SubListUtils.dealForSubList(ids, 2000, subList -> batchMoveToGc(subList, userId, projectId, saveLog, deleteTime));
    }

    private void batchMoveToGc(List<String> ids, String userId, String projectId, boolean saveLog, long deleteTime) {
        extApiTestCaseMapper.batchMoveGc(ids, userId, deleteTime);
        if (saveLog) {
            List<ApiTestCase> apiTestCases = extApiTestCaseMapper.getCaseInfoByIds(ids, true);
            apiTestCaseLogService.batchToGcLog(apiTestCases, userId, projectId);
        }
        apiTestCaseNoticeService.batchSendNotice(ids, userId, projectId, NoticeConstants.Event.CASE_DELETE);
    }

    public void batchEdit(ApiCaseBatchEditRequest request, String userId) {
        List<String> ids = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        SubListUtils.dealForSubList(ids, 500, subList -> batchEditByType(request, subList, userId, request.getProjectId()));
    }

    private void batchEditByType(ApiCaseBatchEditRequest request, List<String> ids, String userId, String projectId) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        ApiTestCase updateCase = new ApiTestCase();
        updateCase.setUpdateUser(userId);
        updateCase.setUpdateTime(System.currentTimeMillis());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiTestCaseMapper mapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        switch (request.getType()) {
            case PRIORITY -> batchUpdatePriority(example, updateCase, request.getPriority(), mapper);
            case STATUS -> batchUpdateStatus(example, updateCase, request.getStatus(), mapper);
            case TAGS -> batchUpdateTags(example, updateCase, request, ids, mapper);
            case ENVIRONMENT -> batchUpdateEnvironment(example, updateCase, request.getEnvironmentId(), mapper);
            default -> throw new MSException(Translator.get("batch_edit_type_error"));
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        List<ApiTestCase> caseInfoByIds = extApiTestCaseMapper.getCaseInfoByIds(ids, false);
        apiTestCaseLogService.batchEditLog(caseInfoByIds, userId, projectId);
        apiTestCaseNoticeService.batchSendNotice(ids, userId, projectId, NoticeConstants.Event.CASE_UPDATE);
    }

    private void batchUpdateEnvironment(ApiTestCaseExample example, ApiTestCase updateCase, String envId, ApiTestCaseMapper mapper) {
        if (StringUtils.isBlank(envId)) {
            throw new MSException(Translator.get("environment_id_is_null"));
        }
        Environment environment = environmentMapper.selectByPrimaryKey(envId);
        if (environment == null) {
            throw new MSException(Translator.get("environment_is_not_exist"));
        }
        updateCase.setEnvironmentId(envId);
        mapper.updateByExampleSelective(updateCase, example);
    }

    private void batchUpdateTags(ApiTestCaseExample example, ApiTestCase updateCase,
                                 ApiCaseBatchEditRequest request, List<String> ids,
                                 ApiTestCaseMapper mapper) {
        if (request.isAppend()) {
            Map<String, ApiTestCase> caseMap = extApiTestCaseMapper.getTagsByIds(ids, false)
                    .stream()
                    .collect(Collectors.toMap(ApiTestCase::getId, Function.identity()));
            if (MapUtils.isNotEmpty(caseMap)) {
                caseMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v.getTags())) {
                        List<String> orgTags = v.getTags();
                        orgTags.addAll(request.getTags());
                        v.setTags(ServiceUtils.parseTags(orgTags.stream().distinct().toList()));
                    } else {
                        v.setTags(request.getTags());
                    }
                    v.setUpdateTime(updateCase.getUpdateTime());
                    v.setUpdateUser(updateCase.getUpdateUser());
                    mapper.updateByPrimaryKeySelective(v);
                });
            }
        } else {
            updateCase.setTags(request.isClear() ? new ArrayList<>() : ServiceUtils.parseTags(request.getTags()));
            mapper.updateByExampleSelective(updateCase, example);
        }
    }

    private void batchUpdateStatus(ApiTestCaseExample example, ApiTestCase updateCase, String status, ApiTestCaseMapper mapper) {
        if (StringUtils.isBlank(status)) {
            throw new MSException(Translator.get("status_is_null"));
        }
        updateCase.setStatus(status);
        mapper.updateByExampleSelective(updateCase, example);
    }

    private void batchUpdatePriority(ApiTestCaseExample example, ApiTestCase updateCase, String priority, ApiTestCaseMapper mapper) {
        if (StringUtils.isBlank(priority)) {
            throw new MSException(Translator.get("priority_is_null"));
        }
        updateCase.setPriority(priority);
        mapper.updateByExampleSelective(updateCase, example);
    }

    public List<ApiTestCaseBlob> getBlobByIds(List<String> apiCaseIds) {
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return Collections.emptyList();
        }
        ApiTestCaseBlobExample example = new ApiTestCaseBlobExample();
        example.createCriteria().andIdIn(apiCaseIds);
        return apiTestCaseBlobMapper.selectByExampleWithBLOBs(example);
    }

    public List<ExecuteReportDTO> getExecuteList(ExecutePageRequest request) {
        List<ExecHistoryDTO> historyList = extApiScenarioMapper.selectExecHistory(request);
        List<ExecuteReportDTO> executeList = handleList(historyList);
        return executeList;
    }

    private List<ExecuteReportDTO> handleList(List<ExecHistoryDTO> historyList) {
        List<ExecuteReportDTO> executeReportDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(historyList)) {
            List<String> userIds = historyList.stream().map(ExecHistoryDTO::getCreateUser).toList();
            Map<String, String> userNameMap = userLoginService.getUserNameMap(userIds);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Map<String, String> reportMap = getReportMap(historyList);
            historyList.forEach(item -> {
                ExecuteReportDTO reportDTO = new ExecuteReportDTO();
                BeanUtils.copyBean(reportDTO, item);
                reportDTO.setOperationUser(userNameMap.get(item.getCreateUser()));
                reportDTO.setId(item.getItemId());
                reportDTO.setNum(sdf.format(item.getStartTime()));
                if (StringUtils.isNotBlank(item.getTestPlanNum())) {
                    reportDTO.setTestPlanNum(StringUtils.join(Translator.get("test_plan"), ": ", item.getTestPlanNum()));
                }
                if (BooleanUtils.isTrue(item.getIntegrated()) && reportMap.containsKey(item.getTaskId())) {
                    reportDTO.setResultDeleted(false);
                }
                if (BooleanUtils.isFalse(item.getIntegrated()) && reportMap.containsKey(item.getItemId())) {
                    reportDTO.setResultDeleted(false);
                }
                executeReportDTOList.add(reportDTO);
            });
        }
        return executeReportDTOList;
    }

    private Map<String, String> getReportMap(List<ExecHistoryDTO> list) {
        Map<String, String> reportMap = new HashMap<>();
        List<String> integratedTaskIds = list.stream().filter(item -> BooleanUtils.isTrue(item.getIntegrated())).map(ExecHistoryDTO::getTaskId).toList();
        List<String> noIntegratedTaskItemIds = list.stream().filter(item -> BooleanUtils.isFalse(item.getIntegrated())).map(ExecHistoryDTO::getItemId).toList();
        List<String> resourceIds = ListUtils.union(integratedTaskIds, noIntegratedTaskItemIds);
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
            example.createCriteria().andTaskResourceIdIn(resourceIds);
            List<ApiReportRelateTask> reportRelateTasks = apiReportRelateTaskMapper.selectByExample(example);
            reportRelateTasks.stream().forEach(item -> reportMap.put(item.getTaskResourceId(), item.getReportId()));
        }
        return reportMap;
    }

    public List<OperationHistoryDTO> operationHistoryList(OperationHistoryRequest request) {
        return operationHistoryService.listWidthTable(request, CASE_TABLE);
    }

    public void updatePriority(String id, String priority, String userId) {
        checkResourceExist(id);
        ApiTestCase update = new ApiTestCase();
        update.setId(id);
        update.setPriority(priority);
        update.setUpdateUser(userId);
        update.setUpdateTime(System.currentTimeMillis());
        apiTestCaseMapper.updateByPrimaryKeySelective(update);
    }

    public List<ApiDefinitionExecuteInfo> getModuleInfoByIds(List<String> apiCaseIds) {
        // 获取接口定义ID和用例ID的映射
        List<ApiTestCase> apiTestCases = extApiTestCaseMapper.getApiCaseDefinitionInfo(apiCaseIds);

        List<String> definitionIds = apiTestCases.stream()
                .map(ApiTestCase::getApiDefinitionId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(definitionIds)) {
            return List.of();
        }

        Map<String, ApiDefinitionExecuteInfo> definitionExecuteInfoMap = extApiDefinitionMapper.getApiDefinitionExecuteInfo(definitionIds).stream()
                .collect(Collectors.toMap(ApiDefinitionExecuteInfo::getResourceId, Function.identity()));

        return apiTestCases.stream().map(apiTestCase -> {
                    ApiDefinitionExecuteInfo apiDefinitionExecuteInfo = definitionExecuteInfoMap.get(apiTestCase.getApiDefinitionId());
                    if (apiDefinitionExecuteInfo == null) {
                        return null;
                    } else {
                        // 将 resourceId 从定义ID替换成用例ID
                        apiDefinitionExecuteInfo = BeanUtils.copyBean(new ApiDefinitionExecuteInfo(), apiDefinitionExecuteInfo);
                        apiDefinitionExecuteInfo.setResourceId(apiTestCase.getId());
                        return apiDefinitionExecuteInfo;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void handleFileAssociationUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(originFileAssociation.getSourceId());
        if (apiTestCaseBlob == null) {
            return;
        }
        AbstractMsTestElement msTestElement = getTestElement(apiTestCaseBlob);
        // 获取接口中需要更新的文件
        List<ApiFile> updateFiles = apiCommonService.getApiFilesByFileId(originFileAssociation.getFileId(), msTestElement);
        // 替换文件的Id和name
        apiCommonService.replaceApiFileInfo(updateFiles, newFileMetadata);

        // 如果有需要更新的文件，则更新 request 字段
        if (CollectionUtils.isNotEmpty(updateFiles)) {
            apiTestCaseBlob.setRequest(ApiDataUtils.toJSONString(msTestElement).getBytes());
            apiTestCaseBlobMapper.updateByPrimaryKeySelective(apiTestCaseBlob);
        }
    }


    public String transfer(ApiTransferRequest request, String userId) {
        return apiFileResourceService.transfer(request, userId, ApiResourceType.API_CASE.name());
    }

    @Override
    public void updatePos(String id, long pos) {
        extApiTestCaseMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> posIds = extApiTestCaseMapper.selectIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtApiTestCaseMapper batchUpdateMapper = sqlSession.getMapper(ExtApiTestCaseMapper.class);
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
                extApiTestCaseMapper::selectDragInfoById,
                extApiTestCaseMapper::selectNodeByPosOperator
        );
        this.sort(sortDTO);
    }

    public List<ReferenceDTO> getReference(ReferenceRequest request) {
        return extApiDefinitionMapper.getReference(request);
    }

    /**
     * 处理接口定义参数变更
     *
     * @param changeRequest
     * @param originRequest
     */
    public void handleApiParamChange(String apiDefinitionId, Object changeRequest, Object originRequest) {
        boolean requestParamDifferent = HttpRequestParamDiffUtils.isRequestParamDiff(changeRequest, originRequest);
        if (requestParamDifferent) {
            // 添加接口变更标识
            extApiTestCaseMapper.setApiChangeByApiDefinitionId(apiDefinitionId);
        }
    }

    public void clearApiChange(String id) {
        checkResourceExist(id);
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId(id);
        apiTestCase.setApiChange(false);
        apiTestCase.setIgnoreApiDiff(true);
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
    }

    public ApiCaseCompareData getApiCompareData(String id) {
        ApiTestCase apiTestCase = checkResourceExist(id);
        ApiDefinition apiDefinition = getApiDefinition(apiTestCase.getApiDefinitionId());
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        AbstractMsTestElement apiMsTestElement = getApiMsTestElement(apiDefinitionBlob);
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        AbstractMsTestElement apiTestCaseMsTestElement = getTestElement(apiTestCaseBlob);
        // 其他协议不处理
        if (apiMsTestElement instanceof MsHTTPElement apiHttpTestElement && apiTestCaseMsTestElement instanceof MsHTTPElement apiCaseHttpTestElement) {
            apiMsTestElement = HttpRequestParamDiffUtils.getCompareHttpElement(apiHttpTestElement);
            apiTestCaseMsTestElement = HttpRequestParamDiffUtils.getCompareHttpElement(apiCaseHttpTestElement);
        }
        ApiCaseCompareData apiCaseCompareData = new ApiCaseCompareData();
        apiCaseCompareData.setApiRequest(apiMsTestElement);
        apiCaseCompareData.setCaseRequest(apiTestCaseMsTestElement);
        return apiCaseCompareData;
    }

    private AbstractMsTestElement getApiMsTestElement(ApiDefinitionBlob apiDefinitionBlob) {
        return ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class);
    }

    public AbstractMsTestElement getTestElement(ApiTestCaseBlob apiTestCaseBlob) {
        return ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class);
    }

    public void ignoreApiChange(String id, boolean ignore) {
        checkResourceExist(id);
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId(id);
        if (ignore) {
            apiTestCase.setApiChange(false);
            apiTestCase.setIgnoreApiDiff(true);
            apiTestCase.setIgnoreApiChange(true);
        } else {
            apiTestCase.setIgnoreApiChange(false);
        }
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
    }

    public void batchSyncApiChange(ApiCaseBatchSyncRequest request, String userId) {
        // 只处理 http 协议的接口
        request.setProtocols(List.of(ApiConstants.HTTP_PROTOCOL));
        List<String> ids = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        SubListUtils.dealForSubList(ids, 500, subList -> doBatchSyncApiChange(request, subList, userId, project));
    }

    public void doBatchSyncApiChange(ApiCaseBatchSyncRequest request, List<String> ids, String userId, Project project) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        Set<String> apiDefinitionIds = apiTestCases.stream().map(ApiTestCase::getApiDefinitionId).collect(Collectors.toSet());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiTestCaseBlobMapper apiTestCaseBlobBatchMapper = sqlSession.getMapper(ApiTestCaseBlobMapper.class);
        ApiTestCaseMapper apiTestCaseBatchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

        ApiCaseSyncRequest apiCaseSyncRequest = new ApiCaseSyncRequest();
        apiCaseSyncRequest.setSyncItems(request.getSyncItems());
        apiCaseSyncRequest.setDeleteRedundantParam(request.getDeleteRedundantParam());

        Map<String, ApiTestCaseLogDTO> originMap = new HashMap<>();
        Map<String, ApiTestCaseLogDTO> modifiedMap = new HashMap<>();

        ApiDefinitionBlobExample apiDefinitionBlobExample = new ApiDefinitionBlobExample();
        apiDefinitionBlobExample.createCriteria().andIdIn(new ArrayList<>(apiDefinitionIds));
        Map<String, ApiDefinitionBlob> apiDefinitionBlobMap = apiDefinitionBlobMapper.selectByExampleWithBLOBs(apiDefinitionBlobExample)
                .stream()
                .collect(Collectors.toMap(ApiDefinitionBlob::getId, Function.identity()));

        // 清除接口变更标识
        extApiTestCaseMapper.clearApiChange(ids);
        try {
            for (ApiTestCase apiTestCase : apiTestCases) {
                ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMap.get(apiTestCase.getApiDefinitionId());
                AbstractMsTestElement apiMsTestElement = getApiMsTestElement(apiDefinitionBlob);
                ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
                AbstractMsTestElement apiTestCaseMsTestElement = getTestElement(apiTestCaseBlob);
                boolean requestParamDifferent = HttpRequestParamDiffUtils.isRequestParamDiff(apiMsTestElement, apiTestCaseMsTestElement);
                if (requestParamDifferent) {
                    // 如果参数与定义不一致，则同步参数，并记录日志和发送通知
                    ApiTestCaseLogDTO originCase = BeanUtils.copyBean(new ApiTestCaseLogDTO(), apiTestCase);
                    originCase.setRequest(apiTestCaseMsTestElement);
                    originMap.put(apiTestCase.getId(), originCase);

                    apiTestCase.setUpdateTime(System.currentTimeMillis());
                    apiTestCase.setUpdateUser(userId);
                    apiTestCase.setApiChange(false);
                    apiTestCaseBatchMapper.updateByPrimaryKeySelective(apiTestCase);
                    apiTestCaseMsTestElement = HttpRequestParamDiffUtils.syncRequestDiff(apiCaseSyncRequest, apiMsTestElement, apiTestCaseMsTestElement);
                    apiTestCaseBlob.setRequest(ApiDataUtils.toJSONString(apiTestCaseMsTestElement).getBytes());
                    apiTestCaseBlobBatchMapper.updateByPrimaryKeySelective(apiTestCaseBlob);

                    ApiTestCaseLogDTO modifiedCase = BeanUtils.copyBean(new ApiTestCaseLogDTO(), apiTestCase);
                    modifiedCase.setRequest(apiTestCaseMsTestElement);
                    modifiedMap.put(apiTestCase.getId(), originCase);
                }
            }
            apiTestCaseLogService.batchSyncLog(originMap, modifiedMap, project, userId);

            User user = userMapper.selectByPrimaryKey(userId);
            apiTestCaseNoticeService.batchSyncSendNotice(new ArrayList<>(modifiedMap.values()), user, project.getId(), request.getNotificationConfig(), NoticeConstants.Event.CASE_UPDATE);
        } finally {
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public AbstractMsTestElement syncApiChange(ApiCaseSyncRequest request) {
        ApiTestCase apiTestCase = checkResourceExist(request.getId());
        ApiDefinition apiDefinition = getApiDefinition(apiTestCase.getApiDefinitionId());
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        AbstractMsTestElement apiMsTestElement = getApiMsTestElement(apiDefinitionBlob);
        AbstractMsTestElement apiTestCaseMsTestElement = ApiDataUtils.parseObject(JSON.toJSONString(request.getApiCaseRequest()), AbstractMsTestElement.class);
        // 清除接口变更标识
        extApiTestCaseMapper.clearApiChange(List.of(request.getId()));
        return HttpRequestParamDiffUtils.syncRequestDiff(request, apiMsTestElement, apiTestCaseMsTestElement);
    }

    public Map<String, String> copyFile(ApiFileCopyRequest request) {
        // 从接口定义复制
        ApiTestCase apiTestCase = checkResourceExist(request.getResourceId());
        String sourceDir = DefaultRepositoryDir.getApiCaseDir(apiTestCase.getProjectId(),
                apiTestCase.getId());

        return apiCommonService.copyFiles2TempDir(request.getFileIds(), sourceDir);
    }
}
