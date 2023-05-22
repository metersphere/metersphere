package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "评审和评审人中间表")
@TableName("case_review_user")
@Data
public class CaseReviewUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{case_review_user.review_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "评审ID", required = true, allowableValues="range[1, 50]")
    private String reviewId;
    
    @NotBlank(message = "{case_review_user.user_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "评审人ID", required = true, allowableValues="range[1, 50]")
    private String userId;
    

}