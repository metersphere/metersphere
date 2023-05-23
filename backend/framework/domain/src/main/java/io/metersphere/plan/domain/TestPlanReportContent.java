package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "测试计划报告内容")
@Table("test_plan_report_content")
@Data
public class TestPlanReportContent implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_report_content.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_report_content.test_plan_report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report_content.test_plan_report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划报告ID", required = true, allowableValues = "range[1, 50]")
    private String testPlanReportId;


    @ApiModelProperty(name = "总结", allowableValues = "range[1, 2000]")
    private String summary;


    @ApiModelProperty(name = "报告内容", dataType = "byte[]")
    private byte[] content;


}