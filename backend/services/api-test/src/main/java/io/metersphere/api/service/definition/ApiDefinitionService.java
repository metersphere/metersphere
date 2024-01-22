package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiDefinitionDocType;
import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.importdto.ApiDefinitionImport;
import io.metersphere.api.dto.definition.importdto.ApiDefinitionImportDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.ImportParser;
import io.metersphere.api.parser.ImportParserFactory;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.domain.OperationLogBlob;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.OperationLogBlobMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.CustomFieldUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionService {

    public static final Long ORDER_STEP = 5000L;

    private static final String ALL_API = "api_definition_module.api.all";

    private static final String UNPLANNED_API = "api_unplanned_request";

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private ApiDefinitionFollowerMapper apiDefinitionFollowerMapper;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Resource
    private UserLoginService userLoginService;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private ApiTestCaseService apiTestCaseService;


    @Resource
    private ApiFileResourceService apiFileResourceService;

    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;

    @Resource
    private ApiDefinitionCustomFieldMapper apiDefinitionCustomFieldMapper;

    @Resource
    private ExtApiDefinitionCustomFieldMapper extApiDefinitionCustomFieldMapper;

    @Resource
    private ApiDefinitionLogService apiDefinitionLogService;
    @Resource
    private ApiDefinitionImportUtilService apiDefinitionImportUtilService;

    @Resource
    private ApiDefinitionMockService apiDefinitionMockService;

    @Resource
    private OperationHistoryService operationHistoryService;

    @Resource
    private OperationLogBlobMapper operationLogBlobMapper;

    public List<ApiDefinitionDTO> getApiDefinitionPage(ApiDefinitionPageRequest request, String userId) {
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request, userId);
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
            processApiDefinitions(list, request.getProjectId());
        }
        return list;
    }

    public List<ApiDefinitionDTO> getDocPage(ApiDefinitionPageRequest request, String userId) {
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request, userId);
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
            processApiDefinitionsDoc(list);
        }
        return list;
    }

    private void processApiDefinitionsDoc(List<ApiDefinitionDTO> list) {
        Set<String> userIds = extractUserIds(list);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));

        list.forEach(item -> {
            // Convert User IDs to Names
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));

            // Convert Blob
            Optional<ApiDefinitionBlob> apiDefinitionBlobOptional = Optional.ofNullable(apiDefinitionBlobMapper.selectByPrimaryKey(item.getId()));
            apiDefinitionBlobOptional.ifPresent(blob -> {
                item.setRequest(ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class));
                // blob.getResponse() 为 null 时不进行转换
                if (blob.getResponse() != null) {
                    item.setResponse(ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class));
                }
            });
        });
    }

    public ApiDefinitionDTO get(String id, String userId) {
        // 1. 避免重复查询数据库，将查询结果传递给get方法
        ApiDefinition apiDefinition = checkApiDefinition(id);
        return getApiDefinitionInfo(id, userId, apiDefinition);
    }

    public ApiDefinition create(ApiDefinitionAddRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, request);
        checkAddExist(apiDefinition);
        apiDefinition.setId(IDGenerator.nextStr());
        apiDefinition.setNum(getNextNum(request.getProjectId()));
        apiDefinition.setPos(getNextOrder(request.getProjectId()));
        apiDefinition.setLatest(true);
        apiDefinition.setStatus(request.getStatus());
        apiDefinition.setCreateUser(userId);
        apiDefinition.setUpdateUser(userId);
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId())));
        apiDefinition.setRefId(apiDefinition.getId());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinition.setTags(request.getTags());
        }
        apiDefinitionMapper.insertSelective(apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        apiDefinitionBlob.setId(apiDefinition.getId());
        apiDefinitionBlob.setRequest(request.getRequest().getBytes());
        apiDefinitionBlob.setResponse(request.getResponse().getBytes());
        apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(apiDefinition.getId(), apiDefinition.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);

        //保存自定义字段
        Map<String, String> customFields = request.getCustomFields();
        if (MapUtils.isNotEmpty(customFields)) {
            List<ApiDefinitionCustomField> list = new ArrayList<>();
            customFields.keySet().forEach(key -> createNewCustomField(apiDefinition.getId(), key, customFields.get(key), list));
            batchInsertCustomFields(list);
        }

        return apiDefinition;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDefinitionDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_DEFINITION);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_DEFINITION);
        return resourceUpdateRequest;
    }

    public ApiDefinition update(ApiDefinitionUpdateRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDefinition originApiDefinition = checkApiDefinition(request.getId());
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, request);
        if (request.getProtocol().equals(ModuleConstants.NODE_PROTOCOL_HTTP)) {
            checkUpdateExist(apiDefinition);
            //http协议的接口，如果修改了path和method，需要同步把case的path和method修改
            if (!originApiDefinition.getPath().equals(apiDefinition.getPath()) || !originApiDefinition.getMethod().equals(apiDefinition.getMethod())) {
                List<String> ids = new ArrayList<>();
                ids.add(request.getId());
                apiTestCaseService.updateByApiDefinitionId(ids, apiDefinition);
            }
        }
        apiDefinition.setStatus(request.getStatus());
        apiDefinition.setUpdateUser(userId);
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setVersionId(request.getVersionId());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinition.setTags(request.getTags());
        }
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        apiDefinitionBlob.setId(apiDefinition.getId());
        apiDefinitionBlob.setRequest(request.getRequest().getBytes());
        apiDefinitionBlob.setResponse(request.getResponse().getBytes());
        apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);

        // 自定义字段
        handleUpdateCustomFields(request, false);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(originApiDefinition.getId(), originApiDefinition.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        resourceUpdateRequest.setUnLinkRefIds(request.getUnLinkRefIds());
        resourceUpdateRequest.setDeleteFileIds(request.getDeleteFileIds());
        apiFileResourceService.updateFileResource(resourceUpdateRequest);

        return apiDefinition;
    }

    public void batchUpdate(ApiDefinitionBatchUpdateRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false, userId);
        // 记录更新前的数据
        apiDefinitionLogService.batchUpdateLog(ids, userId, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            if (request.getType().equals("tags")) {
                handleTags(request, userId, ids);
            } else if (request.getType().equals("customs")) {
                // 自定义字段处理
                ApiDefinitionCustomFieldDTO customField = request.getCustomField();
                Map<String, String> customFieldMap = Collections.singletonMap(customField.getId(), customField.getValue());
                ApiDefinitionUpdateRequest apiDefinitionUpdateRequest = new ApiDefinitionUpdateRequest();
                BeanUtils.copyBean(apiDefinitionUpdateRequest, request);
                apiDefinitionUpdateRequest.setCustomFields(customFieldMap);
                ids.forEach(id -> {
                    apiDefinitionUpdateRequest.setId(id);
                    handleUpdateCustomFields(apiDefinitionUpdateRequest, request.isAppend());
                });
            } else {
                ApiDefinition apiDefinition = new ApiDefinition();
                BeanUtils.copyBean(apiDefinition, request);
                apiDefinition.setUpdateUser(userId);
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
                apiDefinitionExample.createCriteria().andIdIn(ids);
                apiDefinitionMapper.updateByExampleSelective(apiDefinition, apiDefinitionExample);
            }
        }
    }


    private void handleUpdateCustomFields(ApiDefinitionUpdateRequest request, boolean append) {
        Map<String, String> customFields = request.getCustomFields();
        if (MapUtils.isNotEmpty(customFields)) {
            List<ApiDefinitionCustomField> addFields = new ArrayList<>();
            List<ApiDefinitionCustomField> updateFields = new ArrayList<>();
            List<ApiDefinitionCustomFieldDTO> originalFields = extApiDefinitionCustomFieldMapper.getApiCustomFields(List.of(request.getId()), request.getProjectId());
            Map<String, String> originalFieldMap = originalFields.stream().collect(Collectors.toMap(ApiDefinitionCustomFieldDTO::getId, ApiDefinitionCustomFieldDTO::getValue));
            customFields.keySet().forEach(fieldId -> {
                if (!originalFieldMap.containsKey(fieldId)) {
                    // New custom field relationship
                    createNewCustomField(request.getId(), fieldId, customFields.get(fieldId), addFields);
                } else {
                    // Existing custom field relationship
                    updateExistingCustomField(request.getId(), fieldId, append, customFields.get(fieldId), updateFields, originalFieldMap);
                }
            });

            batchInsertCustomFields(addFields);
            batchUpdateCustomFields(updateFields);
        }
    }

    private void createNewCustomField(String apiId, String fieldId, String value, List<ApiDefinitionCustomField> addFields) {
        ApiDefinitionCustomField apiDefinitionCustomField = new ApiDefinitionCustomField();
        apiDefinitionCustomField.setApiId(apiId);
        apiDefinitionCustomField.setFieldId(fieldId);
        apiDefinitionCustomField.setValue(value);
        addFields.add(apiDefinitionCustomField);
    }

    private void updateExistingCustomField(String apiId, String fieldId, boolean append, String value, List<ApiDefinitionCustomField> updateFields, Map<String, String> originalFieldMap) {
        ApiDefinitionCustomField apiDefinitionCustomField = new ApiDefinitionCustomField();
        apiDefinitionCustomField.setApiId(apiId);
        apiDefinitionCustomField.setFieldId(fieldId);
        if (append) {
            apiDefinitionCustomField.setValue(CustomFieldUtils.appendToMultipleCustomField(originalFieldMap.get(fieldId), value));
        } else {
            apiDefinitionCustomField.setValue(value);
        }
        updateFields.add(apiDefinitionCustomField);
    }

    private void batchInsertCustomFields(List<ApiDefinitionCustomField> addFields) {
        if (CollectionUtils.isNotEmpty(addFields)) {
            apiDefinitionCustomFieldMapper.batchInsert(addFields);
        }
    }

    private void batchUpdateCustomFields(List<ApiDefinitionCustomField> updateFields) {
        if (CollectionUtils.isNotEmpty(updateFields)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiDefinitionCustomFieldMapper apiCustomFieldMapper = sqlSession.getMapper(ApiDefinitionCustomFieldMapper.class);
            for (ApiDefinitionCustomField apiDefinitionCustomField : updateFields) {
                ApiDefinitionCustomFieldExample apiDefinitionCustomFieldExample = new ApiDefinitionCustomFieldExample();
                apiDefinitionCustomFieldExample.createCriteria().andApiIdEqualTo(apiDefinitionCustomField.getApiId()).andFieldIdEqualTo(apiDefinitionCustomField.getFieldId());
                apiCustomFieldMapper.updateByExample(apiDefinitionCustomField, apiDefinitionCustomFieldExample);
            }
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public ApiDefinition copy(ApiDefinitionCopyRequest request, String userId) {
        ApiDefinition copyApiDefinition = checkApiDefinition(request.getId());
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, copyApiDefinition);
        apiDefinition.setId(IDGenerator.nextStr());
        apiDefinition.setName(getCopyName(copyApiDefinition.getName()));
        apiDefinition.setStatus(copyApiDefinition.getStatus());
        apiDefinition.setNum(getNextNum(copyApiDefinition.getProjectId()));
        apiDefinition.setPos(getNextOrder(copyApiDefinition.getProjectId()));
        apiDefinition.setRefId(apiDefinition.getId());
        apiDefinition.setCreateUser(userId);
        apiDefinition.setUpdateUser(userId);
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.insertSelective(apiDefinition);

        ApiDefinitionBlob copyApiDefinitionBlob = getApiDefinitionBlob(request.getId());
        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        if (copyApiDefinitionBlob != null) {
            apiDefinitionBlob.setId(apiDefinition.getId());
            apiDefinitionBlob.setRequest(copyApiDefinitionBlob.getRequest());
            apiDefinitionBlob.setResponse(copyApiDefinitionBlob.getResponse());
            apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
        }

        String sourceDir = DefaultRepositoryDir.getApiDefinitionDir(apiDefinition.getProjectId(), request.getId());
        String targetDir = DefaultRepositoryDir.getApiDefinitionDir(apiDefinition.getProjectId(), apiDefinition.getId());
        apiFileResourceService.copyFileByResourceId(request.getId(), sourceDir, apiDefinition.getId(), targetDir);

        return apiDefinition;
    }

    public void delete(ApiDefinitionDeleteRequest request, String userId) {
        checkApiDefinition(request.getId());
        handleDeleteApiDefinition(Collections.singletonList(request.getId()), request.getDeleteAll(), request.getProjectId(), userId, false);
    }

    public void batchDelete(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false, userId);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleDeleteApiDefinition(ids, request.getDeleteAll(), request.getProjectId(), userId, true);
        }
    }

    public void batchMove(ApiDefinitionBatchMoveRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false, userId);
        if (!ids.isEmpty()) {
            // 移动接口所有版本引用的数据
            List<String> refIds = extApiDefinitionMapper.getRefIds(ids, false);
            if (!refIds.isEmpty()) {
                // 记录批量移动日志
                apiDefinitionLogService.batchMoveLog(extApiDefinitionMapper.getIdsByRefId(refIds, false), userId, request.getProjectId());

                extApiDefinitionMapper.batchMove(request, refIds, userId);
            }
        }
    }

    private void processApiDefinitions(List<ApiDefinitionDTO> list, String projectId) {
        Set<String> userIds = extractUserIds(list);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        List<String> apiDefinitionIds = list.stream().map(ApiDefinitionDTO::getId).toList();
        List<ApiCaseComputeDTO> apiCaseComputeList = extApiDefinitionMapper.selectApiCaseByIdsAndStatusIsNotTrash(apiDefinitionIds, projectId);
        Map<String, ApiCaseComputeDTO> resultMap = apiCaseComputeList.stream().collect(Collectors.toMap(ApiCaseComputeDTO::getApiDefinitionId, Function.identity()));

        List<ApiDefinitionCustomFieldDTO> customFields = extApiDefinitionCustomFieldMapper.getApiCustomFields(apiDefinitionIds, projectId);
        Map<String, List<ApiDefinitionCustomFieldDTO>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(ApiDefinitionCustomFieldDTO::getApiId));

        list.forEach(item -> {
            // Convert User IDs to Names
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));

            // Custom Fields
            item.setCustomFields(customFieldMap.get(item.getId()));

            // Calculate API Case Metrics
            ApiCaseComputeDTO apiCaseComputeDTO = resultMap.get(item.getId());
            if (apiCaseComputeDTO != null) {
                item.setCaseTotal(apiCaseComputeDTO.getCaseTotal());
                item.setCasePassRate(apiCaseComputeDTO.getCasePassRate());
                // 状态优先级 未执行，未通过，误报（FAKE_ERROR），通过
                if ((apiCaseComputeDTO.getError() + apiCaseComputeDTO.getFakeError() + apiCaseComputeDTO.getSuccess()) < apiCaseComputeDTO.getCaseTotal()) {
                    item.setCaseStatus(ApiReportStatus.PENDING.name());
                } else if (apiCaseComputeDTO.getError() > 0) {
                    item.setCaseStatus(ApiReportStatus.ERROR.name());
                } else if (apiCaseComputeDTO.getFakeError() > 0) {
                    item.setCaseStatus(ApiReportStatus.FAKE_ERROR.name());
                } else {
                    item.setCaseStatus(ApiReportStatus.SUCCESS.name());
                }
            } else {
                item.setCaseTotal(0);
                item.setCasePassRate("-");
                item.setCaseStatus("-");
            }
        });
    }

    private Set<String> extractUserIds(List<ApiDefinitionDTO> list) {
        return list.stream()
                .flatMap(apiDefinition -> Stream.of(apiDefinition.getUpdateUser(), apiDefinition.getDeleteUser(), apiDefinition.getCreateUser()))
                .collect(Collectors.toSet());
    }

    public Long getNextOrder(String projectId) {
        Long pos = extApiDefinitionMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.API_DEFINITION);
    }

    /**
     * 校验接口是否存在
     *
     * @param apiId 接口id
     */
    public ApiDefinition checkApiDefinition(String apiId) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiId);
        if (apiDefinition == null) {
            throw new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST);
        }
        return apiDefinition;
    }

    private void checkAddExist(ApiDefinition apiDefinition) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria()
                .andPathEqualTo(apiDefinition.getPath()).andMethodEqualTo(apiDefinition.getMethod())
                .andProtocolEqualTo(apiDefinition.getProtocol()).andVersionIdEqualTo(apiDefinition.getVersionId());
        if (CollectionUtils.isNotEmpty(apiDefinitionMapper.selectByExample(example))) {
            throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
        }
    }

    private void checkUpdateExist(ApiDefinition apiDefinition) {
        if (StringUtils.isNotEmpty(apiDefinition.getPath()) && StringUtils.isNotEmpty(apiDefinition.getMethod())) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria()
                    .andIdNotEqualTo(apiDefinition.getId()).andProtocolEqualTo(apiDefinition.getProtocol())
                    .andPathEqualTo(apiDefinition.getPath()).andMethodEqualTo(apiDefinition.getMethod()).andVersionIdEqualTo(apiDefinition.getVersionId());
            if (apiDefinitionMapper.countByExample(example) > 0) {
                throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
            }
        }
    }

    private ApiDefinitionBlob getApiDefinitionBlob(String apiId) {
        ApiDefinitionBlobExample apiDefinitionBlobExample = new ApiDefinitionBlobExample();
        apiDefinitionBlobExample.createCriteria().andIdEqualTo(apiId);
        return apiDefinitionBlobMapper.selectByPrimaryKey(apiId);
    }

    /**
     * 根据接口id 获取接口是否存在多个版本
     *
     * @param apiId 接口id
     */
    public List<ApiDefinitionVersionDTO> getApiDefinitionVersion(String apiId) {
        ApiDefinition apiDefinition = checkApiDefinition(apiId);
        return extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());
    }

    public void follow(String apiId, String userId) {
        checkApiDefinition(apiId);
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiId).andUserIdEqualTo(userId);
        if (apiDefinitionFollowerMapper.countByExample(example) > 0) {
            deleteFollower(apiId, userId);
        } else {
            ApiDefinitionFollower apiDefinitionFollower = new ApiDefinitionFollower();
            apiDefinitionFollower.setApiDefinitionId(apiId);
            apiDefinitionFollower.setUserId(userId);
            apiDefinitionFollowerMapper.insertSelective(apiDefinitionFollower);
        }
    }

    private void handleTags(ApiDefinitionBatchUpdateRequest request, String userId, List<String> ids) {
        if (request.isAppend()) {
            //追加标签
            Map<String, ApiDefinition> collect = extApiDefinitionMapper.getTagsByIds(ids, false)
                    .stream()
                    .collect(Collectors.toMap(ApiDefinition::getId, Function.identity()));
            try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                ApiDefinitionMapper definitionMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
                ids.forEach(id -> {
                    ApiDefinition apiDefinition = new ApiDefinition();
                    if (CollectionUtils.isNotEmpty(collect.get(id).getTags())) {
                        List<String> tags = collect.get(id).getTags();
                        tags.addAll(request.getTags());
                        apiDefinition.setTags(tags);
                    } else {
                        apiDefinition.setTags(request.getTags());
                    }
                    apiDefinition.setId(id);
                    apiDefinition.setUpdateTime(System.currentTimeMillis());
                    apiDefinition.setUpdateUser(userId);
                    definitionMapper.updateByPrimaryKeySelective(apiDefinition);
                });
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        } else {
            //替换标签
            ApiDefinition apiDefinition = new ApiDefinition();
            apiDefinition.setTags(request.getTags());
            apiDefinition.setProjectId(request.getProjectId());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiDefinition.setUpdateUser(userId);
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            apiDefinitionExample.createCriteria().andIdIn(ids).andDeletedEqualTo(false).andProjectIdEqualTo(request.getProjectId());
            apiDefinitionMapper.updateByExampleSelective(apiDefinition, apiDefinitionExample);
        }
    }

    private String getCopyName(String name) {
        String copyName = "copy_" + name;
        if (copyName.length() > 255) {
            copyName = copyName.substring(0, 250) + copyName.substring(copyName.length() - 5);
        }
        return copyName;
    }

    private void handleDeleteApiDefinition(List<String> ids, boolean deleteAll, String projectId, String userId, boolean isBatch) {
        if (deleteAll) {
            //全部删除  进入回收站
            List<String> refIds = extApiDefinitionMapper.getRefIds(ids, false);
            if (CollectionUtils.isNotEmpty(refIds)) {
                SubListUtils.dealForSubList(refIds, 2000, subRefIds -> {
                    List<String> delApiIds = extApiDefinitionMapper.getIdsByRefId(subRefIds, false);
                    SubListUtils.dealForSubList(delApiIds, 2000, subList -> {
                        if (CollectionUtils.isNotEmpty(delApiIds)) {
                            // 删除接口相关数据到回收站
                            deleteApiRelatedData(subList, userId, projectId);
                        }
                    });
                    // 记录删除到回收站的日志, 单条注解记录
                    if (isBatch) {
                        apiDefinitionLogService.batchDelLog(delApiIds, userId, projectId);
                    }
                    extApiDefinitionMapper.batchDeleteByRefId(subRefIds, userId, projectId);
                });
            }
        } else {
            // 列表删除
            if (!ids.isEmpty()) {
                SubListUtils.dealForSubList(ids, 2000, subList -> doDelete(subList, userId, projectId, isBatch));
            }
        }
    }

    private void deleteAfterAction(List<ApiDefinitionVersionDTO> apiDefinitionVersions) {
        apiDefinitionVersions.forEach(item -> {
            clearLatestVersion(item.getRefId(), item.getProjectId());
            ApiDefinition latestData = getLatestData(item.getRefId(), item.getProjectId());
            updateLatestVersion(latestData.getId(), latestData.getProjectId());
        });
    }

    // 清除多版本最新标识
    private void clearLatestVersion(String refId, String projectId) {
        extApiDefinitionMapper.clearLatestVersion(refId, projectId);
    }

    // 获取多版本最新一条数据
    private ApiDefinition getLatestData(String refId, String projectId) {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andRefIdEqualTo(refId).andDeletedEqualTo(false).andProjectIdEqualTo(projectId);
        apiDefinitionExample.setOrderByClause("update_time DESC");
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByExample(apiDefinitionExample).stream().findFirst().orElse(null);
        if (apiDefinition == null) {
            throw new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST);
        }
        return apiDefinition;
    }

    // 更新最新版本标识
    private void updateLatestVersion(String id, String projectId) {
        extApiDefinitionMapper.updateLatestVersion(id, projectId);
    }

    private void doDelete(List<String> ids, String userId, String projectId, boolean isBatch) {
        if (CollectionUtils.isNotEmpty(ids)) {
            // 需要判断是否存在多个版本问题
            ids.forEach(id -> {
                ApiDefinition apiDefinition = checkApiDefinition(id);
                // 删除的数据是否为最新版本的数据，如果是则需要查询是否有多版本数据存在，需要去除当前删除的数据，更新剩余版本数据中最近的一条数据为最新的数据
                if (apiDefinition.getLatest()) {
                    List<ApiDefinitionVersionDTO> apiDefinitionVersions = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());
                    if (apiDefinitionVersions.size() > 1) {
                        deleteAfterAction(apiDefinitionVersions);
                    }
                }
            });
            // 删除 case
            deleteApiRelatedData(ids, userId, projectId);
            // 记录删除到回收站的日志, 单条注解记录
            if (isBatch) {
                apiDefinitionLogService.batchDelLog(ids, userId, projectId);
            }
            // 删除接口到回收站
            extApiDefinitionMapper.batchDeleteById(ids, userId, projectId);
        }

    }

    private void deleteApiRelatedData(List<String> apiIds, String userId, String projectId) {
        // 是否存在 case 删除 case
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, false);
        if (CollectionUtils.isNotEmpty(caseLists)) {
            List<String> caseIds = caseLists.stream().map(ApiTestCase::getId).distinct().toList();
            apiTestCaseService.batchDeleteToGc(caseIds, userId, projectId, true);
        }
    }

    private void deleteFollower(String apiId, String userId) {
        apiDefinitionFollowerMapper.deleteByPrimaryKey(apiId, userId);
    }

    public void recover(ApiDefinitionDeleteRequest request, String userId) {
        // 恢复接口到接口列表
        handleRecoverApiDefinition(Collections.singletonList(request.getId()), userId, request.getProjectId(), false);
    }

    public void handleRecoverApiDefinition(List<String> ids, String userId, String projectId, boolean isBatch) {
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 2000, subList -> doRecover(subList, userId, projectId, isBatch));
        }
    }

    private void doRecover(List<String> apiIds, String userId, String projectId, boolean isBatch) {
        // 记录恢复数据之前的原数据日志，单条通过注解记录日志
        if (isBatch) {
            apiDefinitionLogService.batchRecoverLog(apiIds, userId, projectId);
        }
        extApiDefinitionMapper.batchRecoverById(apiIds, userId, projectId);

        List<String> updateApiIds = new ArrayList<>();
        apiIds.forEach(id -> {
            // 恢复数据恢复最新标识
            ApiDefinition apiDefinition = checkApiDefinition(id);
            // 判断是否存在多个版本
            List<ApiDefinitionVersionDTO> apiDefinitionVersions = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());

            if (CollectionUtils.isNotEmpty(apiDefinitionVersions) && apiDefinitionVersions.size() > 1) {
                handleMultipleVersions(apiDefinition);
            }

            ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(apiDefinition.getModuleId());
            // 判断接口的模块 ID 是否存在，不存在修改模块 ID 为未规划模块 ID
            if (!ModuleConstants.DEFAULT_NODE_ID.equals(apiDefinition.getModuleId()) && apiDefinitionModule == null) {
                updateApiIds.add(apiDefinition.getId());
            }

        });
        // 模块已删除，修改为未规划模块 ID
        handleModule(updateApiIds);
        // 恢复接口关联数据
        recoverApiRelatedData(apiIds, userId, projectId);
    }

    private void handleModule(List<String> updateApiIds) {
        if (!updateApiIds.isEmpty()) {
            ApiDefinition updateApiDefinition = new ApiDefinition();
            updateApiDefinition.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            apiDefinitionExample.createCriteria().andIdIn(updateApiIds);
            apiDefinitionMapper.updateByExampleSelective(updateApiDefinition, apiDefinitionExample);
        }
    }

    private void handleMultipleVersions(ApiDefinition apiDefinition) {
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(apiDefinition.getProjectId());
        // 清除所有最新标识
        clearLatestVersion(apiDefinition.getRefId(), apiDefinition.getProjectId());

        // 获取最新数据，恢复的数据最新标识，则最新数据，反之获取最新一条数据
        ApiDefinition latestData = apiDefinition.getLatest() ? apiDefinition : getLatestData(apiDefinition.getRefId(), apiDefinition.getProjectId());
        // 恢复的数据不为最新标识，同时接口版本为默认版本，则更新此数据为最新标识
        if (!latestData.getLatest() && latestData.getVersionId().equals(defaultVersion)) {
            updateLatestVersion(apiDefinition.getId(), apiDefinition.getProjectId());
        }
    }

    private void recoverApiRelatedData(List<String> apiIds, String userId, String projectId) {
        // 是否存在 case 恢复 case
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, true);
        if (CollectionUtils.isNotEmpty(caseLists)) {
            apiTestCaseService.batchRecover(caseLists, userId, projectId);
        }
    }

    public void trashDel(ApiDefinitionDeleteRequest request, String userId) {
        handleTrashDelApiDefinition(Collections.singletonList(request.getId()), userId, request.getProjectId(), false);
    }

    public void batchRecover(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), true, userId);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleRecoverApiDefinition(ids, userId, request.getProjectId(), true);
        }
    }

    public void batchTrashDel(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), true, userId);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleTrashDelApiDefinition(ids, userId, request.getProjectId(), true);
        }
    }

    private void handleTrashDelApiDefinition(List<String> ids, String userId, String projectId, boolean isBatch) {
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 2000, subList -> doTrashDel(subList, userId, projectId, isBatch));
        }
    }

    private void doTrashDel(List<String> ids, String userId, String projectId, boolean isBatch) {
        if (CollectionUtils.isNotEmpty(ids)) {
            // 删除上传的文件
            ids.forEach(id -> {
                String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, id);
                apiFileResourceService.deleteByResourceId(apiDefinitionDir, id, projectId, userId, OperationLogModule.API_DEFINITION);
            });
            // 删除接口关注人
            ApiDefinitionFollowerExample apiDefinitionFollowerExample = new ApiDefinitionFollowerExample();
            apiDefinitionFollowerExample.createCriteria().andApiDefinitionIdIn(ids).andUserIdEqualTo(userId);
            apiDefinitionFollowerMapper.deleteByExample(apiDefinitionFollowerExample);

            // 删除接口关联数据
            trashDelApiRelatedData(ids, userId, projectId);

            // 记录批量删除日志，单条删除通过注解记录
            if (isBatch) {
                apiDefinitionLogService.batchTrashDelLog(ids, userId, projectId);
            }
            // 删除接口
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            apiDefinitionExample.createCriteria().andIdIn(ids).andDeletedEqualTo(true).andProjectIdEqualTo(projectId);
            apiDefinitionMapper.deleteByExample(apiDefinitionExample);

        }
    }

    private void trashDelApiRelatedData(List<String> apiIds, String userId, String projectId) {
        // 是否存在 case 删除 case
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, true);
        if (CollectionUtils.isNotEmpty(caseLists)) {
            List<String> caseIds = caseLists.stream().map(ApiTestCase::getId).distinct().toList();
            // case 批量删除回收站
            apiTestCaseService.deleteResourceByIds(caseIds, projectId, userId);

        }
        // 删除 mock
        apiDefinitionMockService.deleteByApiIds(apiIds, userId);
    }

    // 获取批量操作选中的ID
    public <T> List<String> getBatchApiIds(T dto, String projectId, String protocol, boolean deleted, String userId) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
        if (request.isSelectAll()) {
            // 全选
            CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request.getCondition(), userId);
            List<String> ids = extApiDefinitionMapper.getIds(request, projectId, protocol, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public String uploadTempFile(MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
    }

    public ApiDefinitionDTO getApiDefinitionInfo(String id, String userId, ApiDefinition apiDefinition) {
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        // 2. 使用Optional避免空指针异常
        apiDefinitionLogService.handleBlob(id, apiDefinitionDTO);
        // 3. 查询自定义字段
        handleCustomFields(id, apiDefinition.getProjectId(), apiDefinitionDTO);
        // 3. 使用Stream简化集合操作
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(id).andUserIdEqualTo(userId);
        apiDefinitionDTO.setFollow(apiDefinitionFollowerMapper.countByExample(example) > 0);
        BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
        return apiDefinitionDTO;
    }

    public void handleCustomFields(String id, String projectId, ApiDefinitionDTO apiDefinitionDTO) {
        List<ApiDefinitionCustomFieldDTO> customFields = extApiDefinitionCustomFieldMapper.getApiCustomFields(Collections.singletonList(id), projectId);
        Map<String, List<ApiDefinitionCustomFieldDTO>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(ApiDefinitionCustomFieldDTO::getApiId));
        apiDefinitionDTO.setCustomFields(customFieldMap.get(id));
    }

    public ApiDefinitionDocDTO getDocInfo(ApiDefinitionDocRequest request) {
        ApiDefinitionDocDTO apiDefinitionDocDTO = new ApiDefinitionDocDTO();
        apiDefinitionDocDTO.setType(request.getType());
        // 下载所有/一个模块接口文档时，不做分页数据量大的时候会不会有性能问题，单独做接口
        if (ApiDefinitionDocType.ALL.name().equals(request.getType()) || ApiDefinitionDocType.MODULE.name().equals(request.getType())) {
            List<ApiDefinitionDTO> list = extApiDefinitionMapper.listDoc(request);
            if (!list.isEmpty()) {
                ApiDefinitionDTO first = list.get(0);
                apiDefinitionLogService.handleBlob(first.getId(), first);
                String docTitle;
                if (ApiDefinitionDocType.ALL.name().equals(request.getType())) {
                    docTitle = Translator.get(ALL_API);
                } else {
                    ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(first.getModuleId());
                    docTitle = (apiDefinitionModule != null) ? apiDefinitionModule.getName() : Translator.get(UNPLANNED_API);
                }
                apiDefinitionDocDTO.setDocTitle(docTitle);
                apiDefinitionDocDTO.setDocInfo(first);
            }
        } else if (ApiDefinitionDocType.API.name().equals(request.getType())) {
            ApiDefinition apiDefinition = checkApiDefinition(request.getApiId());
            ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
            BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
            apiDefinitionLogService.handleBlob(apiDefinition.getId(), apiDefinitionDTO);
            apiDefinitionDocDTO.setDocTitle(apiDefinitionDTO.getName());
            apiDefinitionDocDTO.setDocInfo(apiDefinitionDTO);
        }

        return apiDefinitionDocDTO;
    }

    public ApiDefinitionImport apiTestImport(MultipartFile file, ImportRequest request, String userId, String projectId) {
        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                String suffixName = originalFilename.substring(originalFilename.indexOf(".") + 1);
                apiDefinitionImportUtilService.checkFileSuffixName(request, suffixName);
            }
        }
        request.setUserId(userId);
        if (StringUtils.isBlank(request.getProjectId())) {
            request.setProjectId(projectId);
        }
        ImportParser<?> runService = ImportParserFactory.getImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        if (StringUtils.equals(request.getType(), "SCHEDULE")) {
            request.setProtocol("HTTP");
        }
        try {
            apiImport = (ApiDefinitionImport) Objects.requireNonNull(runService).parse(file == null ? null : file.getInputStream(), request);
            //TODO  处理mock数据
            // 发送通知
        } catch (Exception e) {
            // TODO 发送通知
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("parse_data_error"));
        }

        try {
            apiDefinitionImportUtilService.importApi(request, apiImport);
            apiDefinitionMapper.selectByExample(new ApiDefinitionExample());
            if (CollectionUtils.isNotEmpty(apiImport.getData())) {
                List<String> names = apiImport.getData().stream().map(ApiDefinitionImportDTO::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                List<String> ids = apiImport.getData().stream().map(ApiDefinitionImportDTO::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            // 发送通知
            //apiDefinitionImportUtilService.sendImportNotice(request, apiImportSendNoticeDTOS, project);
        } catch (Exception e) {
            //apiDefinitionImportUtilService.sendFailMessage(request, project);
            LogUtils.error(e);
            throw new MSException(Translator.get("user_import_format_wrong"));
        }
        return apiImport;
    }

    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        List<OperationHistoryDTO> operationHistoryList = operationHistoryService.list(request);
        if (CollectionUtils.isNotEmpty(operationHistoryList)) {
            List<String> apiIds = operationHistoryList.stream()
                    .map(OperationHistoryDTO::getSourceId).toList();

            Map<String, String> apiMap = extApiDefinitionMapper.selectVersionOptionByIds(apiIds).stream()
                    .collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));

            operationHistoryList.forEach(item -> item.setVersionName(apiMap.getOrDefault(item.getSourceId(), StringUtils.EMPTY)));
        }

        return operationHistoryList;
    }

    /**
     * 是否存在所选版本的接口定义，不存在则创建,同时创建日志记录
     */
    public void saveOperationHistory(OperationHistoryVersionRequest request) {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andRefIdEqualTo(request.getSourceId()).andVersionIdEqualTo(request.getVersionId());
        List<ApiDefinition> matchingApiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        ApiDefinition apiDefinition = matchingApiDefinitions.stream().findFirst().orElseGet(ApiDefinition::new);
        ApiDefinitionBlob copyApiDefinitionBlob = getApiDefinitionBlob(request.getSourceId());
        if (apiDefinition.getId() == null) {
            ApiDefinition copyApiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getSourceId());
            BeanUtils.copyBean(apiDefinition, copyApiDefinition);
            apiDefinition.setId(IDGenerator.nextStr());
            apiDefinition.setRefId(request.getSourceId());
            apiDefinition.setVersionId(request.getVersionId());
            apiDefinition.setCreateTime(System.currentTimeMillis());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiDefinitionMapper.insertSelective(apiDefinition);

            ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
            if (copyApiDefinitionBlob != null) {
                apiDefinitionBlob.setId(apiDefinition.getId());
                apiDefinitionBlob.setRequest(copyApiDefinitionBlob.getRequest());
                apiDefinitionBlob.setResponse(copyApiDefinitionBlob.getResponse());
                apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
            }
        }
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
        Optional.ofNullable(copyApiDefinitionBlob)
                .map(blob -> new String(blob.getRequest()))
                .ifPresent(requestBlob -> apiDefinitionDTO.setRequest(ApiDataUtils.parseObject(requestBlob, AbstractMsTestElement.class)));

        Optional.ofNullable(copyApiDefinitionBlob)
                .map(blob -> new String(blob.getResponse()))
                .ifPresent(responseBlob -> apiDefinitionDTO.setResponse(ApiDataUtils.parseArray(responseBlob, HttpResponse.class)));

        Long logId = apiDefinitionLogService.saveOperationHistoryLog(apiDefinitionDTO, apiDefinition.getCreateUser(), apiDefinition.getProjectId());
        operationHistoryService.associationRefId(request.getId(), logId);
    }


    public void recoverOperationHistory(OperationHistoryVersionRequest request) {
        OperationLogBlob operationLogBlob = operationLogBlobMapper.selectByPrimaryKey(request.getId());
        ApiDefinitionDTO apiDefinitionDTO = ApiDataUtils.parseObject(new String(operationLogBlob.getOriginalValue()), ApiDefinitionDTO.class);
        Long logId = recoverApiDefinition(apiDefinitionDTO);
        operationHistoryService.associationRefId(operationLogBlob.getId(), logId);
    }

    public Long recoverApiDefinition(ApiDefinitionDTO apiDefinitionDTO) {
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, apiDefinitionDTO);
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);

        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        apiDefinitionBlob.setId(apiDefinition.getId());
        apiDefinitionBlob.setRequest(ApiDataUtils.toJSONString(apiDefinitionDTO.getRequest()).getBytes(StandardCharsets.UTF_8));
        apiDefinitionBlob.setResponse(ApiDataUtils.toJSONString(apiDefinitionDTO.getResponse()).getBytes(StandardCharsets.UTF_8));
        apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);

        // 记录操作日志
        return apiDefinitionLogService.recoverOperationHistoryLog(apiDefinitionDTO, apiDefinition.getCreateUser(), apiDefinition.getProjectId());
    }

    public List<ApiDefinitionBlob> getBlobByIds(List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return Collections.emptyList();
        }
        ApiDefinitionBlobExample apiDefinitionBlobExample = new ApiDefinitionBlobExample();
        apiDefinitionBlobExample.createCriteria().andIdIn(apiIds);
        return apiDefinitionBlobMapper.selectByExampleWithBLOBs(apiDefinitionBlobExample);
    }
}
