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

@ApiModel(value = "")
@TableName("license")
@Data
public class License implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String id;
    
    /** Create timestamp */
    
    
    @ApiModelProperty(name = "Create timestamp")
    private Long createTime;
    
    /** Update timestamp */
    
    
    @ApiModelProperty(name = "Update timestamp")
    private Long updateTime;
    
    /** license_code */
    
    
    @ApiModelProperty(name = "license_code")
    private String licenseCode;
    

}