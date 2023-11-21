package io.metersphere.api.service.debug;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.domain.ApiDebugBlob;
import io.metersphere.api.domain.ApiDebugExample;
import io.metersphere.api.dto.debug.*;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.mapper.ExtApiDebugMapper;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.metersphere.api.controller.result.ApiResultCode.API_DEBUG_EXIST;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDebugService {
    @Resource
    private ApiDebugMapper apiDebugMapper;
    @Resource
    private ApiDebugBlobMapper apiDebugBlobMapper;
    @Resource
    private ExtApiDebugMapper extApiDebugMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;

    public List<ApiDebugSimpleDTO> list(String protocol, String userId) {
        return extApiDebugMapper.list(protocol, userId);
    }

    public ApiDebugDTO get(String id) {
        checkResourceExist(id);
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(id);
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(id);
        ApiDebugDTO apiDebugDTO = new ApiDebugDTO();
        BeanUtils.copyBean(apiDebugDTO, apiDebug);
        apiDebugDTO.setRequest(ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        apiDebugDTO.setResponse(apiDebugDTO.getResponse());
        apiDebugDTO.setFileIds(apiFileResourceService.getFileIdsByResourceId(id));
        return apiDebugDTO;
    }

    public ApiDebug add(ApiDebugAddRequest request, List<MultipartFile> files, String createUser) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDebug apiDebug = new ApiDebug();
        BeanUtils.copyBean(apiDebug, request);
        apiDebug.setCreateUser(createUser);
        checkAddExist(apiDebug);
        apiDebug.setId(IDGenerator.nextStr());
        apiDebug.setCreateTime(System.currentTimeMillis());
        apiDebug.setUpdateTime(System.currentTimeMillis());
        apiDebug.setUpdateUser(apiDebug.getCreateUser());
        apiDebugMapper.insert(apiDebug);
        // todo 校验 moduleId
        ApiDebugBlob apiDebugBlob = new ApiDebugBlob();
        apiDebugBlob.setId(apiDebug.getId());
        apiDebugBlob.setRequest(request.getRequest().getBytes());
        apiDebugBlobMapper.insert(apiDebugBlob);

        // 处理文件
        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(request.getProjectId(), apiDebug.getId());
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(apiDebug.getProjectId());
        resourceUpdateRequest.setFileIds(request.getFileIds());
        resourceUpdateRequest.setAddFileIds(request.getFileIds());
        resourceUpdateRequest.setFolder(apiDebugDir);
        resourceUpdateRequest.setResourceId(apiDebug.getId());
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_DEBUG);
        apiFileResourceService.addFileResource(resourceUpdateRequest, files);
        return apiDebug;
    }


    public ApiDebug update(ApiDebugUpdateRequest request, List<MultipartFile> files, String updateUser) {
        checkResourceExist(request.getId());
        ApiDebug apiDebug = BeanUtils.copyBean(new ApiDebug(), request);
        ApiDebug originApiDebug = apiDebugMapper.selectByPrimaryKey(request.getId());
        checkUpdateExist(apiDebug, originApiDebug);
        apiDebug.setUpdateUser(updateUser);
        apiDebug.setUpdateTime(System.currentTimeMillis());
        apiDebugMapper.updateByPrimaryKeySelective(apiDebug);
        // todo 校验 moduleId

        ApiDebugBlob apiDebugBlob = new ApiDebugBlob();
        apiDebugBlob.setId(request.getId());
        apiDebugBlob.setRequest(request.getRequest().getBytes());
        apiDebugBlobMapper.updateByPrimaryKeySelective(apiDebugBlob);

        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(originApiDebug.getProjectId(), originApiDebug.getId());

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(originApiDebug.getProjectId());
        resourceUpdateRequest.setFileIds(request.getFileIds());
        resourceUpdateRequest.setAddFileIds(request.getAddFileIds());
        resourceUpdateRequest.setFolder(apiDebugDir);
        resourceUpdateRequest.setResourceId(apiDebug.getId());
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_DEBUG);
        apiFileResourceService.updateFileResource(resourceUpdateRequest, files);
        return apiDebug;
    }

    public void delete(String id) {
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(id);
        checkResourceExist(id);
        apiDebugMapper.deleteByPrimaryKey(id);
        apiDebugBlobMapper.deleteByPrimaryKey(id);
        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(apiDebug.getProjectId(), apiDebug.getId());
        apiFileResourceService.deleteByResourceId(apiDebugDir, id);
    }

    private void checkAddExist(ApiDebug apiDebug) {
        ApiDebugExample example = new ApiDebugExample();
        example.createCriteria()
                .andNameEqualTo(apiDebug.getName())
                .andModuleIdEqualTo(apiDebug.getModuleId());
        if (CollectionUtils.isNotEmpty(apiDebugMapper.selectByExample(example))) {
            throw new MSException(API_DEBUG_EXIST);
        }
    }

    private void checkUpdateExist(ApiDebug apiDebug, ApiDebug originApiDebug) {
        if (StringUtils.isBlank(apiDebug.getName())) {
            return;
        }
        ApiDebugExample example = new ApiDebugExample();
        example.createCriteria()
                .andIdNotEqualTo(apiDebug.getId())
                .andModuleIdEqualTo(apiDebug.getModuleId() == null ? originApiDebug.getModuleId() : apiDebug.getModuleId())
                .andNameEqualTo(apiDebug.getName());
        if (CollectionUtils.isNotEmpty(apiDebugMapper.selectByExample(example))) {
            throw new MSException(API_DEBUG_EXIST);
        }
    }
    private ApiDebug checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(apiDebugMapper.selectByPrimaryKey(id), "permission.system_api_debug.name");
    }
}