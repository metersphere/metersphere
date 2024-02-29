package io.metersphere.api.service.debug;

import io.metersphere.api.constants.ApiResourceType;
import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.domain.ApiDebugBlob;
import io.metersphere.api.domain.ApiDebugExample;
import io.metersphere.api.domain.ApiDebugModule;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.debug.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.mapper.ApiDebugModuleMapper;
import io.metersphere.api.mapper.ExtApiDebugMapper;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.service.ApiPluginService;
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
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiPluginService apiPluginService;
    @Resource
    private ApiDebugModuleMapper apiDebugModuleMapper;
    @Resource
    private ApiCommonService apiCommonService;

    public static final Long ORDER_STEP = 5000L;

    public List<ApiDebugSimpleDTO> list(String protocol, String userId) {
        return extApiDebugMapper.list(protocol, userId);
    }

    public ApiDebugDTO get(String id) {
        checkResourceExist(id);
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(id);
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(id);
        ApiDebugDTO apiDebugDTO = new ApiDebugDTO();
        BeanUtils.copyBean(apiDebugDTO, apiDebug);
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class);
        apiCommonService.setLinkFileInfo(id, msTestElement);
        apiCommonService.setEnableCommonScriptProcessorInfo(msTestElement);
        apiDebugDTO.setRequest(msTestElement);
        apiDebugDTO.setResponse(apiDebugDTO.getResponse());
        return apiDebugDTO;
    }

    public ApiDebug add(ApiDebugAddRequest request, String createUser) {
        ProjectService.checkResourceExist(request.getProjectId());
        ApiDebug apiDebug = new ApiDebug();
        BeanUtils.copyBean(apiDebug, request);
        apiDebug.setCreateUser(createUser);
        checkAddExist(apiDebug);
        apiDebug.setId(IDGenerator.nextStr());
        apiDebug.setCreateTime(System.currentTimeMillis());
        apiDebug.setUpdateTime(System.currentTimeMillis());
        apiDebug.setUpdateUser(apiDebug.getCreateUser());
        apiDebug.setPos(getNextOrder(createUser));

        apiDebugMapper.insert(apiDebug);
        ApiDebugBlob apiDebugBlob = new ApiDebugBlob();
        apiDebugBlob.setId(apiDebug.getId());
        apiDebugBlob.setRequest(getMsTestElementStr(request.getRequest()).getBytes());
        apiDebugBlobMapper.insert(apiDebugBlob);

        // 处理文件
        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(apiDebug.getId(), apiDebug.getProjectId(), createUser);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        apiFileResourceService.addFileResource(resourceUpdateRequest);
        return apiDebug;
    }


    private Long getNextOrder(String userId) {
        Long pos = extApiDebugMapper.getPos(userId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDebugDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiResourceType.API_DEBUG);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_DEBUG);
        return resourceUpdateRequest;
    }

    public ApiDebug update(ApiDebugUpdateRequest request, String updateUser) {
        checkResourceExist(request.getId());
        ApiDebug apiDebug = BeanUtils.copyBean(new ApiDebug(), request);
        ApiDebug originApiDebug = apiDebugMapper.selectByPrimaryKey(request.getId());
        checkUpdateExist(apiDebug, originApiDebug);
        apiDebug.setUpdateUser(updateUser);
        apiDebug.setUpdateTime(System.currentTimeMillis());
        apiDebugMapper.updateByPrimaryKeySelective(apiDebug);

        if (request.getRequest() != null) {
            ApiDebugBlob apiDebugBlob = new ApiDebugBlob();
            apiDebugBlob.setId(request.getId());
            apiDebugBlob.setRequest(getMsTestElementStr(request.getRequest()).getBytes());
            apiDebugBlobMapper.updateByPrimaryKeySelective(apiDebugBlob);
        }

        ApiFileResourceUpdateRequest resourceUpdateRequest = getApiFileResourceUpdateRequest(originApiDebug.getId(), originApiDebug.getProjectId(), updateUser);
        resourceUpdateRequest.setUploadFileIds(request.getUploadFileIds());
        resourceUpdateRequest.setLinkFileIds(request.getLinkFileIds());
        resourceUpdateRequest.setUnLinkFileIds(request.getUnLinkFileIds());
        resourceUpdateRequest.setDeleteFileIds(request.getDeleteFileIds());
        apiFileResourceService.updateFileResource(resourceUpdateRequest);
        return apiDebug;
    }

    private String getMsTestElementStr(Object request) {
        String requestStr = JSON.toJSONString(request);
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(requestStr, AbstractMsTestElement.class);
        // 手动校验参数
        ServiceUtils.validateParam(msTestElement);
        return requestStr;
    }

    public void delete(String id, String operator) {
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(id);
        checkResourceExist(id);
        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(apiDebug.getProjectId(), apiDebug.getId());
        apiFileResourceService.deleteByResourceId(apiDebugDir, id, apiDebug.getProjectId(), operator, OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG);
        apiDebugMapper.deleteByPrimaryKey(id);
        apiDebugBlobMapper.deleteByPrimaryKey(id);
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
        return ServiceUtils.checkResourceExist(apiDebugMapper.selectByPrimaryKey(id), "permission.api_debug.name");
    }

    public String uploadTempFile(MultipartFile file) {
        return apiFileResourceService.uploadTempFile(file);
    }

    public TaskRequestDTO debug(ApiDebugRunRequest request) {
        String id = request.getId();
        String reportId = request.getReportId();

        ApiResourceRunRequest runRequest = BeanUtils.copyBean(new ApiResourceRunRequest(), request);
        runRequest.setProjectId(request.getProjectId());
        runRequest.setTestId(id);
        runRequest.setReportId(reportId);
        runRequest.setResourceType(ApiResourceType.API_DEBUG.name());
        runRequest.setTestElement(ApiDataUtils.parseObject(JSON.toJSONString(request.getRequest()), AbstractMsTestElement.class));

        ApiParamConfig paramConfig = new ApiParamConfig();
        paramConfig.setTestElementClassPluginIdMap(apiPluginService.getTestElementPluginMap());
        paramConfig.setTestElementClassProtocalMap(apiPluginService.getTestElementProtocolMap());
        paramConfig.setReportId(reportId);

        // 设置使用脚本前后置的公共脚本信息
        apiCommonService.setEnableCommonScriptProcessorInfo(runRequest.getTestElement());
        return apiExecuteService.debug(runRequest, paramConfig);
    }

    public void checkModuleExist(String moduleId) {
        if (StringUtils.equals(moduleId, "root")) {
            return;
        }
        ApiDebugModule apiDebugModule = apiDebugModuleMapper.selectByPrimaryKey(moduleId);
        if (apiDebugModule == null) {
            throw new MSException("module.not.exist");
        }
    }

    public void editPos(ApiEditPosRequest request, String userId) {
        ApiDebug apiDebug = checkResourceExist(request.getTargetId());
        if (!StringUtils.equals(request.getModuleId(), apiDebug.getModuleId())) {
            checkModuleExist(request.getModuleId());
            apiDebug.setModuleId(request.getModuleId());
            checkUpdateExist(apiDebug, apiDebug);
            apiDebug.setUpdateUser(userId);
            apiDebug.setUpdateTime(System.currentTimeMillis());
            apiDebugMapper.updateByPrimaryKeySelective(apiDebug);
        }
        if (StringUtils.equals(request.getTargetId(), request.getMoveId())) {
            return;
        }
        request.setProjectId(userId);
        ServiceUtils.updatePosField(request,
                ApiDebug.class,
                apiDebugMapper::selectByPrimaryKey,
                extApiDebugMapper::getPrePos,
                extApiDebugMapper::getLastPos,
                apiDebugMapper::updateByPrimaryKeySelective);
    }

    /**
     * 处理关联的文件被更新
     *
     * @param originFileAssociation
     * @param newFileMetadata
     */
    public void handleFileAssociationUpgrade(FileAssociation originFileAssociation, FileMetadata newFileMetadata) {
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(originFileAssociation.getSourceId());
        if (apiDebugBlob == null) {
            return;
        }
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class);
        // 获取接口中需要更新的文件
        List<ApiFile> updateFiles = apiCommonService.getApiFilesByFileId(originFileAssociation.getFileId(), msTestElement);
        // 替换文件的Id和name
        apiCommonService.replaceApiFileInfo(updateFiles, newFileMetadata);

        // 如果有需要更新的文件，则更新 request 字段
        if (CollectionUtils.isNotEmpty(updateFiles)) {
            apiDebugBlob.setRequest(ApiDataUtils.toJSONString(msTestElement).getBytes());
            apiDebugBlob.setResponse(null);
            apiDebugBlobMapper.updateByPrimaryKeySelective(apiDebugBlob);
        }
    }

    public String transfer(ApiTransferRequest request, String userId) {
        return apiFileResourceService.transfer(request, userId, ApiResourceType.API_DEBUG.name());
    }
}