package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanApiCaseRequest extends BasePageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "计划集id")
    private String collectionId;

    @Schema(description = "接口pk")
    @Size(max = 50, message = "{api_definition.id.length_range}")
    private String apiDefinitionId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> protocols = new ArrayList<>();

    @Schema(description = "模块ID")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "版本fk")
    @Size(max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "版本来源")
    @Size(max = 50, message = "{api_definition.ref_id.length_range}")
    private String refId;

    @Schema(description = "是否包含空执行人")
    private boolean nullExecutorKey;

}