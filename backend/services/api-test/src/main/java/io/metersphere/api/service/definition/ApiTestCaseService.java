package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseService {

    public static final Long ORDER_STEP = 5000L;

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

    @NotNull
    private static ApiFileResourceUpdateRequest getFileResourceRequest(ApiTestCase testCase, List<String> filed, List<String> addFiled) {
        String apiCaseDir = DefaultRepositoryDir.getApiCaseDir(testCase.getProjectId(), testCase.getId());
        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(testCase.getProjectId());
        resourceUpdateRequest.setFileIds(filed);
        resourceUpdateRequest.setAddFileIds(addFiled);
        resourceUpdateRequest.setFolder(apiCaseDir);
        resourceUpdateRequest.setResourceId(testCase.getId());
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_CASE);
        return resourceUpdateRequest;
    }

    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    private ApiDefinition getApiDefinition(String apiDefinitionId) {
        //判断是否存在
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(apiDefinitionId).andDeletedEqualTo(false);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiDefinitions)) {
            throw new MSException(Translator.get("api_definition_not_exist"));
        }
        return apiDefinitions.get(0);
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

    public Long getNextOrder(String projectId) {
        Long pos = extApiTestCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public ApiTestCase addCase(ApiTestCaseAddRequest request, List<MultipartFile> files, String userId) {
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
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            testCase.setTags(JSON.toJSONString(request.getTags()));
        }
        apiTestCaseMapper.insertSelective(testCase);

        ApiTestCaseBlob caseBlob = new ApiTestCaseBlob();
        caseBlob.setId(testCase.getId());
        caseBlob.setRequest(request.getRequest().getBytes());
        apiTestCaseBlobMapper.insert(caseBlob);

        // 处理文件
        ApiFileResourceUpdateRequest resourceRequest =
                getFileResourceRequest(testCase, request.getFileIds(), request.getFileIds());
        apiFileResourceService.addFileResource(resourceRequest, files);
        return testCase;
    }

    private ApiTestCase checkResourceExist(String id) {
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(id);
        if (testCase == null) {
            throw new MSException(Translator.get("api_test_case_not_exist"));
        }
        return testCase;
    }

    public ApiTestCaseDTO get(String id, String userId) {
        ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
        ApiTestCase testCase = checkResourceExist(id);
        ApiTestCaseBlob testCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        BeanUtils.copyBean(apiTestCaseDTO, testCase);
        if (StringUtils.isNotBlank(testCase.getTags())) {
            apiTestCaseDTO.setTags(JSON.parseArray(testCase.getTags(), String.class));
        } else {
            apiTestCaseDTO.setTags(new ArrayList<>());
        }
        ApiDefinition apiDefinition = getApiDefinition(testCase.getApiDefinitionId());
        apiTestCaseDTO.setMethod(apiDefinition.getMethod());
        apiTestCaseDTO.setPath(apiDefinition.getPath());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(id).andUserIdEqualTo(userId);
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        apiTestCaseDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        apiTestCaseDTO.setRequest(ApiDataUtils.parseObject(new String(testCaseBlob.getRequest()), AbstractMsTestElement.class));
        apiTestCaseDTO.setFileIds(apiFileResourceService.getFileIdsByResourceId(id));
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

    private void checkApiExist(String id) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
        if (apiDefinitionMapper.countByExample(example) == 0) {
            throw new MSException(Translator.get("please_recover_the_interface_data_first"));
        }
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

    public ApiTestCase update(ApiTestCaseUpdateRequest request, List<MultipartFile> files, String userId) {
        ApiTestCase testCase = checkResourceExist(request.getId());
        BeanUtils.copyBean(testCase, request);
        checkNameExist(testCase);
        testCase.setUpdateUser(userId);
        testCase.setUpdateTime(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            testCase.setTags(JSON.toJSONString(request.getTags()));
        } else {
            testCase.setTags(null);
        }
        apiTestCaseMapper.updateByPrimaryKey(testCase);
        ApiTestCaseBlob apiTestCaseBlob = new ApiTestCaseBlob();
        apiTestCaseBlob.setId(request.getId());
        apiTestCaseBlob.setRequest(request.getRequest().getBytes());
        apiTestCaseBlobMapper.updateByPrimaryKeySelective(apiTestCaseBlob);

        ApiFileResourceUpdateRequest resourceRequest =
                getFileResourceRequest(testCase, request.getFileIds(), request.getAddFileIds());
        apiFileResourceService.updateFileResource(resourceRequest, files);
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

    public List<ApiTestCaseDTO> page(ApiTestCasePageRequest request, boolean deleted) {
        List<ApiTestCaseDTO> apiCaseLists = extApiTestCaseMapper.listByRequest(request, deleted);
        buildApiTestCaseDTO(apiCaseLists);
        return apiCaseLists;
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
            apiCaseLists.forEach(apiCase -> {
                apiCase.setCreateName(userMap.get(apiCase.getCreateUser()));
                apiCase.setUpdateName(userMap.get(apiCase.getUpdateUser()));
                apiCase.setDeleteName(userMap.get(apiCase.getDeleteUser()));
                if (StringUtils.isNotBlank(apiCase.getEnvironmentId())
                        && MapUtils.isNotEmpty(envMap)
                        && envMap.containsKey(apiCase.getEnvironmentId())) {
                    apiCase.setEnvironmentName(envMap.get(apiCase.getEnvironmentId()));
                }
            });
        }
    }

    public void recover(String id) {
        ApiTestCase testCase = checkResourceExist(id);
        checkApiExist(testCase.getApiDefinitionId());
        checkNameExist(testCase);
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId(id);
        apiTestCase.setDeleted(false);
        apiTestCase.setDeleteUser(null);
        apiTestCase.setDeleteTime(null);
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
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
        SubListUtils.dealForSubList(ids, 2000, subList -> {
            deleteResourceByIds(subList, request.getProjectId(), userId);
        });
    }

    public void deleteResourceByIds(List<String> ids, String projectId, String userId) {
        deleteFollows(ids);
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByIds(ids, true);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        apiTestCaseMapper.deleteByExample(example);
        ApiTestCaseBlobExample blobExample = new ApiTestCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        apiTestCaseBlobMapper.deleteByExample(blobExample);
        //删除文件关联关系
        ids.forEach(id -> {
            String apiCaseDir = DefaultRepositoryDir.getApiCaseDir(projectId, id);
            apiFileResourceService.deleteByResourceId(apiCaseDir, id);
        });
        //记录删除日志
        apiTestCaseLogService.deleteBatchLog(caseLists, userId, projectId);
        //TODO 需要删除测试计划与用例的中间表 功能用例的关联表等
        //TODO 删除附件关系
        //extFileAssociationService.deleteByResourceIds(ids);
    }

    private void deleteFollows(List<String> caseIds) {
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdIn(caseIds);
        apiTestCaseFollowerMapper.deleteByExample(example);
    }

    public List<String> doSelectIds(ApiTestCaseBatchRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> ids = extApiTestCaseMapper.getIds(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
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
        SubListUtils.dealForSubList(ids, 2000, subList -> {
            batchMoveToGc(subList, userId, projectId, saveLog);
        });
    }

    private void batchMoveToGc(List<String> ids, String userId, String projectId, boolean saveLog) {
        extApiTestCaseMapper.batchMoveGc(ids, userId);
        if (saveLog) {
            List<ApiTestCase> apiTestCases = extApiTestCaseMapper.getCaseInfoByIds(ids, true);
            apiTestCaseLogService.batchToGcLog(apiTestCases, userId, projectId);
        }
    }

    public void batchEdit(ApiCaseBatchEditRequest request, String userId) {
        List<String> ids = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        SubListUtils.dealForSubList(ids, 2000, subList -> {
            batchEditByType(request, subList, userId, request.getProjectId());
        });
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
            case PRIORITY -> batchUpdatePriority(example, updateCase, request.getPriority());
            case STATUS -> batchUpdateStatus(example, updateCase, request.getStatus());
            case TAGS ->
                    batchUpdateTags(example, updateCase, request.getTags(), request.isAppendTag(), sqlSession, mapper);
            case ENVIRONMENT -> batchUpdateEnvironment(example, updateCase, request.getEnvId());
            default -> throw new MSException(Translator.get("batch_edit_type_error"));
        }
        List<ApiTestCase> caseInfoByIds = extApiTestCaseMapper.getCaseInfoByIds(ids, false);
        apiTestCaseLogService.batchEditLog(caseInfoByIds, userId, projectId);
    }

    private void batchUpdateEnvironment(ApiTestCaseExample example, ApiTestCase updateCase, String envId) {
        if (StringUtils.isBlank(envId)) {
            throw new MSException(Translator.get("environment_id_is_null"));
        }
        Environment environment = environmentMapper.selectByPrimaryKey(envId);
        if (environment == null) {
            throw new MSException(Translator.get("environment_is_not_exist"));
        }
        updateCase.setEnvironmentId(envId);
        apiTestCaseMapper.updateByExampleSelective(updateCase, example);
    }

    private void batchUpdateTags(ApiTestCaseExample example, ApiTestCase updateCase,
                                 LinkedHashSet<String> tags, boolean appendTag,
                                 SqlSession sqlSession, ApiTestCaseMapper mapper) {
        if (CollectionUtils.isEmpty(tags)) {
            throw new MSException(Translator.get("tags_is_null"));
        }
        if (appendTag) {
            List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(caseList)) {
                caseList.forEach(apiTestCase -> {
                    if (StringUtils.isNotBlank(apiTestCase.getTags())) {
                        LinkedHashSet orgTags = ApiDataUtils.parseObject(apiTestCase.getTags(), LinkedHashSet.class);
                        orgTags.addAll(tags);
                        apiTestCase.setTags(JSON.toJSONString(orgTags));
                    } else {
                        apiTestCase.setTags(JSON.toJSONString(tags));
                    }
                    mapper.updateByPrimaryKey(apiTestCase);
                });
                sqlSession.flushStatements();
                if (sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        } else {
            updateCase.setTags(JSON.toJSONString(tags));
            apiTestCaseMapper.updateByExampleSelective(updateCase, example);
        }
    }

    private void batchUpdateStatus(ApiTestCaseExample example, ApiTestCase updateCase, String status) {
        if (StringUtils.isBlank(status)) {
            throw new MSException(Translator.get("status_is_null"));
        }
        updateCase.setStatus(status);
        apiTestCaseMapper.updateByExampleSelective(updateCase, example);
    }

    private void batchUpdatePriority(ApiTestCaseExample example, ApiTestCase updateCase, String priority) {
        if (StringUtils.isBlank(priority)) {
            throw new MSException(Translator.get("priority_is_null"));
        }
        updateCase.setPriority(priority);
        apiTestCaseMapper.updateByExampleSelective(updateCase, example);
    }

    public void editPos(PosRequest request) {
        ServiceUtils.updatePosField(request,
                ApiTestCase.class,
                apiTestCaseMapper::selectByPrimaryKey,
                extApiTestCaseMapper::getPrePos,
                extApiTestCaseMapper::getLastPos,
                apiTestCaseMapper::updateByPrimaryKeySelective);
    }
}
