package io.metersphere.project.service;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.builder.LogDTOBuilder;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileMetadataLogService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveUploadLog(FileMetadata module, String operator) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file/upload")
                .sourceId(module.getId())
                .content(Translator.get("file.log.upload") + " " + module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveReUploadLog(FileMetadata module, String operator) {
        Project project = projectMapper.selectByPrimaryKey(module.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(module.getProjectId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file/re-upload")
                .sourceId(module.getId())
                .content(Translator.get("file.log.re-upload") + " " + module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveUpdateLog(FileMetadata module, String projectId, String operator) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(projectId)
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .method(HttpMethodConstants.POST.name())
                .path("/project/file/update")
                .sourceId(module.getId())
                .content(module.getName())
                .originalValue(JSON.toJSONBytes(module))
                .createUser(operator)
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public void saveDeleteLog(List<FileMetadata> deleteList, String projectId, String operator) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<LogDTO> list = new ArrayList<>();
        for (FileMetadata fileMetadata : deleteList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(projectId)
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.DELETE.name())
                    .module(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                    .method(HttpMethodConstants.POST.name())
                    .path("/project/file/delete")
                    .sourceId(fileMetadata.getId())
                    .content(fileMetadata.getName())
                    .originalValue(JSON.toJSONBytes(fileMetadata))
                    .createUser(operator)
                    .build().getLogDTO();
            list.add(dto);
        }

        operationLogService.batchAdd(list);
    }
}
