package io.metersphere.sdk.domain;

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

@ApiModel(value = "分享")
@TableName("share_info")
@Data
public class ShareInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 分享ID */
    @TableId
    @NotBlank(message = "分享ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "分享ID")
    private String id;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 分享类型single batch */
    
    
    @ApiModelProperty(name = "分享类型single batch")
    private String shareType;
    
    /** 分享扩展数据 */
    
    
    @ApiModelProperty(name = "分享扩展数据")
    private byte[] customData;
    
    /** 语言 */
    
    
    @ApiModelProperty(name = "语言")
    private String lang;
    

}