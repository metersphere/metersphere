package io.metersphere.project.dto;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.sdk.util.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@NoArgsConstructor
public class FileInformationDTO {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新时间")
    private long updateTime;

    @Schema(description = "预览路径")
    private String previewSrc;

    @Schema(description = "文件大小")
    private long size;

    public FileInformationDTO(FileMetadata fileMetadata) {
        this.id = fileMetadata.getId();
        this.name = fileMetadata.getName();
        this.fileType = fileMetadata.getType();
        this.size = fileMetadata.getSize();
        if (StringUtils.isNotBlank(fileMetadata.getTags())) {
            tags = JSON.parseArray(fileMetadata.getTags(), String.class);
        }
        this.description = fileMetadata.getDescription();
        this.updateUser = fileMetadata.getUpdateUser();
        this.updateTime = fileMetadata.getUpdateTime();
    }
}
