package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.enums.ApiReportStatus;
import io.metersphere.api.enums.ProtocolType;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.sdk.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionService {

    public static final Long ORDER_STEP = 5000L;

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

    public List<ApiDefinitionDTO> getApiDefinitionPage(ApiDefinitionPageRequest request){
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
            processApiDefinitions(list, request.getProjectId());
        }
        return list;
    }

    public List<ApiDefinitionDTO> getDocPage(ApiDefinitionPageRequest request){
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
            processApiDefinitionsDoc(list);
        }
        return list;
    }

    private void processApiDefinitionsDoc(List<ApiDefinitionDTO> list){
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

    public ApiDefinitionDTO get(String id, String userId){
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
            apiDefinition.setTags(JSON.toJSONString(request.getTags()));
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
        if(request.getProtocol().equals(ProtocolType.HTTP.name())){
            checkUpdateExist(apiDefinition);
        }
        apiDefinition.setStatus(request.getStatus());
        apiDefinition.setUpdateUser(userId);
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setVersionId(request.getVersionId());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinition.setTags(JSON.toJSONString(request.getTags()));
        }
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        apiDefinitionBlob.setId(apiDefinition.getId());
        apiDefinitionBlob.setRequest(request.getRequest().getBytes());
        apiDefinitionBlob.setResponse(request.getResponse().getBytes());
        apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);

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
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        if (CollectionUtils.isNotEmpty(ids)) {
            if (request.getType().equals("tags")) {
                handleTags(request, userId, ids);
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
        if(copyApiDefinitionBlob != null){
            apiDefinitionBlob.setId(apiDefinition.getId());
            apiDefinitionBlob.setRequest(copyApiDefinitionBlob.getRequest());
            apiDefinitionBlob.setResponse(copyApiDefinitionBlob.getResponse());
            apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
        }
        // TODO 复制的时候文件需要复制一份 仅复制接口内容， 不包含用例、mock信息

        return apiDefinition;
    }

    public void delete(ApiDefinitionDeleteRequest request, String userId) {
        checkApiDefinition(request.getId());
        handleDeleteApiDefinition(Collections.singletonList(request.getId()),request.getDeleteAll(), request.getProjectId(), userId);
    }

    public void batchDelete(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleDeleteApiDefinition(ids, request.getDeleteAll(), request.getProjectId(), userId);
        }
    }

    public void batchMove(ApiDefinitionBatchMoveRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), false);
        if (!ids.isEmpty()) {
            List<String> refIds = extApiDefinitionMapper.getRefIds(ids, false);
            if (!refIds.isEmpty()) {
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

        list.forEach(item -> {
            // Convert User IDs to Names
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));

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
    private ApiDefinition checkApiDefinition(String apiId) {
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
                .andProtocolEqualTo(apiDefinition.getProtocol());
        if (CollectionUtils.isNotEmpty(apiDefinitionMapper.selectByExample(example))) {
            throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
        }
    }

    private void checkUpdateExist(ApiDefinition apiDefinition) {
        if (StringUtils.isNotEmpty(apiDefinition.getPath()) && StringUtils.isNotEmpty(apiDefinition.getMethod())) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria()
                    .andIdNotEqualTo(apiDefinition.getId()).andProtocolEqualTo(apiDefinition.getProtocol())
                    .andPathEqualTo(apiDefinition.getPath()).andMethodEqualTo(apiDefinition.getMethod());
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
                    if (StringUtils.isNotBlank(collect.get(id).getTags())) {
                        LinkedHashSet<String> tags = new LinkedHashSet<>((JSON.parseArray(collect.get(id).getTags(), String.class)));
                        tags.addAll(request.getTags());
                        apiDefinition.setTags(JSON.toJSONString(tags));
                    } else {
                        apiDefinition.setTags(JSON.toJSONString(request.getTags()));
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
            apiDefinition.setTags(JSON.toJSONString(request.getTags()));
            apiDefinition.setProjectId(request.getProjectId());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiDefinition.setUpdateUser(userId);
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            apiDefinitionExample.createCriteria().andIdIn(ids).andDeletedEqualTo(false).andProjectIdEqualTo(request.getProjectId());
            apiDefinitionMapper.updateByExampleSelective(apiDefinition, apiDefinitionExample);
        }
    }

    private String getCopyName(String name) {
        String copyName = "copy_" + name ;
        if (copyName.length() > 255) {
            copyName = copyName.substring(0, 250) + copyName.substring(copyName.length() - 5);
        }
        return copyName;
    }

    private void handleDeleteApiDefinition(List<String> ids, boolean deleteAll, String projectId, String userId) {
        if (deleteAll) {
            //全部删除  进入回收站
            List<String> refIds = extApiDefinitionMapper.getRefIds(ids, false);
            if(CollectionUtils.isNotEmpty(refIds)){
                SubListUtils.dealForSubList(refIds, 2000, subRefIds -> {
                extApiDefinitionMapper.batchDeleteByRefId(subRefIds, userId, projectId);
                    List<String> delApiIds = extApiDefinitionMapper.getIdsByRefId(subRefIds, false);
                    SubListUtils.dealForSubList(delApiIds, 2000, subList -> {
                        if(CollectionUtils.isNotEmpty(delApiIds)){
                            // 删除接口相关数据到回收站
                            deleteApiRelatedData(subList, userId, projectId);
                        }
                    });
                });
            }
        } else {
            // 列表删除
            if (!ids.isEmpty()) {
                SubListUtils.dealForSubList(ids, 2000, subList -> doDelete(subList, userId, projectId));
            }
        }
    }

    private void deleteAfterAction(List<ApiDefinitionVersionDTO> apiDefinitionVersions){
        apiDefinitionVersions.forEach(item->{
            clearLatestVersion(item.getRefId(),item.getProjectId());
            ApiDefinition latestData = getLatestData(item.getRefId(), item.getProjectId());
            updateLatestVersion(latestData.getId(),latestData.getProjectId());
        });
    }

    // 清除多版本最新标识
    private void clearLatestVersion(String refId, String projectId){
        extApiDefinitionMapper.clearLatestVersion(refId, projectId);
    }

    // 获取多版本最新一条数据
    private ApiDefinition getLatestData(String refId, String projectId){
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andRefIdEqualTo(refId).andDeletedEqualTo(false).andProjectIdEqualTo(projectId);
        apiDefinitionExample.setOrderByClause("update_time DESC");
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByExample(apiDefinitionExample).stream().findFirst().orElse(null);
        if (apiDefinition == null) {
            throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
        }
        return apiDefinition;
    }

    // 更新最新版本标识
    private void updateLatestVersion(String id, String projectId){
        extApiDefinitionMapper.updateLatestVersion(id, projectId);
    }

    private void doDelete(List<String> ids, String userId, String projectId) {
        if(CollectionUtils.isNotEmpty(ids)){
            extApiDefinitionMapper.batchDeleteById(ids, userId, projectId);
            // 需要判断是否存在多个版本问题
            ids.forEach(id -> {
                ApiDefinition apiDefinition = checkApiDefinition(id);
                // 删除的数据是否为最新版本的数据，如果是则需要查询是否有多版本数据存在，需要去除当前删除的数据，更新剩余版本数据中最近的一条数据为最新的数据
                if(apiDefinition.getLatest()){
                    List<ApiDefinitionVersionDTO> apiDefinitionVersions = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());
                    if (apiDefinitionVersions.size() > 1) {
                        deleteAfterAction(apiDefinitionVersions);
                    }
                }
            });
            // 删除 case
            deleteApiRelatedData(ids, userId, projectId);
        }

    }

    private void deleteApiRelatedData(List<String> apiIds, String userId, String projectId){
        // 是否存在 case 删除 case
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, false);
        if(CollectionUtils.isNotEmpty(caseLists)){
            List<String> caseIds = caseLists.stream().map(ApiTestCase::getId).distinct().toList();
            apiTestCaseService.batchDeleteToGc(caseIds, userId, projectId, true);
        }
    }

    private void deleteFollower(String apiId, String userId) {
        apiDefinitionFollowerMapper.deleteByPrimaryKey(apiId, userId);
    }

    public void restore(ApiDefinitionDeleteRequest request, String userId) {
        // 恢复接口到接口列表
        handleRestoreApiDefinition(Collections.singletonList(request.getId()), userId, request.getProjectId());
    }
    private void handleRestoreApiDefinition(List<String> ids, String userId, String projectId){
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 2000, subList -> doRestore(subList, userId, projectId));
        }
    }

    private void doRestore(List<String> apiIds, String userId, String projectId) {
        if (CollectionUtils.isNotEmpty(apiIds)) {
            extApiDefinitionMapper.batchRestoreById(apiIds, userId, projectId);

            apiIds.forEach(id -> {
                // 恢复数据恢复最新标识
                ApiDefinition apiDefinition = checkApiDefinition(id);
                // 判断是否存在多个版本
                List<ApiDefinitionVersionDTO> apiDefinitionVersions = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());

                if (CollectionUtils.isNotEmpty(apiDefinitionVersions) && apiDefinitionVersions.size() > 1) {
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
            });
            // 恢复接口关联数据
            recoverApiRelatedData(apiIds, userId, projectId);
        }
    }

    private void recoverApiRelatedData(List<String> apiIds, String userId, String projectId){
        // 是否存在 case 恢复 case
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, true);
        if(CollectionUtils.isNotEmpty(caseLists)) {
            apiTestCaseService.batchRecover(caseLists, userId, projectId);
        }
    }
    public void trashDel(ApiDefinitionDeleteRequest request, String userId) {
        handleTrashDelApiDefinition(Collections.singletonList(request.getId()), userId, request.getProjectId());
    }

    public void batchRestore(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), true);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleRestoreApiDefinition(ids, userId, request.getProjectId());
        }
    }

    public void batchTrashDel(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol(), true);
        if (CollectionUtils.isNotEmpty(ids)) {
            handleTrashDelApiDefinition(ids, userId, request.getProjectId());
        }
    }

    private void handleTrashDelApiDefinition(List<String> ids, String userId, String projectId){
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 2000, subList -> doTrashDel(subList, userId, projectId));
        }
    }

    private void doTrashDel(List<String> ids, String userId, String projectId){
        if(CollectionUtils.isNotEmpty(ids)){
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
            recycleDelApiRelatedData(ids, userId, projectId);

            // 删除接口
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            apiDefinitionExample.createCriteria().andIdIn(ids).andDeletedEqualTo(true).andProjectIdEqualTo(projectId);
            apiDefinitionMapper.deleteByExample(apiDefinitionExample);
        }
    }

    private void recycleDelApiRelatedData(List<String> apiIds, String userId, String projectId){
        // 是否存在 case 删除 case
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(apiIds, true);
        if(CollectionUtils.isNotEmpty(caseLists)) {
            List<String> caseIds = caseLists.stream().map(ApiTestCase::getId).distinct().toList();
            // case 批量删除回收站
            apiTestCaseService.deleteResourceByIds(caseIds, projectId, userId);

        }
    }

    // 获取批量操作选中的ID
    public <T> List<String> getBatchApiIds(T dto, String projectId, String protocol, boolean deleted) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
        if (request.isSelectAll()) {
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

    public ApiDefinitionDTO getInfo(String id, String userId){
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        if(apiDefinition != null){
            apiDefinitionDTO = getApiDefinitionInfo(id, userId, apiDefinition);
        }
        return apiDefinitionDTO;
    }

    public ApiDefinitionDTO getApiDefinitionInfo(String id, String userId, ApiDefinition apiDefinition) {
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        // 2. 使用Optional避免空指针异常
        Optional<ApiDefinitionBlob> apiDefinitionBlobOptional = Optional.ofNullable(apiDefinitionBlobMapper.selectByPrimaryKey(id));
        apiDefinitionBlobOptional.ifPresent(blob -> {
            apiDefinitionDTO.setRequest(ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class));
            // blob.getResponse() 为 null 时不进行转换
            if (blob.getResponse() != null) {
                apiDefinitionDTO.setResponse(ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class));
            }
        });
        // 3. 使用Stream简化集合操作
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(id).andUserIdEqualTo(userId);
        apiDefinitionDTO.setFollow(apiDefinitionFollowerMapper.countByExample(example) > 0);
        BeanUtils.copyBean(apiDefinitionDTO, apiDefinition);
        return apiDefinitionDTO;
    }

    public List<String> getProtocolTypes() {
        return Arrays.stream(ProtocolType.values()).map(Enum::name).toList();
    }

}
