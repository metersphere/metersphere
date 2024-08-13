package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanApiScenarioRequest extends BasePageRequest {

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "计划集id")
    private String collectionId;

    @Schema(description = "场景pk")
    @Size(min = 1, max = 50, message = "{api_scenario_step.scenario_id.length_range}")
    private String scenarioId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "{api_definition.project_id.length_range}")
    private String projectId;

    @Schema(description = "版本fk")
    @Size(min = 1, max = 50, message = "{api_definition.version_id.length_range}")
    private String versionId;

    @Schema(description = "版本引用fk")
    @Size(min = 1, max = 50, message = "{api_definition.ref_id.length_range}")
    private String refId;

    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<@NotBlank String> moduleIds;

    @Schema(description = "删除状态(状态为 1 时为回收站数据)")
    private Boolean deleted = false;

    @Schema(description = "查询时排除的ID")
    private List<String> excludeIds = new ArrayList<>();

    @Schema(description = "是否包含空执行人")
    private boolean nullExecutorKey;

}