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

    @TableId
    @NotBlank(message = "{plugin.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "plugin name", required = false, allowableValues = "range[1, 300]")
    private String name;

    @Size(min = 1, max = 300, message = "{plugin.plugin_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.plugin_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Plugin id", required = true, allowableValues = "range[1, 300]")
    private String pluginId;

    @Size(min = 1, max = 300, message = "{plugin.script_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.script_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Ui script id", required = true, allowableValues = "range[1, 300]")
    private String scriptId;

    @Size(min = 1, max = 500, message = "{plugin.clazz_name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.clazz_name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Plugin clazzName", required = true, allowableValues = "range[1, 500]")
    private String clazzName;

    @Size(min = 1, max = 300, message = "{plugin.jmeter_clazz.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.jmeter_clazz.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Jmeter base clazzName", required = true, allowableValues = "range[1, 300]")
    private String jmeterClazz;

    @Size(min = 1, max = 300, message = "{plugin.source_path.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.source_path.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Plugin jar path", required = true, allowableValues = "range[1, 300]")
    private String sourcePath;

    @Size(min = 1, max = 300, message = "{plugin.source_name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.source_name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Plugin jar name", required = true, allowableValues = "range[1, 300]")
    private String sourceName;


    @ApiModelProperty(name = "plugin init entry class", required = false, allowableValues = "range[1, 300]")
    private String execEntry;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "Is xpack plugin", required = false, allowableValues = "range[1, 1]")
    private Boolean xpack;

    @Size(min = 1, max = 50, message = "{plugin.scenario.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{plugin.scenario.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Plugin usage scenarios", required = true, allowableValues = "range[1, 50]")
    private String scenario;


}