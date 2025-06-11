package io.metersphere.api.dto.definition;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
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

    @Schema(description = "用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}")
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}")
    @EnumValue(enumClass = ApiDefinitionStatus.class)
    private String status;

    @Schema(description = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.api_definition_id.not_blank}")
    @Size(min = 1, max = 50, message = "{api_test_case.api_definition_id.length_range}")
    private String apiDefinitionId;

    @Schema(description = "标签")
    private LinkedHashSet<
            @NotBlank
            @Size(min = 1, max = 64, message = "{api_test_case.tag.length_range}")
                    String> tags;

    @Schema(description = "环境fk")
    @Size(max = 50, message = "{api_test_case.environment_id.length_range}")
    private String environmentId;

    @Schema(description = "请求内容")
    @NotNull
    private Object request;

    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;
    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;

    @Schema(description = "是否是ai生成的用例")
    private Boolean aiCreate = false;

    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(tags);
        }
    }

}
