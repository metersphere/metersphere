package io.metersphere.project.domain;

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

@ApiModel(value = "API模版表")
@TableName("api_template")
@Data
public class ApiTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_template.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "模版ID", required = true, allowableValues = "range[1, 100]")
    private String id;

    @Size(min = 1, max = 64, message = "{api_template.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_template.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模版名称", required = true, allowableValues = "range[1, 64]")
    private String name;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 255]")
    private String description;

    @Size(min = 1, max = 1, message = "{api_template.system.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_template.system.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是系统模版", required = true, allowableValues = "range[1, 1]")
    private Boolean system;

    @Size(min = 1, max = 1, message = "{api_template.global.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_template.global.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是全局模版", required = true, allowableValues = "range[1, 1]")
    private Boolean global;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "项目ID", required = false, allowableValues = "range[1, 64]")
    private String projectId;


}