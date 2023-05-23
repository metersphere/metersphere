package io.metersphere.functional.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用例评审关注人")
@Table("case_review_follow")
@Data
public class CaseReviewFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{case_review_follow.review_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "评审ID", required = true, allowableValues = "range[1, 50]")
    private String reviewId;


    @NotBlank(message = "{case_review_follow.follow_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "关注人", required = true, allowableValues = "range[1, 50]")
    private String followId;


}