package io.metersphere.api.service.definition;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.enums.ApiReportStatus;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionFollowerMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApiDefinitionService {

    public static final Long ORDER_STEP = 5000L;

    private static final String MAIN_FOLDER_PROJECT = "project";
    private static final String APP_NAME_API_DEFINITION = "apiDefinition";

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
    private UserLoginService userLoginService;

    @Resource
    private MinioRepository minioRepository;

    @Resource
    private SqlSessionFactory sqlSessionFactory;


    public List<ApiDefinitionDTO> getApiDefinitionPage(ApiDefinitionPageRequest request, Boolean deleted){
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request, deleted);
        if (!CollectionUtils.isEmpty(list)) {
            processApiDefinitions(list, request.getProjectId());
        }
        return list;
    }

    public ApiDefinitionDTO get(String id, String userId){
        ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
        ApiDefinition apiDefinition = checkApiDefinition(id);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(id);
        BeanUtils.copyBean(apiDefinitionDTO,apiDefinition);
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(id).andUserIdEqualTo(userId);
        List<ApiDefinitionFollower> followers = apiDefinitionFollowerMapper.selectByExample(example);
        apiDefinitionDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        if(apiDefinitionBlob != null){
            apiDefinitionDTO.setRequest(ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
        }
        return apiDefinitionDTO;
    }

    public ApiDefinition create(ApiDefinitionAddRequest request, List<MultipartFile> bodyFiles, String userId) {
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
        apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
        // TODO 文件只是上传到minio，不需要入库吗
        uploadBodyFile(bodyFiles, apiDefinition.getId(), request.getProjectId());
        return apiDefinition;
    }

    public ApiDefinition update(ApiDefinitionUpdateRequest request, List<MultipartFile> bodyFiles, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDefinition originApiDefinition = checkApiDefinition(request.getId());
        ApiDefinition apiDefinition = new ApiDefinition();
        BeanUtils.copyBean(apiDefinition, request);
        checkUpdateExist(apiDefinition, originApiDefinition);
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
        apiDefinitionBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
        // TODO 需要处理之前的文件

        uploadBodyFile(bodyFiles, request.getId(), request.getProjectId());
        return apiDefinition;
    }

    public void batchUpdate(ApiDefinitionBatchUpdateRequest request, String userId) {
        ProjectService.checkResourceExist(request.getProjectId());
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol());
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
        // TODO 复制的时候文件需要复制一份

        return apiDefinition;
    }

    public void delete(ApiDefinitionDeleteRequest request, String userId) {
        checkApiDefinition(request.getId());
        handleDeleteApiDefinition(Collections.singletonList(request.getId()),request.getDeleteAll(), userId);
    }

    public void batchDelete(ApiDefinitionBatchRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol());
        if (CollectionUtils.isNotEmpty(ids)) {
            handleDeleteApiDefinition(ids, request.getDeleteAll(), userId);
        }
    }

    public void batchMove(ApiDefinitionBatchMoveRequest request, String userId) {
        List<String> ids = getBatchApiIds(request, request.getProjectId(), request.getProtocol());
        if (!ids.isEmpty()) {
            List<String> refIds = extApiDefinitionMapper.getRefIds(ids);
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

            // Convert tags

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

    public void uploadBodyFile(List<MultipartFile> files, String apiId, String projectId) {
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(file.getName());
                fileRequest.setFolder(minioPath(projectId));
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    minioRepository.saveFile(file, fileRequest);
                } catch (Exception e) {
                    LogUtils.info("上传body文件失败:  文件名称:" + file.getName(), e);
                    throw new MSException(Translator.get("file_upload_fail"));
                }
            });
        }
    }

    /**
     * 校验接口是否存在
     *
     * @param apiId
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
                .andNameEqualTo(apiDefinition.getName())
                .andModuleIdEqualTo(apiDefinition.getModuleId());
        if (CollectionUtils.isNotEmpty(apiDefinitionMapper.selectByExample(example))) {
            throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
        }
    }

    private void checkUpdateExist(ApiDefinition apiDefinition, ApiDefinition originApiDefinition) {
        if (StringUtils.isBlank(apiDefinition.getName())) {
            return;
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria()
                .andIdNotEqualTo(apiDefinition.getId())
                .andModuleIdEqualTo(apiDefinition.getModuleId() == null ? originApiDefinition.getModuleId() : apiDefinition.getModuleId())
                .andNameEqualTo(apiDefinition.getName());
        if (CollectionUtils.isNotEmpty(apiDefinitionMapper.selectByExample(example))) {
            throw new MSException(ApiResultCode.API_DEFINITION_EXIST);
        }
    }

    private ApiDefinitionBlob getApiDefinitionBlob(String apiId) {
        ApiDefinitionBlobExample apiDefinitionBlobExample = new ApiDefinitionBlobExample();
        apiDefinitionBlobExample.createCriteria().andIdEqualTo(apiId);
        return apiDefinitionBlobMapper.selectByPrimaryKey(apiId);
    }

    public <T> List<String> getBatchApiIds(T dto, String projectId, String protocol) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extApiDefinitionMapper.getIds(request, projectId, protocol, false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    /**
     * 根据接口id 获取接口是否存在多个版本
     *
     * @param apiId
     * @return
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
            Map<String, ApiDefinition> collect = extApiDefinitionMapper.getTagsByIds(ids)
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

    private void handleDeleteApiDefinition(List<String> ids, Boolean deleteAll, String userId) {
        if (deleteAll) {
            //全部删除  进入回收站
            List<String> refId = extApiDefinitionMapper.getRefIds(ids);
            if(CollectionUtils.isNotEmpty(refId)){
                extApiDefinitionMapper.batchDelete(refId, userId);
            }
        } else {
            //列表删除 需要判断是否存在多个版本问题
            ids.forEach(id -> {
                ApiDefinition apiDefinition = checkApiDefinition(id);
                // 选中的数据直接放入回收站
                doDelete(id, userId);
                // 删除的数据是否为最新版本的数据，如果是则需要查询是否有多版本数据存在，需要去除当前删除的数据，更新剩余版本数据中最近的一条数据为最新的数据
                if(apiDefinition.getLatest()){
                    List<ApiDefinitionVersionDTO> apiDefinitionVersions = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());
                    if (apiDefinitionVersions.size() > 1) {
                        deleteAfterAction(apiDefinitionVersions);
                    }
                }
            });
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

    private void doDelete(String id, String userId) {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setDeleted(true);
        apiDefinition.setId(id);
        apiDefinition.setDeleteUser(userId);
        apiDefinition.setDeleteTime(System.currentTimeMillis());
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);
    }

    private void deleteFollower(String apiId, String userId) {
        apiDefinitionFollowerMapper.deleteByPrimaryKey(apiId, userId);
    }


    private String minioPath(String projectId) {
        return StringUtils.join(MAIN_FOLDER_PROJECT, "/", projectId, "/", APP_NAME_API_DEFINITION);
    }
}
