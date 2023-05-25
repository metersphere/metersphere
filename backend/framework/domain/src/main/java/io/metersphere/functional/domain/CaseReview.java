package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "用例评审")
@Table("case_review")
@Data
public class CaseReview implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{case_review.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 200, message = "{case_review.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.name.not_blank}", groups = {Created.class})
    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(min = 1, max = 64, message = "{case_review.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.status.not_blank}", groups = {Created.class})
    @Schema(title = "评审状态：未开始/进行中/已完成/已结束/已归档", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;


    @Schema(title = "评审结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long endTime;


    @Schema(title = "描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Size(min = 1, max = 50, message = "{case_review.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;


    @Schema(title = "标签", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tags;

    @Size(min = 1, max = 50, message = "{case_review.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Size(min = 1, max = 64, message = "{case_review.review_pass_rule.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}", groups = {Created.class})
    @Schema(title = "评审规则：单人通过/全部通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewPassRule;


}