package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "功能用例和关注人的中间表")
@TableName("functional_case_follow")
@Data
public class FunctionalCaseFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{functional_case_follow.case_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "功能用例ID", required = true, allowableValues = "range[1, 50]")
    private String caseId;

    @NotBlank(message = "{functional_case_follow.follow_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "关注人ID", required = true, allowableValues = "range[1, 50]")
    private String followId;


}