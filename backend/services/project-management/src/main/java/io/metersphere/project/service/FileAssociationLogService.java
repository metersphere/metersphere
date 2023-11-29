package io.metersphere.project.service;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.aspect.OperationLogAspect;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileAssociationLogService {


    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;

    public void saveBatchInsertLog(String sourceName, List<FileMetadata> addFileList, FileLogRecord fileLogRecord) {
        List<LogDTO> list = new ArrayList<>();
        Project project = projectMapper.selectByPrimaryKey(fileLogRecord.getProjectId());
        for (FileMetadata fileMetadata : addFileList) {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(project.getId())
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.ADD.name())
                    .module(fileLogRecord.getLogModule())
                    .method(OperationLogAspect.getMethod())
                    .path(OperationLogAspect.getPath())
                    .createUser(fileLogRecord.getOperator())
                    .sourceId(fileMetadata.getId())
                    .content(sourceName + StringUtils.SPACE + Translator.get("file.log.association") + ":" + fileMetadata.getName())
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveBatchUpdateLog(String sourceName, Collection<FileMetadata> values, FileLogRecord fileLogRecord) {
        List<LogDTO> list = new ArrayList<>();
        Project project = projectMapper.selectByPrimaryKey(fileLogRecord.getProjectId());
        for (FileMetadata fileMetadata : values) {
            LogDTO dto = this.genUpdateFileAssociationLogDTO(project, sourceName, fileMetadata, fileLogRecord);
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveUpdateLog(String sourceName, FileMetadata fileMetadata, FileLogRecord fileLogRecord) {
        Project project = projectMapper.selectByPrimaryKey(fileLogRecord.getProjectId());
        LogDTO dto = this.genUpdateFileAssociationLogDTO(project, sourceName, fileMetadata, fileLogRecord);
        operationLogService.add(dto);
    }

    private LogDTO genUpdateFileAssociationLogDTO(Project project, String sourceName, FileMetadata fileMetadata, FileLogRecord fileLogRecord) {
        return LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.UPDATE.name())
                .module(fileLogRecord.getLogModule())
                .method(OperationLogAspect.getMethod())
                .path(OperationLogAspect.getPath())
                .createUser(fileLogRecord.getOperator())
                .sourceId(fileMetadata.getId())
                .content(sourceName + StringUtils.SPACE + Translator.get("file.log.association.update") + ":" + fileMetadata.getName())
                .build().getLogDTO();
    }

    public void saveDeleteLog(Map<String, List<String>> sourceToFileNameMap, FileLogRecord fileLogRecord) {
        Project project = projectMapper.selectByPrimaryKey(fileLogRecord.getProjectId());
        List<LogDTO> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : sourceToFileNameMap.entrySet()) {
            String sourceName = entry.getKey();
            List<String> fileNameList = entry.getValue();
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(project.getId())
                    .organizationId(project.getOrganizationId())
                    .type(OperationLogType.DELETE.name())
                    .module(fileLogRecord.getLogModule())
                    .method(OperationLogAspect.getMethod())
                    .path(OperationLogAspect.getPath())
                    .createUser(fileLogRecord.getOperator())
                    .sourceId(IDGenerator.nextStr())
                    .content(sourceName + StringUtils.SPACE + Translator.get("file.log.association.delete") + ":" + StringUtils.join(fileNameList, ","))
                    .build().getLogDTO();
            list.add(dto);
        }
        operationLogService.batchAdd(list);
    }

    public void saveTransferAssociationLog(String sourceId, String fileName, String sourceName, FileLogRecord fileLogRecord) {
        Project project = projectMapper.selectByPrimaryKey(fileLogRecord.getProjectId());
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(project.getId())
                .organizationId(project.getOrganizationId())
                .type(OperationLogType.ADD.name())
                .module(fileLogRecord.getLogModule())
                .method(OperationLogAspect.getMethod())
                .path(OperationLogAspect.getPath())
                .createUser(fileLogRecord.getOperator())
                .sourceId(sourceId)
                .content(sourceName + StringUtils.SPACE + Translator.get("file.log.transfer.association") + ":" + fileName)
                .build().getLogDTO();

        operationLogService.add(dto);
    }
}
