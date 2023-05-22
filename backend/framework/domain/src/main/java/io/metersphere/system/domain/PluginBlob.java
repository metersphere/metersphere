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
public class PluginBlob extends Plugin implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{plugin_blob.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "plugin form option", required = false, allowableValues = "range[1, ]")
    private byte[] formOption;


    @ApiModelProperty(name = "plugin form script", required = false, allowableValues = "range[1, ]")
    private byte[] formScript;


}