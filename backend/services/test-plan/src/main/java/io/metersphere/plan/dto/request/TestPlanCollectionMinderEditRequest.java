package io.metersphere.plan.dto.request;

import io.metersphere.plan.dto.TestPlanCollectionMinderEditDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanCollectionMinderEditRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String planId;

    @Schema(description = "新增/修改的节点集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TestPlanCollectionMinderEditDTO> editList;

}
