package io.metersphere.plan.dto.request;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanTableRequest extends BasePageRequest {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<String> moduleIds;

    @Schema(description = "项目ID")
    @NotBlank(message = "{test_plan.project_id.not_blank}")
    private String projectId;

    @Schema(description = "类型", allowableValues = {"ALL", "TEST_PLAN", "GROUP"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan.type.not_blank}")
    private String type;


    public String getSortString() {
        if (StringUtils.isEmpty(super.getSortString())) {
            return "t.update_time desc";
        } else {
            return "t." + super.getSortString();
        }

    }
}
