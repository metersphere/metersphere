package io.metersphere.api.service.definition;

import io.metersphere.api.constants.*;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.debug.ApiResourceRunRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.scenario.ApiFileCopyRequest;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.JsonSchemaBuilder;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.project.service.MoveNodeService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApiFileResourceType;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.domain.OperationLogBlob;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.OperationLogBlobMapper;
import io.metersphere.sdk.util.*;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionService extends MoveNodeService {

    private static final String ALL_API = "api_definition_module.api.all";

    private static final String UNPLANNED_API = "api_unplanned_request";
    private static final String API_TABLE = "api_definition";

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private ExtApiScenarioStepMapper extApiScenarioStepMapper;

    @Resource
    private ExtApiDefinitionModuleMapper extApiDefinitionModuleMapper;

    @Resource
    private ApiDefinitionFollowerMapper apiDefinitionFollowerMapper;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private EnvironmentService environmentService;

    @Resource
    private ApiCommonService apiCommonService;

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
    private ExtApiDefinitionCustomFieldMapper extApiDefinitionCustomFieldMapper;

    @Resource
    private ApiDefinitionLogService apiDefinitionLogService;

    @Resource
    private ApiDefinitionMockService apiDefinitionMockService;

    @Resource
    private OperationHistoryService operationHistoryService;

    @Resource
    private OperationLogBlobMapper operationLogBlobMapper;

    @Resource
    private ApiExecuteService apiExecuteService;

    @Resource
    private ApiDefinitionNoticeService apiDefinitionNoticeService;

    public List<ApiDefinitionDTO> getApiDefinitionPage(ApiDefinitionPageRequest request, String userId) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request);
        processApiDefinitions(list);
        return list;
    }

    public void initApiSelectIds(ApiDefinitionPageRequest request) {
        request.setExcludeIds(this.getQueryExcludeIds(request.getFilter(), request.getProjectId(), request.getProtocols()));
        request.setIncludeIds(this.getQueryIncludeIds(request.getFilter(), request.getProjectId(), request.getProtocols()));
    }

    public List<String> getQueryIncludeIds(Map<String, List<String>> queryFilter, String projectId, List<String> protocol) {
        if (queryFilter != null && CollectionUtils.isNotEmpty(queryFilter.get("coverFrom"))) {
            String coverFrom = queryFilter.get("coverFrom").getFirst();
            if (ApiCoverageConstants.API_DEFINITION.equals(coverFrom)) {
                return this.selectApiIdInCaseAndScenarioStep(projectId, protocol);
            } else if (ApiCoverageConstants.API_CASE.equals(coverFrom)) {
                return this.selectApiIdInCase(projectId, protocol);
            } else if (ApiCoverageConstants.API_SCENARIO.equals(coverFrom)) {
                return this.selectApiIdInScenarioStep(projectId, protocol, null);
            }
        }
        return null;
    }

    public List<String> getQueryExcludeIds(Map<String, List<String>> queryFilter, String projectId, List<String> protocol) {
        if (queryFilter != null && CollectionUtils.isNotEmpty(queryFilter.get("unCoverFrom"))) {
            String unCoverFrom = queryFilter.get("unCoverFrom").getFirst();
            if (ApiCoverageConstants.API_DEFINITION.equals(unCoverFrom)) {
                return this.selectApiIdInCaseAndScenarioStep(projectId, protocol);
            } else if (ApiCoverageConstants.API_CASE.equals(unCoverFrom)) {
                return this.selectApiIdInCase(projectId, protocol);
            } else if (ApiCoverageConstants.API_SCENARIO.equals(unCoverFrom)) {
                return this.selectApiIdInScenarioStep(projectId, protocol, null);
            }
        }
        return null;
    }

    private List<String> selectApiIdInCaseAndScenarioStep(String projectId, List<String> protocols) {
        List<String> apiInCase = this.selectApiIdInCase(projectId, protocols);
        List<String> apiInScenarioStep = this.selectApiIdInScenarioStep(projectId, protocols, apiInCase);
        List<String> returnList = ListUtils.union(apiInCase, apiInScenarioStep);
        if (returnList.isEmpty()) {
            returnList.add("nonePrepared");
        }
        return returnList;
    }

    private List<String> selectApiIdInCase(String projectId, List<String> protocols) {
        if (CollectionUtils.isEmpty(protocols)) {
            return new ArrayList<>();
        }
        return extApiTestCaseMapper.selectApiIdByProjectAndProtocol(projectId, protocols);
    }

    private List<String> selectApiIdInScenarioStep(String projectId, List<String> protocols, List<String> ignoreApiIds) {

        List<String> apiInScenarioStep = extApiScenarioStepMapper.selectApiResourceId(projectId, ApiScenarioStepType.API.name(), protocols);
        List<String> apiCaseIdInStep = extApiScenarioStepMapper.selectApiCaseResourceId(projectId, ApiScenarioStepType.API_CASE.name(), protocols);
        // 如果有场景步骤中的 API 用例 ID，追加相关 API ID
        if (CollectionUtils.isNotEmpty(apiCaseIdInStep)) {
            List<String> apiCaseIdInScenarioStep = extApiTestCaseMapper.selectApiIdByCaseId(apiCaseIdInStep, protocols, ignoreApiIds);
            apiInScenarioStep.addAll(apiCaseIdInScenarioStep);
            apiCaseIdInScenarioStep = null;
            apiCaseIdInStep = null;
        }

        if (protocols.contains("HTTP")) {
            List<ApiDefinition> apiDefinitions = extApiDefinitionMapper.selectBaseInfoByProjectId(projectId, List.of("HTTP"), ignoreApiIds);
            List<ApiDefinition> httpApiList = apiDefinitions.stream()
                    .filter(api -> StringUtils.equalsIgnoreCase(api.getProtocol(), "http"))
                    .toList();
            apiDefinitions = null;
            List<String> apiInStepList = new ArrayList<>(CommonBeanFactory.getBean(ApiScenarioService.class).selectApiIdInCustomRequest(projectId, httpApiList));
            apiInScenarioStep.addAll(apiInStepList);
        }
        
        return apiInScenarioStep;
    }

    public List<ApiDefinitionDTO> getDocPage(ApiDefinitionPageRequest request, String userId) {
        if (CollectionUtils.isEmpty(request.getProtocols())) {
            return new ArrayList<>();
        }
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
        ApiDefinition apiDefinition = checkApiDefinitionDeleted(id);
        return getApiDefinitionInfo(id, userId, apiDefinition);
    }

    public ApiDefinition create(ApiDefinitionAddRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, request);
        apiDefinition.setVersionId(StringUtils.defaultIfBlank(request.getVersionId(), extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId())));
        checkAddExist(apiDefinition);
        checkResponseNameCode(request.getResponse());
        apiDefinition.setId(IDGenerator.nextStr());
        apiDefinition.setNum(getNextNum(request.getProjectId()));
        apiDefinition.setPos(getNextOrder(request.getProjectId()));
        apiDefinition.setLatest(true);
        apiDefinition.setStatus(request.getStatus());
        apiDefinition.setCreateUser(userId);
        apiDefinition.setUpdateUser(userId);
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setRefId(apiDefinition.getId());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinition.setTags(ServiceUtils.parseTags(request.getTags()));
        }
        apiDefinitionMapper.insertSelective(apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        apiDefinitionBlob.setId(apiDefinition.getId());
        apiDefinitionBlob.setRequest(getMsTestElementStr(request.getRequest()).getBytes());
        if (request.getResponse() != null) {
            List<HttpResponse> msHttpResponse = request.getResponse();
            msHttpResponse.forEach(item -> item.setId(IDGenerator.nextStr()));
            apiDefinitionBlob.setResponse(JSON.toJSONString(msHttpResponse).getBytes());
        }
        apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(apiDefinition.getId(), apiDefinition.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);

        //保存自定义字段
        List<ApiDefinitionCustomField> customFields = request.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields = customFields.stream().distinct().toList();
            batchInsertCustomFields(apiDefinition.getId(), customFields);
        }

        return apiDefinition;
    }

    private String getMsTestElementStr(Object request) {
        String requestStr = JSON.toJSONString(request);
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(requestStr, AbstractMsTestElement.class);
        // 手动校验参数
        ServiceUtils.validateParam(msTestElement);
        return requestStr;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDefinitionDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiFileResourceType.API);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_TEST_MANAGEMENT_DEFINITION);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_DEFINITION);
        return resourceUpdateRequest;
    }

    public ApiDefinition update(ApiDefinitionUpdateRequest request, String userId) {
        ApiDefinition originApiDefinition = checkApiDefinition(request.getId());
        ApiDefinitionBlob originApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(request.getId());
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, request);
        apiDefinition.setTags(ServiceUtils.parseTags(apiDefinition.getTags()));
        checkResponseNameCode(request.getResponse());
        checkUpdateExist(apiDefinition, originApiDefinition);

        apiDefinition.setUpdateUser(userId);
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinition.setTags(request.getTags());
        }
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);

        if (request.getResponse() != null || request.getRequest() != null) {
            // 更新请求和响应
            ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
            apiDefinitionBlob.setId(apiDefinition.getId());
            Optional.ofNullable(request.getRequest())
                    .ifPresent(req -> apiDefinitionBlob.setRequest(getMsTestElementStr(req).getBytes()));
            Optional.ofNullable(request.getResponse())
                    .ifPresent(responses -> apiDefinitionBlob.setResponse(JSON.toJSONString(responses).getBytes()));
            apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
        }

        // 自定义字段
        handleUpdateCustomFields(request, originApiDefinition.getProjectId());

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(originApiDefinition.getId(), originApiDefinition.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        resourceUpdateRequest.setUnLinkFileIds(request.getUnLinkFileIds());
        resourceUpdateRequest.setDeleteFileIds(request.getDeleteFileIds());
        apiFileResourceService.updateFileResource(resourceUpdateRequest);

        AbstractMsTestElement originRequest = ApiDataUtils.parseObject(new String(originApiDefinitionBlob.getRequest()), AbstractMsTestElement.class);
        // 处理接口定义参数变更
        String requestStr = JSON.toJSONString(request.getRequest());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(requestStr, AbstractMsTestElement.class);
        apiTestCaseService.handleApiParamChange(apiDefinition.getId(), msTestElement, originRequest);
        return apiDefinition;
    }

    public void batchUpdate(ApiDefinitionBatchUpdateRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocols(), false, userId);
        // 记录更新前的数据
        apiDefinitionLogService.batchUpdateLog(ids, userId, request.getProjectId());
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinition apiDefinition = new ApiDefinition();
            BeanUtils.copyBean(apiDefinition, request);
            apiDefinition.setUpdateUser(userId);
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            switch (request.getType()) {
                case "tags" -> handleTags(request, userId, ids);
                case "customs" -> detailCustoms(request, ids);
                case "method" -> handleMethod(request, userId, ids, apiDefinition, apiDefinitionExample);
                default -> {
                    apiDefinitionExample.createCriteria().andIdIn(ids);
                    apiDefinitionMapper.updateByExampleSelective(apiDefinition, apiDefinitionExample);
                }
            }
            //发送通知
            apiDefinitionNoticeService.batchSendNotice(ids, userId, request.getProjectId(), NoticeConstants.Event.UPDATE);
        }
    }

    private void handleMethod(ApiDefinitionBatchUpdateRequest request, String userId, List<String> ids, ApiDefinition apiDefinition, ApiDefinitionExample apiDefinitionExample) {
        apiDefinitionExample.createCriteria().andIdIn(ids).andProtocolEqualTo(ApiConstants.HTTP_PROTOCOL);
        apiDefinitionMapper.updateByExampleSelective(apiDefinition, apiDefinitionExample);
    }

    private void detailCustoms(ApiDefinitionBatchUpdateRequest request, List<String> ids) {
        // 自定义字段处理
        ApiDefinitionCustomFieldDTO customField = request.getCustomField();
        List<ApiDefinitionCustomField> list = new ArrayList<>();
        ApiDefinitionCustomField apiDefinitionCustomField = new ApiDefinitionCustomField();
        apiDefinitionCustomField.setFieldId(customField.getId());
        apiDefinitionCustomField.setValue(customField.getValue());
        list.add(apiDefinitionCustomField);
        ApiDefinitionUpdateRequest apiDefinitionUpdateRequest = new ApiDefinitionUpdateRequest();
        BeanUtils.copyBean(apiDefinitionUpdateRequest, request);
        apiDefinitionUpdateRequest.setCustomFields(list);
        ids.forEach(id -> {
            apiDefinitionUpdateRequest.setId(id);
            handleUpdateCustomFields(apiDefinitionUpdateRequest, request.getProjectId());
        });
    }


    private void handleUpdateCustomFields(ApiDefinitionUpdateRequest request, String projectId) {
        List<ApiDefinitionCustomField> customFields = request.getCustomFields();
        //更新自定义字段
        if (CollectionUtils.isNotEmpty(customFields)) {
            List<ApiDefinitionCustomField> addFields = new ArrayList<>();
            List<ApiDefinitionCustomField> updateFields = new ArrayList<>();
            List<ApiDefinitionCustomFieldDTO> originalFields = extApiDefinitionCustomFieldMapper.getApiCustomFields(List.of(request.getId()), projectId);
            Map<String, ApiDefinitionCustomFieldDTO> originalFieldMap = originalFields.stream().collect(Collectors.toMap(ApiDefinitionCustomFieldDTO::getId, Function.identity()));

            customFields.forEach(customField -> {
                if (!originalFieldMap.containsKey(customField.getFieldId())) {
                    // New custom field relationship
                    updateExistingCustomField(request.getId(), customField, addFields);
                } else {
                    // Existing custom field relationship
                    updateExistingCustomField(request.getId(), customField, updateFields);
                }
            });

            batchInsertCustomFields(request.getId(), addFields);
            batchUpdateCustomFields(request.getId(), updateFields);
        }
    }

    private void updateExistingCustomField(String apiId, ApiDefinitionCustomField customField, List<ApiDefinitionCustomField> updateFields) {
        ApiDefinitionCustomField apiDefinitionCustomField = new ApiDefinitionCustomField();
        apiDefinitionCustomField.setApiId(apiId);
        apiDefinitionCustomField.setFieldId(customField.getFieldId());
        apiDefinitionCustomField.setValue(customField.getValue());
        updateFields.add(apiDefinitionCustomField);
    }

    private void batchInsertCustomFields(String apiId, List<ApiDefinitionCustomField> addFields) {
        if (CollectionUtils.isNotEmpty(addFields)) {
            extApiDefinitionCustomFieldMapper.batchInsertCustomField(apiId, addFields);
        }
    }

    private void batchUpdateCustomFields(String apiId, List<ApiDefinitionCustomField> updateFields) {
        if (CollectionUtils.isNotEmpty(updateFields)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiDefinitionCustomFieldMapper apiCustomFieldMapper = sqlSession.getMapper(ApiDefinitionCustomFieldMapper.class);
            for (ApiDefinitionCustomField apiDefinitionCustomField : updateFields) {
                ApiDefinitionCustomFieldExample apiDefinitionCustomFieldExample = new ApiDefinitionCustomFieldExample();
                apiDefinitionCustomFieldExample.createCriteria().andApiIdEqualTo(apiId).andFieldIdEqualTo(apiDefinitionCustomField.getFieldId());
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

    public void deleteToGc(String id, boolean deleteAllVersion, String userId) {
        ApiDefinition apiDefinition = checkApiDefinition(id);
        handleDeleteApiDefinition(Collections.singletonList(id), deleteAllVersion, apiDefinition.getProjectId(), userId, false);
    }

    public void batchDeleteToGc(ApiDefinitionBatchDeleteRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocols(), false, userId);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleDeleteApiDefinition(ids, request.getDeleteAllVersion(), request.getProjectId(), userId, true);
        }
    }

    public void batchMove(ApiDefinitionBatchMoveRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocols(), false, userId);
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

    public void processApiDefinitions(List<ApiDefinitionDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Set<String> userIds = extractUserIds(list);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));

        List<String> apiDefinitionIds = list.stream().map(ApiDefinitionDTO::getId).toList();
        List<ApiTestCase> apiCaseList = extApiDefinitionMapper.selectNotInTrashCaseIdsByApiIds(apiDefinitionIds);
        Map<String, List<ApiTestCase>> apiCaseMap = apiCaseList.stream().
                collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));

        List<String> moduleIds = list.stream().map(ApiDefinitionDTO::getModuleId).toList();
        List<ApiDefinitionModule> modules = extApiDefinitionModuleMapper.getNameInfoByIds(moduleIds);
        Map<String, String> moduleNameMap = modules.stream()
                .collect(Collectors.toMap(ApiDefinitionModule::getId, ApiDefinitionModule::getName));

        list.forEach(item -> {
            // Convert User IDs to Names
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));

            // Calculate API Case Metrics
            List<ApiTestCase> apiTestCases = apiCaseMap.get(item.getId());
            if (apiTestCases != null) {
                item.setCaseTotal(apiTestCases.size());
            } else {
                item.setCaseTotal(0);
            }

            if (moduleNameMap.get(item.getModuleId()) == null) {
                item.setModuleName(Translator.get("api_unplanned_request"));
            } else {
                item.setModuleName(moduleNameMap.get(item.getModuleId()));
            }
        });
    }

    private Set<String> extractUserIds(List<ApiDefinitionDTO> list) {
        return list.stream()
                .flatMap(apiDefinition -> Stream.of(apiDefinition.getUpdateUser(), apiDefinition.getDeleteUser(), apiDefinition.getCreateUser()))
                .collect(Collectors.toSet());
    }

    public long getNextOrder(String projectId) {
        Long pos = extApiDefinitionMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
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

    public ApiDefinition checkApiDefinitionDeleted(String apiId) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(apiId).andDeletedEqualTo(false);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiDefinitions)) {
            throw new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST);
        }
        return apiDefinitions.getFirst();
    }

    private void checkAddExist(ApiDefinition apiDefinition) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (StringUtils.equals(apiDefinition.getProtocol(), ApiConstants.HTTP_PROTOCOL)) {
            example.createCriteria()
                    .andPathEqualTo(apiDefinition.getPath())
                    .andMethodEqualTo(apiDefinition.getMethod())
                    .andProtocolEqualTo(apiDefinition.getProtocol())
                    .andProjectIdEqualTo(apiDefinition.getProjectId())
                    .andDeletedEqualTo(false);
        } else {
            example.createCriteria()
                    .andNameEqualTo(apiDefinition.getName())
                    .andProtocolEqualTo(apiDefinition.getProtocol())
                    .andModuleIdEqualTo(apiDefinition.getModuleId())
                    .andProjectIdEqualTo(apiDefinition.getProjectId())
                    .andDeletedEqualTo(false);
        }
        if (CollectionUtils.isNotEmpty(apiDefinitionMapper.selectByExample(example))) {
            throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
        }
    }

    private void checkUpdateExist(ApiDefinition apiDefinition, ApiDefinition originApiDefinition) {
        if (StringUtils.equals(originApiDefinition.getProtocol(), ApiConstants.HTTP_PROTOCOL)) {
            if (StringUtils.isNotEmpty(apiDefinition.getPath()) || StringUtils.isNotEmpty(apiDefinition.getMethod())) {
                ApiDefinitionExample example = new ApiDefinitionExample();
                String method = StringUtils.isBlank(apiDefinition.getMethod()) ? originApiDefinition.getMethod() : apiDefinition.getMethod();
                String path = StringUtils.isBlank(apiDefinition.getPath()) ? originApiDefinition.getPath() : apiDefinition.getPath();
                example.createCriteria()
                        .andPathEqualTo(path)
                        .andMethodEqualTo(method)
                        .andIdNotEqualTo(originApiDefinition.getId())
                        .andProtocolEqualTo(originApiDefinition.getProtocol())
                        .andProjectIdEqualTo(originApiDefinition.getProjectId())
                        .andRefIdNotEqualTo(originApiDefinition.getRefId())
                        .andDeletedEqualTo(false);
                if (apiDefinitionMapper.countByExample(example) > 0) {
                    throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
                }
            }
        } else {
            if (StringUtils.isNotBlank(apiDefinition.getName())) {
                ApiDefinitionExample example = new ApiDefinitionExample();
                example.createCriteria()
                        .andNameEqualTo(apiDefinition.getName())
                        .andIdNotEqualTo(originApiDefinition.getId())
                        .andProtocolEqualTo(originApiDefinition.getProtocol())
                        .andModuleIdEqualTo(originApiDefinition.getModuleId())
                        .andProjectIdEqualTo(originApiDefinition.getProjectId())
                        .andRefIdNotEqualTo(originApiDefinition.getRefId())
                        .andDeletedEqualTo(false);
                if (apiDefinitionMapper.countByExample(example) > 0) {
                    throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
                }
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
                        apiDefinition.setTags(ServiceUtils.parseTags(tags.stream().distinct().toList()));
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
            apiDefinition.setTags(request.isClear() ? new ArrayList<>() : ServiceUtils.parseTags(request.getTags()));
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

    public void handleDeleteApiDefinition(List<String> ids, boolean deleteAllVersion, String projectId, String userId, boolean isBatch) {
        if (deleteAllVersion) {
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
                        apiDefinitionNoticeService.batchSendNotice(delApiIds, userId, projectId, NoticeConstants.Event.DELETE);
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

    public void delete(String id, String userId) {
        ApiDefinition apiDefinition = checkApiDefinition(id);
        handleTrashDelApiDefinition(Collections.singletonList(id), userId, apiDefinition.getProjectId(), false);
    }

    public void batchRecover(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocols(), true, userId);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleRecoverApiDefinition(ids, userId, request.getProjectId(), true);
        }
    }

    public void batchDelete(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocols(), true, userId);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleTrashDelApiDefinition(ids, userId, request.getProjectId(), true);
        }
    }

    public void handleTrashDelApiDefinition(List<String> ids, String userId, String projectId, boolean isBatch) {
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 2000, subList -> doTrashDel(subList, userId, projectId, isBatch));
        }
    }

    private void doTrashDel(List<String> ids, String userId, String projectId, boolean isBatch) {
        if (CollectionUtils.isNotEmpty(ids)) {
            // 删除上传的文件
            String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, StringUtils.EMPTY);
            apiFileResourceService.deleteByResourceIds(apiDefinitionDir, ids, projectId, userId, OperationLogModule.API_TEST_MANAGEMENT_DEFINITION);

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
        apiDefinitionMockService.deleteByApiIds(apiIds, userId, projectId);
    }

    // 获取批量操作选中的ID
    public <T> List<String> getBatchApiIds(T dto, String projectId, List<String> protocols, boolean deleted, String userId) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
        if (request.isSelectAll() && CollectionUtils.isNotEmpty(protocols)) {
            List<String> includeIds = this.getQueryIncludeIds(request.getCondition().getFilter(), projectId, protocols);
            List<String> excludeIds = this.getQueryExcludeIds(request.getCondition().getFilter(), projectId, protocols);
            List<String> ids = extApiDefinitionMapper.getIds(request, projectId, protocols, deleted, includeIds, excludeIds);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

    public ApiDefinitionDTO getApiDefinitionInfo(String id, String userId, ApiDefinition apiDefinition) {
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
        // 2. 使用Optional避免空指针异常
        handleBlob(id, apiDefinitionDTO);
        // 3. 查询自定义字段
        handleCustomFields(id, apiDefinition.getProjectId(), apiDefinitionDTO);
        // 3. 使用Stream简化集合操作
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(id).andUserIdEqualTo(userId);
        apiDefinitionDTO.setFollow(apiDefinitionFollowerMapper.countByExample(example) > 0);
        Set<String> userIds = extractUserIds(List.of(apiDefinitionDTO));
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        apiDefinitionDTO.setCreateUserName(userMap.get(apiDefinitionDTO.getCreateUser()));
        apiDefinitionDTO.setUpdateUserName(userMap.get(apiDefinitionDTO.getUpdateUser()));
        ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(apiDefinitionDTO.getModuleId());
        if (apiDefinitionModule != null) {
            apiDefinitionDTO.setModuleName(apiDefinitionModule.getName());
        } else {
            apiDefinitionDTO.setModuleName(Translator.get("api_unplanned_request"));
        }
        return apiDefinitionDTO;
    }

    public void handleBlob(String id, ApiDefinitionDTO apiDefinitionDTO) {
        Optional<ApiDefinitionBlob> apiDefinitionBlobOptional = Optional.ofNullable(apiDefinitionBlobMapper.selectByPrimaryKey(id));
        apiDefinitionBlobOptional.ifPresent(blob -> {
            AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);

            apiCommonService.setLinkFileInfo(id, msTestElement);
            apiCommonService.setEnableCommonScriptProcessorInfo(msTestElement);
            apiCommonService.setApiDefinitionExecuteInfo(msTestElement, apiDefinitionDTO);

            apiDefinitionDTO.setRequest(msTestElement);
            // blob.getResponse() 为 null 时不进行转换
            if (blob.getResponse() != null) {
                List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class);
                for (HttpResponse httpResponse : httpResponses) {
                    apiCommonService.setLinkFileInfo(id, httpResponse.getBody());
                }
                apiDefinitionDTO.setResponse(httpResponses);
            }
        });
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
                ApiDefinitionDTO first = list.getFirst();
                handleBlob(first.getId(), first);
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
            handleBlob(apiDefinition.getId(), apiDefinitionDTO);
            apiDefinitionDocDTO.setDocTitle(apiDefinitionDTO.getName());
            apiDefinitionDocDTO.setDocInfo(apiDefinitionDTO);
        }

        return apiDefinitionDocDTO;
    }

    public List<OperationHistoryDTO> list(OperationHistoryRequest request) {
        return operationHistoryService.listWidthTable(request, API_TABLE);
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

    public void editPos(ApiEditPosRequest request, String userId) {
        ApiDefinition apiDefinition = checkApiDefinition(request.getMoveId());
        if (StringUtils.isNotBlank(request.getModuleId()) && !StringUtils.equals(request.getModuleId(), apiDefinition.getModuleId())) {
            checkModuleExist(request.getModuleId());
            apiDefinition.setModuleId(request.getModuleId());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiDefinition.setUpdateUser(userId);
            apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);
        }
        if (StringUtils.equals(request.getTargetId(), request.getMoveId())) {
            return;
        }
        moveNode(request);
    }

    @Override
    public void updatePos(String id, long pos) {
        extApiDefinitionMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> posIds = extApiDefinitionMapper.selectIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtApiDefinitionMapper batchUpdateMapper = sqlSession.getMapper(ExtApiDefinitionMapper.class);
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
                extApiDefinitionMapper::selectDragInfoById,
                extApiDefinitionMapper::selectNodeByPosOperator
        );
        this.sort(sortDTO);
    }

    private void checkResponseNameCode(Object response) {
        if (response != null && !response.toString().isEmpty() && !response.toString().equals("{}")) {
            List<HttpResponse> httpResponses = ApiDataUtils.parseArray(JSON.toJSONString(response), HttpResponse.class);
            boolean isUnique = httpResponses.stream()
                    .map(httpResponse -> httpResponse.getName() + httpResponse.getStatusCode())
                    .collect(Collectors.toSet())
                    .size() == httpResponses.size();
            if (!isUnique) {
                throw new MSException(ApiResultCode.API_RESPONSE_NAME_CODE_UNIQUE);
            }
        }
    }

    public List<ApiDefinitionExecuteInfo> getModuleInfoByIds(List<String> apiIds) {
        return extApiDefinitionMapper.getApiDefinitionExecuteInfo(apiIds);
    }

    public void checkModuleExist(String moduleId) {
        if (StringUtils.equals(moduleId, "root")) {
            return;
        }
        ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(moduleId);
        if (apiDefinitionModule == null) {
            throw new MSException("module.not.exist");
        }
    }

    public void handleFileAssociationUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(originFileAssociation.getSourceId());
        if (apiDefinitionBlob == null) {
            return;
        }
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class);
        // 获取接口中需要更新的文件
        List<ApiFile> updateFiles = apiCommonService.getApiFilesByFileId(originFileAssociation.getFileId(), msTestElement);
        // 如果有需要更新的文件，则更新 request 字段
        if (CollectionUtils.isNotEmpty(updateFiles)) {
            // 替换文件的Id和name
            apiCommonService.replaceApiFileInfo(updateFiles, newFileMetadata);
            apiDefinitionBlob.setRequest(ApiDataUtils.toJSONString(msTestElement).getBytes());
            apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
        }

        // 处理响应的文件
        if (apiDefinitionBlob.getResponse() != null) {
            List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(apiDefinitionBlob.getResponse()), HttpResponse.class);
            List<ApiFile> responseUpdateFiles = new ArrayList<>(0);
            for (HttpResponse httpResponse : httpResponses) {
                responseUpdateFiles.addAll(apiCommonService.getApiBodyFiles(httpResponse.getBody())
                        .stream()
                        .filter(file -> StringUtils.equals(originFileAssociation.getFileId(), file.getFileId()))
                        .toList());

            }
            if (CollectionUtils.isNotEmpty(responseUpdateFiles)) {
                // 替换文件的Id和name
                apiCommonService.replaceApiFileInfo(responseUpdateFiles, newFileMetadata);
                apiDefinitionBlob.setResponse(JSON.toJSONString(httpResponses).getBytes());
                apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
            }
        }
    }

    public String transfer(ApiTransferRequest request, String userId) {
        return apiFileResourceService.transfer(request, userId, ApiResourceType.API.name());
    }

    public TaskRequestDTO debug(ApiDefinitionRunRequest request) {
        ApiResourceRunRequest runRequest = apiExecuteService.getApiResourceRunRequest(request);
        EnvironmentInfoDTO environmentInfoDTO = environmentService.get(request.getEnvironmentId());
        ApiParamConfig apiParamConfig = apiExecuteService.getApiParamConfig(request.getProjectId());

        TaskRequestDTO taskRequest = apiExecuteService.getTaskRequest(request.getReportId(), request.getId(), request.getProjectId());
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.setTaskId(request.getReportId());
        taskInfo.setSaveResult(false);
        taskInfo.setRealTime(true);
        taskInfo.setResourceType(ApiResourceType.API.name());
        taskInfo.setRunMode(apiExecuteService.getDebugRunModule(request.getFrontendDebug()));
        taskRequest.getTaskItem().setId(request.getReportId());

        AbstractMsTestElement msTestElement = runRequest.getTestElement();

        // 设置 method 等信息
        apiCommonService.setApiDefinitionExecuteInfo(msTestElement, BeanUtils.copyBean(new ApiDefinition(), request));
        // 设置环境
        apiParamConfig.setEnvConfig(environmentInfoDTO);

        return apiExecuteService.apiExecute(runRequest, taskRequest, apiParamConfig);
    }

    public List<ReferenceDTO> getReference(ReferenceRequest request) {
        return extApiDefinitionMapper.getReference(request);
    }

    public String preview(JsonSchemaItem jsonSchemaItem) {
        if (BooleanUtils.isFalse(jsonSchemaItem.getEnable())) {
            return "{}";
        }
        filterDisableItem(jsonSchemaItem);
        return JsonSchemaBuilder.preview(JSON.toJSONString(jsonSchemaItem));
    }

    public String jsonSchemaAutoGenerate(JsonSchemaItem jsonSchemaItem) {
        if (BooleanUtils.isFalse(jsonSchemaItem.getEnable())) {
            return "{}";
        }
        filterDisableItem(jsonSchemaItem);
        return JsonSchemaBuilder.jsonSchemaAutoGenerate(JSON.toJSONString(jsonSchemaItem));
    }

    private void filterDisableItem(JsonSchemaItem jsonSchemaItem) {
        if (isObjectItem(jsonSchemaItem)) {
            Map<String, JsonSchemaItem> properties = jsonSchemaItem.getProperties();
            if (properties != null) {
                Iterator<String> iterator = properties.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    JsonSchemaItem item = properties.get(key);
                    if (item == null) {
                        continue;
                    }
                    if (BooleanUtils.isFalse(item.getEnable()) || StringUtils.isBlank(key)) {
                        iterator.remove();
                    } else if (isObjectItem(jsonSchemaItem) || isArrayItem(jsonSchemaItem)) {
                        filterDisableItem(item);
                    }
                }
            }
        } else if (isArrayItem(jsonSchemaItem)) {
            List<JsonSchemaItem> items = jsonSchemaItem.getItems();
            if (items != null) {
                Iterator<JsonSchemaItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    JsonSchemaItem item = iterator.next();
                    if (item == null) {
                        continue;
                    }
                    if (BooleanUtils.isFalse(item.getEnable())) {
                        iterator.remove();
                    } else if (isObjectItem(jsonSchemaItem) || isArrayItem(jsonSchemaItem)){
                        filterDisableItem(item);
                    }
                }
            }
        }
    }

    private boolean isArrayItem(JsonSchemaItem jsonSchemaItem) {
        return StringUtils.equals(jsonSchemaItem.getType(), PropertyConstant.ARRAY);
    }

    private boolean isObjectItem(JsonSchemaItem jsonSchemaItem) {
        return StringUtils.equals(jsonSchemaItem.getType(), PropertyConstant.OBJECT);
    }

    public Map<String, String> copyFile(ApiFileCopyRequest request) {
        // 从接口定义复制
        ApiDefinition apiDefinition = checkApiDefinition(request.getResourceId());
        String sourceDir = DefaultRepositoryDir.getApiDefinitionDir(apiDefinition.getProjectId(),
                apiDefinition.getId());

        return apiCommonService.copyFiles2TempDir(request.getFileIds(), sourceDir);
    }
}
