package io.metersphere.plan.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "测试计划api用例执行结果详情")
@TableName("test_plan_api_case_report_blob")
@Data
public class TestPlanApiCaseReportBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_api_case_report_blob.test_plan_api_report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试计划接口报告fk", required = true, allowableValues="range[1, 50]")
    private String testPlanApiReportId;

    @ApiModelProperty(name = "结果内容详情", dataType = "byte[]")
    private byte[] content;

    @ApiModelProperty(name = "执行环境配置", dataType = "byte[]")
    private byte[] config;

    @ApiModelProperty(name = "执行过程日志", dataType = "byte[]")
    private byte[] console;
}
