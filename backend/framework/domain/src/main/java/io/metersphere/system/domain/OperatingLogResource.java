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
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String id;
    
    /** Operating log ID */
    @Size(min = 1, max = 50, message = "Operating log ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Operating log ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Operating log ID")
    private String operatingLogId;
    
    /** operating source id */
    @Size(min = 1, max = 50, message = "operating source id长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "operating source id不能为空", groups = {Created.class})
    @ApiModelProperty(name = "operating source id")
    private String sourceId;
    

}