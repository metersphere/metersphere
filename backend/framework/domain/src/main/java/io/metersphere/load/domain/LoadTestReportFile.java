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

@ApiModel(value = "测试报告文件关联表")
@TableName("load_test_report_file")
@Data
public class LoadTestReportFile implements Serializable {
    private static final long serialVersionUID = 1L;
    @Size(min = 1, max = 50, message = "{load_test_report_file.report_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report_file.report_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "报告ID", required = true, allowableValues="range[1, 50]")
    private String reportId;
    
    @Size(min = 1, max = 50, message = "{load_test_report_file.file_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_report_file.file_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "文件ID", required = true, allowableValues="range[1, 50]")
    private String fileId;
    
    
    @ApiModelProperty(name = "文件排序", required = true, dataType = "Integer")
    private Integer sort;
    

}