package io.metersphere.project.service;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleRepository;
import io.metersphere.project.dto.filemanagement.FileRepositoryLog;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryCreateRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryUpdateRequest;
import io.metersphere.project.mapper.FileModuleRepositoryMapper;
import io.metersphere.project.utils.GitRepositoryUtil;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
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
    private FileModuleRepositoryMapper fileModuleRepositoryMapper;

    public List<BaseTreeNode> getTree(String projectId) {
        List<BaseTreeNode> fileModuleList = extFileModuleMapper.selectBaseByProjectId(projectId, ModuleConstants.NODE_TYPE_GIT);
        return super.buildTreeAndCountResource(fileModuleList, false, Translator.get("default.module"));
    }

    public String addRepository(FileRepositoryCreateRequest request, String operator) {
        this.connect(request.getUrl(), request.getToken(), request.getUserName());
        this.checkPlatForm(request.getPlatform());
        //记录模块节点数据
        FileModule fileModule = new FileModule();
        fileModule.setId(IDGenerator.nextStr());
        fileModule.setName(request.getName().trim());
        fileModule.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
        fileModule.setProjectId(request.getProjectId());
        super.checkDataValidity(fileModule);
        fileModule.setCreateTime(System.currentTimeMillis());
        fileModule.setUpdateTime(fileModule.getCreateTime());
        fileModule.setPos(this.countPos(ModuleConstants.ROOT_NODE_PARENT_ID, ModuleConstants.NODE_TYPE_GIT));
        fileModule.setCreateUser(operator);
        fileModule.setUpdateUser(operator);
        fileModule.setModuleType(ModuleConstants.NODE_TYPE_DEFAULT);
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
            throw new MSException(Translator.get("file_repository.not.exist"));
        }
    }

    public void updateRepository(FileRepositoryUpdateRequest request, String operator) {
        if (ObjectUtils.allNull(request.getName(), request.getPlatform(), request.getToken(), request.getUserName())) {
            return;
        }
        FileModule fileModule = fileModuleMapper.selectByPrimaryKey(request.getId());
        FileModuleRepository repository = fileModuleRepositoryMapper.selectByPrimaryKey(request.getId());
        if (ObjectUtils.anyNull(fileModule, repository)) {
            throw new MSException(Translator.get("file_repository.not.exist"));
        }
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
            if (request.getPlatform() != null) {
                this.checkPlatForm(request.getPlatform());
                repository.setPlatform(request.getPlatform());
            }
            fileModuleRepositoryMapper.updateByPrimaryKeySelective(repository);
        }
        //记录日志
        fileModuleLogService.saveUpdateRepositoryLog(new FileRepositoryLog(fileModule, repository), operator);
    }

    private void checkPlatForm(String platform) {
        if (!StringUtils.equalsAny(platform, ModuleConstants.NODE_TYPE_GITHUB, ModuleConstants.NODE_TYPE_GITEE, ModuleConstants.NODE_TYPE_GITLAB)) {
            throw new MSException(Translator.get("file_repository.platform.error"));
        }
    }
}
