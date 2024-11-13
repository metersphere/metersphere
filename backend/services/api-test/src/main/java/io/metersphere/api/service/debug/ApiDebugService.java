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
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.service.MoveNodeService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ApiFileResourceType;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.metersphere.api.controller.result.ApiResultCode.API_DEBUG_EXIST;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDebugService extends MoveNodeService {
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
    private ApiDebugModuleMapper apiDebugModuleMapper;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

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
        checkAddExist(apiDebug, createUser);
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


    public long getNextOrder(String userId) {
        Long pos = extApiDebugMapper.getPos(userId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
    }

    private static ApiFileResourceUpdateRequest getApiFileResourceUpdateRequest(String sourceId, String projectId, String operator) {
        String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(projectId, sourceId);
        ApiFileResourceUpdateRequest resourceUpdateRequest = new ApiFileResourceUpdateRequest();
        resourceUpdateRequest.setProjectId(projectId);
        resourceUpdateRequest.setFolder(apiDebugDir);
        resourceUpdateRequest.setResourceId(sourceId);
        resourceUpdateRequest.setApiResourceType(ApiFileResourceType.API_DEBUG);
        resourceUpdateRequest.setOperator(operator);
        resourceUpdateRequest.setLogModule(OperationLogModule.API_TEST_DEBUG_MANAGEMENT_DEBUG);
        resourceUpdateRequest.setFileAssociationSourceType(FileAssociationSourceUtil.SOURCE_TYPE_API_DEBUG);
        return resourceUpdateRequest;
    }

    public ApiDebug update(ApiDebugUpdateRequest request, String updateUser) {
        checkResourceExist(request.getId());
        ApiDebug apiDebug = BeanUtils.copyBean(new ApiDebug(), request);
        ApiDebug originApiDebug = apiDebugMapper.selectByPrimaryKey(request.getId());
        checkUpdateExist(apiDebug, originApiDebug, updateUser);
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

    private void checkAddExist(ApiDebug apiDebug, String userId) {
        ApiDebugExample example = new ApiDebugExample();
        example.createCriteria()
                .andNameEqualTo(apiDebug.getName())
                .andCreateUserEqualTo(userId)
                .andModuleIdEqualTo(apiDebug.getModuleId());
        if (CollectionUtils.isNotEmpty(apiDebugMapper.selectByExample(example))) {
            throw new MSException(API_DEBUG_EXIST);
        }
    }

    private void checkUpdateExist(ApiDebug apiDebug, ApiDebug originApiDebug, String userId) {
        if (StringUtils.isBlank(apiDebug.getName())) {
            return;
        }
        ApiDebugExample example = new ApiDebugExample();
        example.createCriteria()
                .andIdNotEqualTo(apiDebug.getId())
                .andCreateUserEqualTo(userId)
                .andModuleIdEqualTo(apiDebug.getModuleId() == null ? originApiDebug.getModuleId() : apiDebug.getModuleId())
                .andNameEqualTo(apiDebug.getName());
        if (CollectionUtils.isNotEmpty(apiDebugMapper.selectByExample(example))) {
            throw new MSException(API_DEBUG_EXIST);
        }
    }

    private ApiDebug checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(apiDebugMapper.selectByPrimaryKey(id), "permission.api_debug.name");
    }

    public TaskRequestDTO debug(ApiDebugRunRequest request) {
        ApiResourceRunRequest runRequest = apiExecuteService.getApiResourceRunRequest(request);
        ApiParamConfig apiParamConfig = apiExecuteService.getApiParamConfig();

        TaskRequestDTO taskRequest = apiExecuteService.getTaskRequest(request.getReportId(), request.getId(), request.getProjectId());
        TaskInfo taskInfo = taskRequest.getTaskInfo();
        taskInfo.setTaskId(request.getReportId());
        taskInfo.setSaveResult(false);
        taskInfo.setRealTime(true);
        taskInfo.setResourceType(ApiResourceType.API_DEBUG.name());
        taskInfo.setRunMode(apiExecuteService.getDebugRunModule(request.getFrontendDebug()));
        taskRequest.getTaskItem().setId(request.getReportId());
        apiParamConfig.setTaskItemId(taskRequest.getTaskItem().getId());
        return apiExecuteService.apiExecute(runRequest, taskRequest, apiParamConfig);
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
        ApiDebug apiDebug = checkResourceExist(request.getMoveId());
        if (!StringUtils.equals(request.getModuleId(), apiDebug.getModuleId())) {
            checkModuleExist(request.getModuleId());
            apiDebug.setModuleId(request.getModuleId());
            checkUpdateExist(apiDebug, apiDebug, userId);
            apiDebug.setUpdateUser(userId);
            apiDebug.setUpdateTime(System.currentTimeMillis());
            apiDebugMapper.updateByPrimaryKeySelective(apiDebug);
        }
        if (StringUtils.equals(request.getTargetId(), request.getMoveId())) {
            return;
        }
        request.setProjectId(userId);
        moveNode(request);
    }

    @Override
    public void updatePos(String id, long pos) {
        extApiDebugMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> posIds = extApiDebugMapper.selectIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtApiDebugMapper batchUpdateMapper = sqlSession.getMapper(ExtApiDebugMapper.class);
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
                extApiDebugMapper::selectDragInfoById,
                extApiDebugMapper::selectNodeByPosOperator
        );
        this.sort(sortDTO);
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
}