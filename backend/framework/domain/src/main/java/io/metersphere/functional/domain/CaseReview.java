package io.metersphere.functional.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class CaseReview implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{case_review.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 200, message = "{case_review.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "评审状态：未开始/进行中/已完成/已结束/已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "评审结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{case_review.end_time.not_blank}", groups = {Created.class})
    private Long endTime;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "评审规则：单人通过/全部通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{case_review.review_pass_rule.length_range}", groups = {Created.class, Updated.class})
    private String reviewPassRule;

    @Schema(title = "描述")
    private String description;

    private static final long serialVersionUID = 1L;
}