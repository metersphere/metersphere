package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "功能用例")
@TableName("functional_case_blob")
@Data
public class FunctionalCaseBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{functional_case_blob.functional_case_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "功能用例ID", required = true, allowableValues = "range[1, 50]")
    private String functionalCaseId;


    @ApiModelProperty(name = "用例步骤（JSON)，step_model 为 0 时启用", required = false)
    private String steps;


    @ApiModelProperty(name = "步骤描述，step_model 为 1 时启用", required = false)
    private String stepDescription;


    @ApiModelProperty(name = "预期结果，step_model 为 1 时启用", required = false)
    private String expectedResult;


    @ApiModelProperty(name = "前置条件", required = false)
    private String prerequisite;


    @ApiModelProperty(name = "备注", required = false)
    private String description;
}
