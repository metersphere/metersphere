
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "API/CASE执行结果详情")
@TableName("api_report_blob")
@Data
public class ApiReportBlob extends ApiReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_report_blob.api_report_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口报告fk", required = true, allowableValues = "range[1, 50]")
    private String apiReportId;

    @ApiModelProperty(name = "结果内容详情", required = false, dataType = "byte[]")
    private byte[] content;

    @ApiModelProperty(name = "执行环境配置", required = false, dataType = "byte[]")
    private byte[] config;

    @ApiModelProperty(name = "执行过程日志", required = false, dataType = "byte[]")
    private byte[] console;

}