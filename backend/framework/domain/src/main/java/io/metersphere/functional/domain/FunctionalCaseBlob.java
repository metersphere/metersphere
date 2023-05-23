package io.metersphere.functional.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "功能用例")
@Table("functional_case_blob")
@Data
public class FunctionalCaseBlob extends FunctionalCase implements Serializable {
    private static final long serialVersionUID = 1L;

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
