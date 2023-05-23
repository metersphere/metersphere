package io.metersphere.load.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "部分报告结果")
@Table("load_test_report_result_part")
@Data
public class LoadTestReportResultPart implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{load_test_report_result_part.report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues = "range[1, 50]")
    private String reportId;

    @Id
    @NotBlank(message = "{load_test_report_result_part.report_key.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "报告项目key", required = true, allowableValues = "range[1, 64]")
    private String reportKey;

    @Id
    @NotBlank(message = "{load_test_report_result_part.resource_index.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "资源节点索引号", required = true, dataType = "Integer")
    private Integer resourceIndex;


    @ApiModelProperty(name = "报告项目内容", required = false, allowableValues = "range[1, ]")
    private byte[] reportValue;


}