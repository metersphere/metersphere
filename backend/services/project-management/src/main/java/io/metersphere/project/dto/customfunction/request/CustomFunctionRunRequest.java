package io.metersphere.project.dto.customfunction.request;

import io.metersphere.project.api.KeyValueParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CustomFunctionRunRequest  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "脚本语言类型")
    @NotBlank
    @Size(max = 50)
    private String type;

    @Schema(description = "报告ID")
    @NotBlank
    @Size(max = 50)
    private String reportId;

    @Schema(description =  "参数列表")
    private List<KeyValueParam> params;

    @Schema(description =  "函数体")
    @NotBlank
    private String script;

    @Schema(description =  "项目ID")
    @NotBlank
    @Size(max = 50)
    private String projectId;
}
