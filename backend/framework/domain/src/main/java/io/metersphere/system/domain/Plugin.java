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

@ApiModel(value = "插件")
@TableName("plugin")
@Data
public class Plugin implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String id;
    
    /** plugin name */
    
    
    @ApiModelProperty(name = "plugin name")
    private String name;
    
    /** Plugin id */
    @Size(min = 1, max = 300, message = "Plugin id长度必须在1-300之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Plugin id不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Plugin id")
    private String pluginId;
    
    /** Ui script id */
    @Size(min = 1, max = 300, message = "Ui script id长度必须在1-300之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Ui script id不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Ui script id")
    private String scriptId;
    
    /** Plugin clazzName */
    @Size(min = 1, max = 500, message = "Plugin clazzName长度必须在1-500之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Plugin clazzName不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Plugin clazzName")
    private String clazzName;
    
    /** Jmeter base clazzName */
    @Size(min = 1, max = 300, message = "Jmeter base clazzName长度必须在1-300之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Jmeter base clazzName不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Jmeter base clazzName")
    private String jmeterClazz;
    
    /** Plugin jar path */
    @Size(min = 1, max = 300, message = "Plugin jar path长度必须在1-300之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Plugin jar path不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Plugin jar path")
    private String sourcePath;
    
    /** Plugin jar name */
    @Size(min = 1, max = 300, message = "Plugin jar name长度必须在1-300之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Plugin jar name不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Plugin jar name")
    private String sourceName;
    
    /** plugin init entry class */
    
    
    @ApiModelProperty(name = "plugin init entry class")
    private String execEntry;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long createTime;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long updateTime;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String createUser;
    
    /** Is xpack plugin */
    
    
    @ApiModelProperty(name = "Is xpack plugin")
    private Boolean xpack;
    
    /** Plugin usage scenarios */
    @Size(min = 1, max = 50, message = "Plugin usage scenarios长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Plugin usage scenarios不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Plugin usage scenarios")
    private String scenario;
    

}