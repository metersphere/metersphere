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
public class FunctionalCaseBlob extends FunctionalCase implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(name = "用例步骤（JSON)，step_model 为 0 时启用")
    private String steps;


    @ApiModelProperty(name = "步骤描述，step_model 为 1 时启用")
    private String stepDescription;


    @ApiModelProperty(name = "预期结果，step_model 为 1 时启用")
    private String expectedResult;


    @ApiModelProperty(name = "前置条件")
    private String prerequisite;


    @ApiModelProperty(name = "备注")
    private String description;
}
