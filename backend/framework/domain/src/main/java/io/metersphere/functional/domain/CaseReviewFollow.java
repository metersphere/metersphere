package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class CaseReviewFollow implements Serializable {
    @Schema(title = "评审ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_follow.review_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_follow.review_id.length_range}", groups = {Created.class, Updated.class})
    private String reviewId;

    @Schema(title = "关注人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_follow.follow_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{case_review_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    private String followId;

    private static final long serialVersionUID = 1L;
}