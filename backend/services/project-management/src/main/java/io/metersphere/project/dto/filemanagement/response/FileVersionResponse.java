package io.metersphere.project.dto.filemanagement.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FileVersionResponse {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "文件版本")
    private String fileVersion;

    @Schema(description = "更新历史")
    private String updateHistory;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作时间")
    private long operateTime;

}
