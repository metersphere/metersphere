package io.metersphere.ui.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "场景报告大字段")
@TableName("ui_scenario_report_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class UiScenarioReportBlob extends UiScenarioReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_scenario_report_blob.report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues = "range[1, 50]")
    private String reportId;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, ]")
    private byte[] description;


    @ApiModelProperty(name = "执行环境配置", required = false, allowableValues = "range[1, ]")
    private byte[] envConfig;


}