package io.metersphere.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "功能用例模版扩展")
@TableName("functional_case_template_extend")
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCaseTemplateExtend extends FunctionalCaseTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{functional_case_template_extend.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "模板ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "用例名称模板", required = false, allowableValues = "range[1, 255]")
    private String caseName;


    @ApiModelProperty(name = "前置条件模板", required = false, allowableValues = "range[1, ]")
    private String prerequisite;


    @ApiModelProperty(name = "步骤描述模板", required = false, allowableValues = "range[1, ]")
    private String stepDescription;


    @ApiModelProperty(name = "预期结果模板", required = false, allowableValues = "range[1, ]")
    private String expectedResult;


    @ApiModelProperty(name = "实际结果模板", required = false, allowableValues = "range[1, ]")
    private String actualResult;

    @Size(min = 1, max = 64, message = "{functional_case_template_extend.step_model.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_template_extend.step_model.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "编辑模式模板：步骤模式/文本模式", required = true, allowableValues = "range[1, 64]")
    private String stepModel;


    @ApiModelProperty(name = "用例步骤", required = false, allowableValues = "range[1, ]")
    private String steps;


}