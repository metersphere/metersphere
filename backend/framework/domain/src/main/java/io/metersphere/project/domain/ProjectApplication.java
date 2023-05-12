package io.metersphere.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "项目应用")
@TableName("project_application")
@Data
public class ProjectApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{project_application.project_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @TableId
    @NotBlank(message = "{project_application.type.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "配置项", required = true, allowableValues = "range[1, 50]")
    private String type;


    @ApiModelProperty(name = "配置值", required = false, allowableValues = "range[1, 255]")
    private String typeValue;


}