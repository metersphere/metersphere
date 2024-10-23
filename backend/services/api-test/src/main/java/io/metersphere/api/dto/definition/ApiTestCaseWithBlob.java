package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiTestCaseBlob;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiTestCaseWithBlob extends ApiTestCaseBlob {

    @Schema(description = "接口用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{api_test_case.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.priority.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.priority.length_range}", groups = {Created.class, Updated.class})
    private String priority;


    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{api_test_case.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description = "api的协议")
    private String protocol;
    @Schema(description = "api的路径")
    private String path;
    @Schema(description = "api的方法")
    private String method;
    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "最新执行结果状态")
    private String lastReportStatus;

    @Schema(description = "接口定义ID")
    private String apiDefinitionId;

    @Schema(description = "接口定义名称")
    private String apiDefinitionName;

    @Schema(description = "接口用例编号id")
    private Long num;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_test_case.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_test_case.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

}
