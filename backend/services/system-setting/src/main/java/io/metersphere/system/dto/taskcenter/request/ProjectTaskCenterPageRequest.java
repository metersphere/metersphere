package io.metersphere.system.dto.taskcenter.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2024/1/17 11:21
 * @version: 1.0
 */
@Data
public class ProjectTaskCenterPageRequest extends TaskCenterPageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    @Size(min = 1, max = 50, message = "{project.id.length_range}")
    private String projectId;

}
