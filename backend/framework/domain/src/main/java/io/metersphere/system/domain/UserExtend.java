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

@ApiModel(value = "用户扩展")
@TableName("user_extend")
@Data
public class UserExtend implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    @TableId
    @NotBlank(message = "用户ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "用户ID")
    private String userId;
    
    /** 其他平台对接信息 */
    
    
    @ApiModelProperty(name = "其他平台对接信息")
    private byte[] platformInfo;
    
    /** UI本地调试地址 */
    
    
    @ApiModelProperty(name = "UI本地调试地址")
    private String seleniumServer;
    

}