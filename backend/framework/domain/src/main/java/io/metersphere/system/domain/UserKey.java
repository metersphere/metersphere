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

@ApiModel(value = "用户api key")
@TableName("user_key")
@Data
public class UserKey implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** user_key ID */
    @TableId
    @NotBlank(message = "user_key ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "user_key ID")
    private String id;
    
    /** 用户ID */
    @Size(min = 1, max = 50, message = "用户ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "用户ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "用户ID")
    private String createUser;
    
    /** access_key */
    @Size(min = 1, max = 50, message = "access_key长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "access_key不能为空", groups = {Created.class})
    @ApiModelProperty(name = "access_key")
    private String accessKey;
    
    /** secret key */
    @Size(min = 1, max = 50, message = "secret key长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "secret key不能为空", groups = {Created.class})
    @ApiModelProperty(name = "secret key")
    private String secretKey;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 状态 */
    
    
    @ApiModelProperty(name = "状态")
    private String status;
    

}