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

@ApiModel(value = "服务集成")
@TableName("service_integration")
@Data
public class ServiceIntegration implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 平台 */
    @Size(min = 1, max = 50, message = "平台长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "平台不能为空", groups = {Created.class})
    @ApiModelProperty(name = "平台")
    private String platform;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private byte[] configuration;
    
    /** 工作空间ID */
    
    
    @ApiModelProperty(name = "工作空间ID")
    private String workspaceId;
    

}