package io.metersphere.project.service;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.dto.filemanagement.FileRepositoryLog;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileModuleLogService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveAddLog(FileModule module, String operator) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file-module/add")
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveAddRepositoryLog(FileRepositoryLog repositoryLog, String operator) {
        Project project = projectMapper.selectByPrimaryKey(repositoryLog.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(repositoryLog.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file/repository/add-repository")
                .sourceId(repositoryLog.getId())
                .content(repositoryLog.getName())
                .originalValue(JSON.toJSONBytes(repositoryLog))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateLog(FileModule oldModule, FileModule newModule, String projectId, String operator) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file-module/update")
                .sourceId(newModule.getId())
                .content(newModule.getName())
                .originalValue(JSON.toJSONBytes(oldModule))
                .modifiedValue(JSON.toJSONBytes(newModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateRepositoryLog(FileRepositoryLog oldRepositoryLog, FileRepositoryLog newRepositoryLog, String operator) {
        Project project = projectMapper.selectByPrimaryKey(newRepositoryLog.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(newRepositoryLog.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file/repository/update-repository")
                .sourceId(newRepositoryLog.getId())
                .content(newRepositoryLog.getName())
                .originalValue(JSON.toJSONBytes(oldRepositoryLog))
                .modifiedValue(JSON.toJSONBytes(newRepositoryLog))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(FileModule deleteModule, String operator) {
        Project project = projectMapper.selectByPrimaryKey(deleteModule.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(deleteModule.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.DELETE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.GET.name())
                .path("/project/file-module/delete")
                .sourceId(deleteModule.getId())
                .content(deleteModule.getName() + " " + Translator.get("log.delete_module"))
                .originalValue(JSON.toJSONBytes(deleteModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveMoveLog(@Validated NodeSortDTO request, String operator) {
        BaseModule moveNode = request.getNode();
        BaseModule previousNode = request.getPreviousNode();
        BaseModule nextNode = request.getNextNode();
        BaseModule parentModule = request.getParent();

        Project project = projectMapper.selectByPrimaryKey(moveNode.getProjectId());
        String logContent;
        if (nextNode == null && previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName();
        } else if (nextNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " + previousNode.getName() + Translator.get("file.log.next");
        } else if (previousNode == null) {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " + nextNode.getName() + Translator.get("file.log.previous");
        } else {
            logContent = moveNode.getName() + " " + Translator.get("file.log.move_to") + parentModule.getName() + " " +
                    previousNode.getName() + Translator.get("file.log.next") + " " + nextNode.getName() + Translator.get("file.log.previous");
        }
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(moveNode.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file-module/move")
                .sourceId(moveNode.getId())
                .content(logContent)
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

}
