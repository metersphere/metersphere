package io.metersphere.load.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;

@ApiModel(value = "报告日志jmeter.log")
@TableName("load_test_report_log")
@Data
public class LoadTestReportLog implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{load_test_report_log.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "主键无实际意义", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{load_test_report_log.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report_log.report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues="range[1, 50]")
    private String reportId;
    
    @Size(min = 1, max = 50, message = "{load_test_report_log.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report_log.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源节点ID", required = true, allowableValues="range[1, 50]")
    private String resourceId;
    
    
    @ApiModelProperty(name = "jmeter.log 内容", required = false, allowableValues="range[1, ]")
    private byte[] content;
    
    
    @ApiModelProperty(name = "日志内容分段", required = false, dataType = "Long")
    private Long part;
    

}