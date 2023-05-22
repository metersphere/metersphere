package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "功能用例评审和评审人的中间表")
@TableName("case_review_functional_case_user")
@Data
public class CaseReviewFunctionalCaseUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case_user.case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "功能用例和评审中间表的ID", required = true, allowableValues = "range[1, 50]")
    private String caseId;

    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.review_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case_user.review_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "评审ID", required = true, allowableValues = "range[1, 50]")
    private String reviewId;

    @Size(min = 1, max = 50, message = "{case_review_functional_case_user.user_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review_functional_case_user.user_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "评审人ID", required = true, allowableValues = "range[1, 50]")
    private String userId;


}