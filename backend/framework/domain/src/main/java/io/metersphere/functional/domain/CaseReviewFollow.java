package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用例评审关注人")
@TableName("case_review_follow")
@Data
public class CaseReviewFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{case_review_follow.review_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "评审ID", required = true, allowableValues = "range[1, 50]")
    private String reviewId;


    @NotBlank(message = "{case_review_follow.follow_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "关注人", required = true, allowableValues = "range[1, 50]")
    private String followId;


}