package io.metersphere.load.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "性能报告大字段")
@Table("load_test_report_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class LoadTestReportBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{load_test_report_blob.report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues = "range[1, 50]")
    private String reportId;


    @ApiModelProperty(name = "压力配置", required = false, allowableValues = "range[1, ]")
    private byte[] loadConfiguration;


    @ApiModelProperty(name = "JMX内容", required = false, allowableValues = "range[1, ]")
    private byte[] jmxContent;


    @ApiModelProperty(name = "高级配置", required = false, allowableValues = "range[1, ]")
    private byte[] advancedConfiguration;


    @ApiModelProperty(name = "环境信息 (JSON format)", required = false, allowableValues = "range[1, ]")
    private byte[] envInfo;


}