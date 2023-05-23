package io.metersphere.project.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "项目")
@Table("project")
@Data
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{project.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{project.workspace_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project.workspace_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "工作空间ID", required = true, allowableValues = "range[1, 50]")
    private String workspaceId;

    @Size(min = 1, max = 64, message = "{project.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目名称", required = true, allowableValues = "range[1, 64]")
    private String name;


    @ApiModelProperty(name = "项目描述", required = false, allowableValues = "range[1, 255]")
    private String description;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String systemId;


}