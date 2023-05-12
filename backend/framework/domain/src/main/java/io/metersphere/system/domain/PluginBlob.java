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

@ApiModel(value = "插件大字段")
@TableName("plugin_blob")
@Data
public class PluginBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String pluginId;
    
    /** plugin form option */
    
    
    @ApiModelProperty(name = "plugin form option")
    private byte[] formOption;
    
    /** plugin form script */
    
    
    @ApiModelProperty(name = "plugin form script")
    private byte[] formScript;
    

}