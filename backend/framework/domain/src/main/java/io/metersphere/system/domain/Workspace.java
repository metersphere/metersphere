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

@ApiModel(value = "工作空间")
@TableName("workspace")
@Data
public class Workspace implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{workspace.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "工作空间ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 100, message = "{workspace.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{workspace.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "工作空间名称", required = true, allowableValues = "range[1, 100]")
    private String name;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 255]")
    private String description;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{workspace.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{workspace.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


}