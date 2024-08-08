package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class ExportTaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description =  "文件id")
    private String fileId;

    @Schema(description =  "任务id")
    private String taskId;
}
