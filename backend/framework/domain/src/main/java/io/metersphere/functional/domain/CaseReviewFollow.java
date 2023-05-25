package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "用例评审关注人")
@Table("case_review_follow")
@Data
public class CaseReviewFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{case_review_follow.review_id.not_blank}", groups = {Updated.class})
    @Schema(title = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewId;


    @NotBlank(message = "{case_review_follow.follow_id.not_blank}", groups = {Updated.class})
    @Schema(title = "关注人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String followId;


}