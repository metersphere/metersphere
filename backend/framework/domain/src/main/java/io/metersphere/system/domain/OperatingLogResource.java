package io.metersphere.system.domain;

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

@ApiModel(value = "操作日志关系记录")
@TableName("operating_log_resource")
@Data
public class OperatingLogResource implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId
    @NotBlank(message = "{operating_log_resource.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{operating_log_resource.operating_log_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{operating_log_resource.operating_log_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Operating log ID", required = true, allowableValues="range[1, 50]")
    private String operatingLogId;
    
    @Size(min = 1, max = 50, message = "{operating_log_resource.source_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{operating_log_resource.source_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "operating source id", required = true, allowableValues="range[1, 50]")
    private String sourceId;
    

}