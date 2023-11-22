package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;

@Data
public class ApiTestCaseAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.name.not_blank}")
    @Size(min = 1, max = 255, message = "{api_debug.name.length_range}")
    private String name;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_debug.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_debug.project_id.length_range}")
    private String projectId;

    @Schema(description = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.priority.not_blank}")
    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}")
    private String priority;

    @Schema(description = "用例状态  Underway Prepare Completed", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}")
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}")
    private String status;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}")
    private String apiDefinitionId;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
                    String> tags;

    @Schema(description = "环境fk")
    private String environmentId;

    @Schema(description = "请求内容")
    @NotBlank
    private String request;

    @Schema(description = "接口用例所需的所有文件资源ID")
    private List<String> fileIds;

}
