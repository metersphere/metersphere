package io.metersphere.project.dto.filemanagement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileAssociationResponse {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "资源Id")
    private String sourceId;

    @Schema(description = "前端跳转Id")
    private String redirectId;

    @Schema(description = "资源编号")
    private String sourceNum;

    @Schema(description = "文件Id")
    private String fileId;

    @Schema(description = "资源名称")
    private String sourceName;

    @Schema(description = "资源类型")
    private String sourceType;

    @Schema(description = "文件版本")
    private String fileVersion;
}
