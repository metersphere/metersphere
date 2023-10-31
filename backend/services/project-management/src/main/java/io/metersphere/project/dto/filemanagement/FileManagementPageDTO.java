package io.metersphere.project.dto.filemanagement;

import io.metersphere.project.request.filemanagement.FileBatchProcessRequest;
import io.metersphere.project.request.filemanagement.FileMetadataTableRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class FileManagementPageDTO {
    public String projectId;
    public String keyword;
    public List<String> moduleIds;
    public String fileType;
    public String operator;

    public FileManagementPageDTO(FileBatchProcessRequest batchProcessDTO) {
        this.projectId = batchProcessDTO.getProjectId();
        this.keyword = batchProcessDTO.getCondition().getKeyword();
        this.moduleIds = batchProcessDTO.getModuleIds();
        this.fileType = batchProcessDTO.getFileType();
        if (MapUtils.isNotEmpty(batchProcessDTO.getCondition().getCombine())) {
            this.operator = batchProcessDTO.getCondition().getCombine().get("createUser").toString();
        }
    }

    public FileManagementPageDTO(FileMetadataTableRequest batchProcessDTO) {
        this.projectId = batchProcessDTO.getProjectId();
        this.keyword = batchProcessDTO.getKeyword();
        this.moduleIds = batchProcessDTO.getModuleIds();
        this.fileType = batchProcessDTO.getFileType();
        if (MapUtils.isNotEmpty(batchProcessDTO.getCombine()) && batchProcessDTO.getCombine().get("createUser") != null) {
            this.operator = batchProcessDTO.getCombine().get("createUser").toString();
        }
    }
}
