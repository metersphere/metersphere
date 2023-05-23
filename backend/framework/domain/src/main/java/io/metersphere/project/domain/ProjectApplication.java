package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "项目应用")
@Table("project_application")
@Data
public class ProjectApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{project_application.project_id.not_blank}", groups = {Created.class, Updated.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @NotBlank(message = "{project_application.type.not_blank}", groups = {Created.class, Updated.class})
    @ApiModelProperty(name = "配置项", required = true, allowableValues = "range[1, 50]")
    private String type;


    @ApiModelProperty(name = "配置值", required = false, allowableValues = "range[1, 255]")
    private String typeValue;
}