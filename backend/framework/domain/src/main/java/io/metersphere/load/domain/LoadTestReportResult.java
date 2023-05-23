package io.metersphere.load.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "报告结果")
@Table("load_test_report_result")
@Data
public class LoadTestReportResult implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{load_test_report_result.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "主键无实际意义", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{load_test_report_result.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report_result.report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues = "range[1, 50]")
    private String reportId;


    @ApiModelProperty(name = "报告项目key", required = false, allowableValues = "range[1, 64]")
    private String reportKey;


    @ApiModelProperty(name = "报告项目内容", required = false, allowableValues = "range[1, ]")
    private byte[] reportValue;


}