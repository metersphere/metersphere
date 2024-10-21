package io.metersphere.api.service.definition;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiMockBatchEditRequest;
import io.metersphere.api.dto.definition.ApiTestCaseBatchRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.dto.mockserver.*;
import io.metersphere.api.dto.request.http.body.BinaryBody;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiDefinitionMockConfigMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMockMapper;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApiFileResourceType;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.OperationHistoryService;
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
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: LAN
 * @date: 2023/12/7 17:28
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionMockService {

    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ExtApiDefinitionMockMapper extApiDefinitionMockMapper;

    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;

    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiDefinitionMockLogService apiDefinitionMockLogService;
    @Resource
    private ApiDefinitionMockNoticeService apiDefinitionMockNoticeService;
    @Resource
    private OperationHistoryService operationHistoryService;
    @Resource
    private ApiCommonService apiCommonService;
    public static final String STATUS = "Status";
    public static final String TAGS = "Tags";
    private static final String MOCK_TABLE = "api_definition_mock";

    public List<ApiDefinitionMockDTO> getPage(ApiDefinitionMockPageRequest request) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        return extApiDefinitionMockMapper.list(request);
    }

    public ApiDefinitionMockDTO detail(ApiDefinitionMockRequest request) {
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(request.getId());
        ApiDefinitionMockDTO apiDefinitionMockDTO = new ApiDefinitionMockDTO();
        handleMockConfig(request.getId(), apiDefinitionMockDTO);
        handleApiDefinition(apiDefinitionMock.getApiDefinitionId(), apiDefinitionMockDTO);
        BeanUtils.copyBean(apiDefinitionMockDTO, apiDefinitionMock);
        return apiDefinitionMockDTO;
    }

    public void handleMockConfig(String id, ApiDefinitionMockDTO apiDefinitionMockDTO) {
        Optional<ApiDefinitionMockConfig> apiDefinitionMockConfigOptional = Optional.ofNullable(apiDefinitionMockConfigMapper.selectByPrimaryKey(id));
        apiDefinitionMockConfigOptional.ifPresent(config -> {
            MockMatchRule matchRule = ApiDataUtils.parseObject(new String(config.getMatching()), MockMatchRule.class);
            if (matchRule != null) {
                setLinkFileInfo(id, matchRule.getBody());
                apiDefinitionMockDTO.setMockMatchRule(matchRule);
            }
            if (config.getResponse() != null) {
                MockResponse httpResponses = ApiDataUtils.parseObject(new String(config.getResponse()), MockResponse.class);
                apiCommonService.setLinkFileInfo(id, httpResponses.getBody());
                apiDefinitionMockDTO.setResponse(httpResponses);
            }
        });
    }

    public void setLinkFileInfo(String id, BodyParamMatchRule body) {
        List<ApiFile> updateFiles = new ArrayList<>(0);
        if (body != null) {
            MockFormDataBody formDataBody = body.getFormDataBody();
            if (formDataBody != null) {
                List<FormKeyValueInfo> formValues = formDataBody.getMatchRules();
                if (CollectionUtils.isNotEmpty(formValues)) {
                    formValues.forEach(keyValueInfo -> {
                        List<ApiFile> files = keyValueInfo.getFiles();
                        if (CollectionUtils.isNotEmpty(files)) {
                            updateFiles.addAll(files);
                        }
                    });
                }
            }
            BinaryBody binaryBody = body.getBinaryBody();
            if (binaryBody != null && binaryBody.getFile() != null) {
                updateFiles.add(binaryBody.getFile());
            }
        }
        apiCommonService.setLinkFileInfo(id, updateFiles);
    }


    public void handleApiDefinition(String id, ApiDefinitionMockDTO apiDefinitionMockDTO) {
        Optional.ofNullable(apiDefinitionMapper.selectByPrimaryKey(id)).ifPresent(apiDefinition -> {
            apiDefinitionMockDTO.setApiNum(apiDefinition.getNum());
            apiDefinitionMockDTO.setApiName(apiDefinition.getName());
            apiDefinitionMockDTO.setApiPath(apiDefinition.getPath());
            apiDefinitionMockDTO.setApiMethod(apiDefinition.getMethod());
        });
    }

    /**
     * 校验Mock是否存在
     *
     * @param id mock id
     */
    private ApiDefinitionMock checkApiDefinitionMock(String id) {
        return ServiceUtils.checkResourceExist(apiDefinitionMockMapper.selectByPrimaryKey(id), "permission.api_mock.name");
    }

    public ApiDefinitionMock create(ApiDefinitionMockAddRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());

        if (request.getMockMatchRule() == null) {
            request.setMockMatchRule(new MockMatchRule());
        }
        if (request.getResponse() == null) {
            request.setResponse(new MockResponse());
        }

        ApiDefinitionMock apiDefinitionMock = new ApiDefinitionMock();
        BeanUtils.copyBean(apiDefinitionMock, request);
        checkAddExist(apiDefinitionMock);
        apiDefinitionMock.setId(IDGenerator.nextStr());
        apiDefinitionMock.setCreateTime(System.currentTimeMillis());
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMock.setCreateUser(userId);
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinitionMock.setTags(ServiceUtils.parseTags(request.getTags()));
        }
        apiDefinitionMock.setEnable(true);
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        apiDefinitionMock.setExpectNum(String.valueOf(NumGenerator.nextNum(request.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_MOCK)));
        apiDefinition.setVersionId(apiDefinition.getVersionId());
        apiDefinitionMockMapper.insertSelective(apiDefinitionMock);
        ApiDefinitionMockConfig apiDefinitionMockConfig = new ApiDefinitionMockConfig();
        apiDefinitionMockConfig.setId(apiDefinitionMock.getId());
        apiDefinitionMockConfig.setMatching(JSON.toJSONString(request.getMockMatchRule()).getBytes());
        apiDefinitionMockConfig.setResponse(JSON.toJSONString(request.getResponse()).getBytes());
        apiDefinitionMockConfigMapper.insertSelective(apiDefinitionMockConfig);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceRequest(apiDefinitionMock.getId(), apiDefinitionMock.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);

        return apiDefinitionMock;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceRequest(String sourceId, String projectId, String operator) {
        String apiDefinitionMockDir = DefaultRepositoryDir.getApiMockDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDefinitionMockDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiFileResourceType.API_MOCK);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_TEST_MANAGEMENT_MOCK);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_DEFINITION_MOCK);
        return resourceUpdateRequest;
    }

    private void checkAddExist(ApiDefinitionMock apiDefinitionMock) {
        ApiDefinitionMockExample example = new ApiDefinitionMockExample();
        example.createCriteria()
                .andNameEqualTo(apiDefinitionMock.getName()).andApiDefinitionIdEqualTo(apiDefinitionMock.getApiDefinitionId());
        if (apiDefinitionMockMapper.countByExample(example) > 0) {
            throw new MSException(ApiResultCode.API_DEFINITION_MOCK_EXIST);
        }
    }

    private void checkUpdateExist(ApiDefinitionMock apiDefinitionMock) {
        ApiDefinitionMockExample example = new ApiDefinitionMockExample();
        example.createCriteria()
                .andIdNotEqualTo(apiDefinitionMock.getId())
                .andNameEqualTo(apiDefinitionMock.getName()).andApiDefinitionIdEqualTo(apiDefinitionMock.getApiDefinitionId());
        if (apiDefinitionMockMapper.countByExample(example) > 0) {
            throw new MSException(ApiResultCode.API_DEFINITION_MOCK_EXIST);
        }
    }

    public ApiDefinitionMock update(ApiDefinitionMockUpdateRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(request.getId());
        BeanUtils.copyBean(apiDefinitionMock, request);
        checkUpdateExist(apiDefinitionMock);
        apiDefinitionMock.setUpdateUser(userId);
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinitionMock.setTags(request.getTags());
        }
        apiDefinitionMockMapper.updateByPrimaryKeySelective(apiDefinitionMock);
        ApiDefinitionMockConfig apiDefinitionMockConfig = new ApiDefinitionMockConfig();
        apiDefinitionMockConfig.setId(apiDefinitionMock.getId());
        if (request.getMockMatchRule() != null) {
            apiDefinitionMockConfig.setMatching(JSON.toJSONString(request.getMockMatchRule()).getBytes());
        }
        if (request.getResponse() != null) {
            apiDefinitionMockConfig.setResponse(JSON.toJSONString(request.getResponse()).getBytes());
        }
        apiDefinitionMockConfigMapper.updateByPrimaryKeySelective(apiDefinitionMockConfig);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceRequest(apiDefinitionMock.getId(), apiDefinitionMock.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        resourceUpdateRequest.setUnLinkFileIds(request.getUnLinkFileIds());
        resourceUpdateRequest.setDeleteFileIds(request.getDeleteFileIds());
        apiFileResourceService.updateFileResource(resourceUpdateRequest);

        return apiDefinitionMock;
    }

    public void delete(ApiDefinitionMockRequest request, String userId) {
        checkApiDefinitionMock(request.getId());
        String apiDefinitionMockDir = DefaultRepositoryDir.getApiMockDir(request.getProjectId(), request.getId());
        apiFileResourceService.deleteByResourceId(apiDefinitionMockDir, request.getId(), request.getProjectId(), userId, OperationLogModule.API_TEST_MANAGEMENT_MOCK);
        apiDefinitionMockConfigMapper.deleteByPrimaryKey(request.getId());
        apiDefinitionMockMapper.deleteByPrimaryKey(request.getId());
    }

    public ApiDefinitionMock copy(ApiDefinitionMockRequest request, String userId) {
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(request.getId());
        apiDefinitionMock.setId(IDGenerator.nextStr());
        apiDefinitionMock.setName(getCopyName(apiDefinitionMock.getName()));
        apiDefinitionMock.setCreateTime(System.currentTimeMillis());
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMock.setCreateUser(userId);
        apiDefinitionMock.setEnable(true);
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        apiDefinitionMock.setExpectNum(String.valueOf(NumGenerator.nextNum(request.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_MOCK)));
        apiDefinitionMockMapper.insertSelective(apiDefinitionMock);

        Optional<ApiDefinitionMockConfig> apiDefinitionMockConfigOptional = Optional.ofNullable(apiDefinitionMockConfigMapper.selectByPrimaryKey(request.getId()));
        apiDefinitionMockConfigOptional.ifPresent(config -> {
            ApiDefinitionMockConfig apiDefinitionMockConfig = new ApiDefinitionMockConfig();
            apiDefinitionMockConfig.setId(apiDefinitionMock.getId());
            apiDefinitionMockConfig.setMatching(config.getMatching());
            apiDefinitionMockConfig.setResponse(config.getResponse());
            apiDefinitionMockConfigMapper.insertSelective(apiDefinitionMockConfig);
        });

        String sourceDir = DefaultRepositoryDir.getApiMockDir(apiDefinitionMock.getProjectId(), request.getId());
        String targetDir = DefaultRepositoryDir.getApiMockDir(apiDefinitionMock.getProjectId(), apiDefinitionMock.getId());
        apiFileResourceService.copyFileByResourceId(request.getId(), sourceDir, apiDefinitionMock.getId(), targetDir);

        return apiDefinitionMock;
    }

    private String getCopyName(String name) {
        String copyName = "copy_" + name;
        if (copyName.length() > 200) {
            copyName = copyName.substring(0, 195) + copyName.substring(copyName.length() - 5);
        }
        return copyName;
    }

    public void updateEnable(String id, String userId) {
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(id);
        ApiDefinitionMock update = new ApiDefinitionMock();
        update.setId(id);
        update.setEnable(!apiDefinitionMock.getEnable());
        update.setUpdateTime(System.currentTimeMillis());
        update.setUpdateUser(userId);
        apiDefinitionMockMapper.updateByPrimaryKeySelective(update);
    }

    public void deleteByApiIds(List<String> apiIds, String userId, String projectId) {
        ApiDefinitionMockExample apiDefinitionMockExample = new ApiDefinitionMockExample();
        apiDefinitionMockExample.createCriteria().andApiDefinitionIdIn(apiIds);

        List<ApiDefinitionMock> apiDefinitionMocks = apiDefinitionMockMapper.selectByExample(apiDefinitionMockExample);

        if (!apiDefinitionMocks.isEmpty()) {
            List<String> mockIds = apiDefinitionMocks.stream().map(ApiDefinitionMock::getId).toList();
            String apiDefinitionMockDir = DefaultRepositoryDir.getApiMockDir(projectId, StringUtils.EMPTY);
            apiFileResourceService.deleteByResourceIds(apiDefinitionMockDir, mockIds, projectId, userId, OperationLogModule.API_TEST_MANAGEMENT_MOCK);

            ApiDefinitionMockConfigExample apiDefinitionMockConfigExample = new ApiDefinitionMockConfigExample();
            apiDefinitionMockConfigExample.createCriteria().andIdIn(mockIds);
            apiDefinitionMockConfigMapper.deleteByExample(apiDefinitionMockConfigExample);

            apiDefinitionMockMapper.deleteByExample(apiDefinitionMockExample);
        }
    }

    public String getMockUrl(String id) {
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(id);
        //检查接口是否存在
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        if (apiDefinition == null) {
            throw new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST);
        }
        // 获取mock环境
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(apiDefinitionMock.getProjectId()).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        if (CollectionUtils.isNotEmpty(environments)) {
            EnvironmentInfoDTO environmentInfoDTO = environmentService.get(environments.getFirst().getId());
            return StringUtils.join(environmentInfoDTO.getConfig().getHttpConfig().getFirst().getUrl(), "/", apiDefinition.getNum(), apiDefinition.getPath());
        }

        return null;
    }

    public void batchDelete(ApiTestCaseBatchRequest request, String userId) {
        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 500, subList -> deleteResourceByIds(subList, request.getProjectId(), userId));
        }
    }

    public void deleteResourceByIds(List<String> ids, String projectId, String userId) {
        List<ApiDefinitionMock> mockList = extApiDefinitionMockMapper.getMockInfoByIds(ids);

        // 批量删除关联文件
        String apiMockDir = DefaultRepositoryDir.getApiMockDir(projectId, StringUtils.EMPTY);
        apiFileResourceService.deleteByResourceIds(apiMockDir, ids, projectId, userId, OperationLogModule.API_TEST_MANAGEMENT_MOCK);

        ApiDefinitionMockExample example = new ApiDefinitionMockExample();
        example.createCriteria().andIdIn(ids);
        apiDefinitionMockMapper.deleteByExample(example);
        ApiDefinitionMockConfigExample blobExample = new ApiDefinitionMockConfigExample();
        blobExample.createCriteria().andIdIn(ids);
        apiDefinitionMockConfigMapper.deleteByExample(blobExample);
        //记录删除日志
        apiDefinitionMockLogService.deleteBatchLog(mockList, userId, projectId);
        apiDefinitionMockNoticeService.batchSendNotice(ids, userId, projectId, NoticeConstants.Event.MOCK_DELETE);
    }

    public void batchEdit(ApiMockBatchEditRequest request, String userId) {

        List<String> ids = doSelectIds(request);
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 500, subList -> batchEditByType(request, subList, userId, request.getProjectId()));
        }
    }

    private void batchEditByType(ApiMockBatchEditRequest request, List<String> ids, String userId, String projectId) {
        ApiDefinitionMockExample example = new ApiDefinitionMockExample();
        example.createCriteria().andIdIn(ids);
        ApiDefinitionMock updateCase = new ApiDefinitionMock();
        updateCase.setUpdateUser(userId);
        updateCase.setUpdateTime(System.currentTimeMillis());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMockMapper mapper = sqlSession.getMapper(ApiDefinitionMockMapper.class);
        switch (request.getType()) {
            case STATUS -> batchUpdateStatus(example, updateCase, request.isEnable(), mapper);
            case TAGS -> batchUpdateTags(example, updateCase, request, ids, mapper);
            default -> throw new MSException(Translator.get("batch_edit_type_error"));
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        List<ApiDefinitionMock> mockInfoByIds = extApiDefinitionMockMapper.getMockInfoByIds(ids);
        apiDefinitionMockLogService.batchEditLog(mockInfoByIds, userId, projectId);
        apiDefinitionMockNoticeService.batchSendNotice(ids, userId, projectId, NoticeConstants.Event.MOCK_UPDATE);
    }

    private void batchUpdateTags(ApiDefinitionMockExample example, ApiDefinitionMock updateMock,
                                 ApiMockBatchEditRequest request, List<String> ids,
                                 ApiDefinitionMockMapper mapper) {
        if (request.isAppend()) {
            Map<String, ApiDefinitionMock> mockMap = extApiDefinitionMockMapper.getTagsByIds(ids)
                    .stream()
                    .collect(Collectors.toMap(ApiDefinitionMock::getId, Function.identity()));
            if (MapUtils.isNotEmpty(mockMap)) {
                mockMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v.getTags())) {
                        List<String> orgTags = v.getTags();
                        orgTags.addAll(request.getTags());
                        v.setTags(ServiceUtils.parseTags(orgTags.stream().distinct().toList()));
                    } else {
                        v.setTags(request.getTags());
                    }
                    v.setUpdateTime(updateMock.getUpdateTime());
                    v.setUpdateUser(updateMock.getUpdateUser());
                    mapper.updateByPrimaryKeySelective(v);
                });
            }
        } else {
            updateMock.setTags(request.isClear() ? new ArrayList<>() : ServiceUtils.parseTags(request.getTags()));
            mapper.updateByExampleSelective(updateMock, example);
        }
    }

    private void batchUpdateStatus(ApiDefinitionMockExample example, ApiDefinitionMock updateMock, boolean enable, ApiDefinitionMockMapper mapper) {
        updateMock.setEnable(enable);
        mapper.updateByExampleSelective(updateMock, example);
    }

    public List<String> doSelectIds(ApiTestCaseBatchRequest request) {
        if (request.isSelectAll() && CollectionUtils.isNotEmpty(request.getProtocols())) {
            List<String> ids = extApiDefinitionMockMapper.getIds(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return new ArrayList<>(ids.stream().distinct().toList());
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

    public List<OperationHistoryDTO> operationHistoryList(OperationHistoryRequest request) {
        return operationHistoryService.listWidthTable(request, MOCK_TABLE);
    }
}
