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

@ApiModel(value = "三方认证源")
@TableName("auth_source")
@Data
public class AuthSource implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 认证源ID */
    @TableId
    @NotBlank(message = "认证源ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "认证源ID")
    private String id;
    
    /** 认证源配置 */
    
    
    @ApiModelProperty(name = "认证源配置")
    private byte[] configuration;
    
    /** 状态 启用 禁用 */
    @Size(min = 1, max = 64, message = "状态 启用 禁用长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "状态 启用 禁用不能为空", groups = {Created.class})
    @ApiModelProperty(name = "状态 启用 禁用")
    private String status;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 名称 */
    
    
    @ApiModelProperty(name = "名称")
    private String name;
    
    /** 类型 */
    
    
    @ApiModelProperty(name = "类型")
    private String type;
    

}