package io.metersphere.project.dto.customfunction.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2024/1/9 19:43
 * @version: 1.0
 */
@Data
public class CustomFunctionPageRequest extends BasePageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID")
    @NotBlank(message = "{custom_function.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{custom_function.project_id.length_range}")
    private String projectId;

    @Schema(description = "脚本语言类型")
    private String type;

    @Schema(description = "脚本状态（草稿/测试通过）")
    private String status;
}
