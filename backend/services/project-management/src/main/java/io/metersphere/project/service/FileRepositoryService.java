package io.metersphere.project.service;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataRepository;
import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleRepository;
import io.metersphere.project.dto.filemanagement.FileRepositoryLog;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryCreateRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryUpdateRequest;
import io.metersphere.project.dto.filemanagement.request.RepositoryFileAddRequest;
import io.metersphere.project.dto.filemanagement.response.FileRepositoryResponse;
import io.metersphere.project.mapper.ExtFileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataRepositoryMapper;
import io.metersphere.project.mapper.FileModuleRepositoryMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.RemoteFileAttachInfo;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.GitRepositoryUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileRepositoryService extends FileModuleService {

    @Resource
    private FileMetadataLogService fileMetadataLogService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;
    @Resource
    private FileModuleRepositoryMapper fileModuleRepositoryMapper;
    @Resource
    private FileMetadataRepositoryMapper fileMetadataRepositoryMapper;

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = extFileModuleMapper.selectBaseByProjectId(projectId, ModuleConstants.NODE_TYPE_GIT);
        return super.buildTreeAndCountResource(fileModuleList, false, Translator.get("default.module"));
    }

    public String addRepository(FileRepositoryCreateRequest request, String operator) {
        this.connect(request.getUrl(), request.getToken(), request.getUserName());
        //记录模块节点数据
        FileModule fileModule = new FileModule();
        fileModule.setId(IDGenerator.nextStr());
        fileModule.setName(request.getName().trim());
        fileModule.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
        fileModule.setProjectId(request.getProjectId());
        fileModule.setModuleType(ModuleConstants.NODE_TYPE_GIT);
        super.checkDataValidity(fileModule);
        fileModule.setCreateTime(System.currentTimeMillis());
        fileModule.setUpdateTime(fileModule.getCreateTime());
        fileModule.setPos(this.countPos(ModuleConstants.ROOT_NODE_PARENT_ID, ModuleConstants.NODE_TYPE_GIT));
        fileModule.setCreateUser(operator);
        fileModule.setUpdateUser(operator);
        fileModuleMapper.insert(fileModule);

        //记录模块仓库数据
        FileModuleRepository fileRepository = new FileModuleRepository();
        fileRepository.setFileModuleId(fileModule.getId());
        fileRepository.setUrl(request.getUrl());
        fileRepository.setPlatform(request.getPlatform());
        fileRepository.setToken(request.getToken());
        fileRepository.setUserName(request.getUserName());
        fileModuleRepositoryMapper.insert(fileRepository);

        //记录日志
        fileModuleLogService.saveAddRepositoryLog(new FileRepositoryLog(fileModule, fileRepository), operator);
        return fileModule.getId();
    }

    public void connect(String url, String token, String userName) {
        GitRepositoryUtil utils = new GitRepositoryUtil(url, userName, token);
        List<String> branches = utils.getBranches();
        if (CollectionUtils.isEmpty(branches)) {
            throw new MSException(Translator.get("file_repository.connect.error"));
        }
    }

    public void updateRepository(FileRepositoryUpdateRequest request, String operator) {
        if (ObjectUtils.allNull(request.getName(), request.getPlatform(), request.getToken(), request.getUserName())) {
            return;
        }
        FileModule fileModule = fileModuleMapper.selectByPrimaryKey(request.getId());
        FileModuleRepository repository = fileModuleRepositoryMapper.selectByPrimaryKey(request.getId());
        if (ObjectUtils.anyNull(fileModule, repository)) {
            throw new MSException(Translator.get("file_repository.connect.error"));
        }

        FileRepositoryLog oldLog = new FileRepositoryLog(fileModule, repository);

        this.connect(repository.getUrl(),
                request.getToken() == null ? repository.getToken() : request.getToken(),
                request.getUserName() == null ? repository.getUserName() : request.getUserName());
        if (StringUtils.isNotBlank(request.getName())) {
            fileModule.setName(request.getName().trim());
            super.checkDataValidity(fileModule);
        }
        fileModule.setUpdateTime(System.currentTimeMillis());
        fileModule.setUpdateUser(operator);
        fileModuleMapper.updateByPrimaryKeySelective(fileModule);

        if (request.getToken() != null || request.getUserName() != null) {
            if (request.getToken() != null) {
                repository.setToken(request.getToken());
            }
            if (request.getUserName() != null) {
                repository.setUserName(request.getUserName());
            }
            fileModuleRepositoryMapper.updateByPrimaryKeySelective(repository);
        }

        //记录日志
        fileModuleLogService.saveUpdateRepositoryLog(oldLog, new FileRepositoryLog(fileModule, repository), operator);
    }

    public String addFile(RepositoryFileAddRequest request, String operator) {
        FileModule fileModule = fileModuleMapper.selectByPrimaryKey(request.getModuleId());
        FileModuleRepository repository = fileModuleRepositoryMapper.selectByPrimaryKey(request.getModuleId());
        if (ObjectUtils.anyNull(fileModule, repository)) {
            throw new MSException(Translator.get("file_repository.connect.error"));
        }
        GitRepositoryUtil utils = new GitRepositoryUtil(repository.getUrl(), repository.getUserName(), repository.getToken());

        RemoteFileAttachInfo fileAttachInfo = utils.selectLastCommitIdByBranch(request.getBranch(), request.getFilePath());
        if (fileAttachInfo == null || fileAttachInfo.fileIsNotExist()) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        FileMetadata fileMetadata = fileMetadataService.genFileMetadata(null, request.getFilePath(), StorageType.GIT.name(), fileAttachInfo.getSize(), request.isEnable(),
                fileModule.getProjectId(), fileModule.getId(), operator);

        //判断文件是否存在: 同一存储库下文件路径和分支不能重复
        if (extFileMetadataMapper.countRepositoryFileByFileNameAndBranch(
                fileModule.getProjectId(), fileModule.getId(), request.getFilePath(), request.getBranch()) > 0) {
            throw new MSException(Translator.get("file.name.exist") + ":" + fileMetadata.getName());
        }

        fileMetadata.setFileVersion(fileAttachInfo.getCommitId());
        fileMetadataMapper.insert(fileMetadata);

        FileMetadataRepository fileMetadataRepository = new FileMetadataRepository();
        fileMetadataRepository.setFileMetadataId(fileMetadata.getId());
        fileMetadataRepository.setBranch(fileAttachInfo.getBranch());
        fileMetadataRepository.setCommitId(fileAttachInfo.getCommitId());
        fileMetadataRepository.setCommitMessage(fileAttachInfo.getCommitMessage());
        fileMetadataRepositoryMapper.insert(fileMetadataRepository);
        //记录日志
        fileMetadataLogService.saveRepositoryAddLog(fileMetadata, fileMetadataRepository, operator);

        return fileMetadata.getId();
    }

    public FileRepositoryResponse getRepositoryInfo(String id) {
        FileModule fileModule = fileModuleMapper.selectByPrimaryKey(id);
        FileModuleRepository repository = fileModuleRepositoryMapper.selectByPrimaryKey(id);
        if (fileModule == null || repository == null) {
            throw new MSException(Translator.get("file_repository.not.exist"));
        }
        return new FileRepositoryResponse(fileModule, repository);
    }
}
