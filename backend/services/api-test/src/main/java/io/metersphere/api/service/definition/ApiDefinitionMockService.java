package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.debug.ApiFileResourceUpdateRequest;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiDefinitionMockConfigMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMockMapper;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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

    public List<ApiDefinitionMockDTO> getPage(ApiDefinitionMockPageRequest request) {
        return extApiDefinitionMockMapper.list(request);
    }

    public ApiDefinitionMockDTO detail(ApiDefinitionMockRequest request) {
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(request.getId());
        ApiDefinitionMockDTO apiDefinitionMockDTO = new ApiDefinitionMockDTO();
        handleMockConfig(request.getId(), apiDefinitionMockDTO);
        handleApiDefinition(request.getApiDefinitionId(), apiDefinitionMockDTO);
        BeanUtils.copyBean(apiDefinitionMockDTO, apiDefinitionMock);
        return apiDefinitionMockDTO;
    }

    public void handleMockConfig(String id, ApiDefinitionMockDTO apiDefinitionMockDTO) {
        Optional<ApiDefinitionMockConfig> apiDefinitionMockConfigOptional = Optional.ofNullable(apiDefinitionMockConfigMapper.selectByPrimaryKey(id));
        apiDefinitionMockConfigOptional.ifPresent(config -> {
            apiDefinitionMockDTO.setMatching(ApiDataUtils.parseObject(new String(config.getMatching()), AbstractMsTestElement.class));
            apiDefinitionMockDTO.setResponse(ApiDataUtils.parseArray(new String(config.getResponse()), HttpResponse.class));
        });
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

        ApiDefinitionMock apiDefinitionMock = new ApiDefinitionMock();
        BeanUtils.copyBean(apiDefinitionMock, request);
        checkAddExist(apiDefinitionMock);
        apiDefinitionMock.setId(IDGenerator.nextStr());
        apiDefinitionMock.setCreateTime(System.currentTimeMillis());
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMock.setCreateUser(userId);
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinitionMock.setTags(request.getTags());
        }
        apiDefinitionMock.setEnable(true);
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        apiDefinitionMock.setExpectNum(String.valueOf(NumGenerator.nextNum(request.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_MOCK)));

        apiDefinitionMockMapper.insertSelective(apiDefinitionMock);
        ApiDefinitionMockConfig apiDefinitionMockConfig = new ApiDefinitionMockConfig();
        apiDefinitionMockConfig.setId(apiDefinitionMock.getId());
        apiDefinitionMockConfig.setMatching(request.getMatching().getBytes());
        apiDefinitionMockConfig.setResponse(request.getResponse().getBytes());
        apiDefinitionMockConfigMapper.insertSelective(apiDefinitionMockConfig);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceRequest(apiDefinitionMock.getId(), apiDefinitionMock.getProjectId(), userId);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);

        return apiDefinitionMock;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceRequest(String sourceId, String projectId, String operator) {
        String apiDefinitionMockDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDefinitionMockDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_MOCK);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_DEFINITION_MOCK);
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
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            apiDefinitionMock.setTags(request.getTags());
        }
        apiDefinitionMockMapper.updateByPrimaryKeySelective(apiDefinitionMock);
        ApiDefinitionMockConfig apiDefinitionMockConfig = new ApiDefinitionMockConfig();
        apiDefinitionMockConfig.setId(apiDefinitionMock.getId());
        apiDefinitionMockConfig.setMatching(request.getMatching().getBytes());
        apiDefinitionMockConfig.setResponse(request.getResponse().getBytes());
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
        String apiDefinitionMockDir = DefaultRepositoryDir.getApiDefinitionDir(request.getProjectId(), request.getId());
        apiFileResourceService.deleteByResourceId(apiDefinitionMockDir, request.getId(), request.getProjectId(), userId, OperationLogModule.API_DEFINITION_MOCK);
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

        String sourceDir = DefaultRepositoryDir.getApiDefinitionDir(apiDefinitionMock.getProjectId(), request.getId());
        String targetDir = DefaultRepositoryDir.getApiDefinitionDir(apiDefinitionMock.getProjectId(), apiDefinitionMock.getId());
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

    public void updateEnable(String id) {
        ApiDefinitionMock apiDefinitionMock = checkApiDefinitionMock(id);
        ApiDefinitionMock update = new ApiDefinitionMock();
        update.setId(id);
        update.setEnable(!apiDefinitionMock.getEnable());
        update.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMockMapper.updateByPrimaryKeySelective(update);
    }

    public String uploadTempFile(MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
    }

    public void deleteByApiIds(List<String> apiIds, String userId) {
        ApiDefinitionMockExample apiDefinitionMockExample = new ApiDefinitionMockExample();
        apiDefinitionMockExample.createCriteria().andApiDefinitionIdIn(apiIds);

        List<ApiDefinitionMock> apiDefinitionMocks = apiDefinitionMockMapper.selectByExample(apiDefinitionMockExample);

        if (!apiDefinitionMocks.isEmpty()) {
            apiDefinitionMocks.forEach(item -> {
                String apiDefinitionMockDir = DefaultRepositoryDir.getApiDefinitionDir(item.getProjectId(), item.getId());
                apiFileResourceService.deleteByResourceId(apiDefinitionMockDir, item.getId(), item.getProjectId(), userId, OperationLogModule.API_DEFINITION_MOCK);
            });

            List<String> mockIds = apiDefinitionMocks.stream().map(ApiDefinitionMock::getId).toList();

            ApiDefinitionMockConfigExample apiDefinitionMockConfigExample = new ApiDefinitionMockConfigExample();
            apiDefinitionMockConfigExample.createCriteria().andIdIn(mockIds);
            apiDefinitionMockConfigMapper.deleteByExample(apiDefinitionMockConfigExample);

            apiDefinitionMockMapper.deleteByExample(apiDefinitionMockExample);
        }
    }
}
