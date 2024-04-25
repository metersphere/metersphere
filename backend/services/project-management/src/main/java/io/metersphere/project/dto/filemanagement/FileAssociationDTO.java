package io.metersphere.project.dto.filemanagement;

import io.metersphere.sdk.constants.ModuleConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件转存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAssociationDTO {
    @NotBlank
    private String fileName;

    @NotBlank
    private String originalName;

    @NotBlank
    private String sourceId;

    @NotBlank
    private String sourceType;

    @NotBlank
    private String moduleId = ModuleConstants.NODE_TYPE_DEFAULT;

    private byte[] fileBytes;

    private FileLogRecord fileLogRecord;

    public FileAssociationDTO(String fileName, String originalName, byte[] fileBytes, String sourceId, String sourceType, FileLogRecord fileLogRecord) {
        this.fileName = fileName;
        this.originalName = originalName;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.fileBytes = fileBytes;
        this.fileLogRecord = fileLogRecord;
    }
}
