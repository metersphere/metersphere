package io.metersphere.project.service;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.builder.LogDTOBuilder;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileModuleLogService {
    @Resource
    private FileModuleMapper fileModuleMapper;
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

    public void saveUpdateLog(FileModule module, String projectId, String operator) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file-module/update")
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
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
                .path("/project/file-module/delete/%s")
                .sourceId(deleteModule.getId())
                .content(deleteModule.getName() + " " + Translator.get("file.log.delete_module"))
                .originalValue(JSON.toJSONBytes(deleteModule))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveMoveLog(NodeMoveRequest request, String operator) {
        FileModule moveNode;
        FileModule previousNode = null;
        FileModule nextNode = null;
        FileModule parentModule;
        moveNode = fileModuleMapper.selectByPrimaryKey(request.getNodeId());
        if (request.getPreviousNodeId() != null) {
            previousNode = fileModuleMapper.selectByPrimaryKey(request.getPreviousNodeId());
        }
        if (request.getNextNodeId() != null) {
            nextNode = fileModuleMapper.selectByPrimaryKey(request.getNextNodeId());
        }

        parentModule = fileModuleMapper.selectByPrimaryKey(request.getParentId());
        if (parentModule == null) {
            parentModule = new FileModule();
            parentModule.setName(Translator.get("file.module.root"));
        }

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
                .originalValue(JSON.toJSONBytes(moveNode))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }
}
