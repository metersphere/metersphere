package io.metersphere.functional.request;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class CaseReviewRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例评审id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "评审名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.name.not_blank}")
    private String name;

    @Schema(description = "用例评审的模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.module_id.not_blank}")
    private String moduleId;

    @Schema(description = "通过标准：单人通过(SINGLE)/多人通过(MULTIPLE)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}")
    private String reviewPassRule;

    @Schema(description = "评审开始时间")
    private Long startTime;

    @Schema(description = "评审结束时间")
    private Long endTime;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "默认评审人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{case_review.reviewers.not_empty}")
    private List<String> reviewers;

    @Schema(description = "查询功能用例的条件")
    private BaseAssociateCaseRequest baseAssociateCaseRequest;
}
