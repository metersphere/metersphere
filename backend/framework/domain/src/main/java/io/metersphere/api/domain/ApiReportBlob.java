
package io.metersphere.api.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "API/CASE执行结果详情")
@Table("api_report_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class ApiReportBlob extends ApiReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_report_blob.api_report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口报告fk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @ApiModelProperty(name = "结果内容详情", required = false, dataType = "byte[]")
    private byte[] content;

    @ApiModelProperty(name = "执行环境配置", required = false, dataType = "byte[]")
    private byte[] config;

    @ApiModelProperty(name = "执行过程日志", required = false, dataType = "byte[]")
    private byte[] console;

}