package io.metersphere.sdk.domain;

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

@ApiModel(value = "环境组")
@Table("environment_group")
@Data
public class EnvironmentGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{environment_group.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "环境组id", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{environment_group.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{environment_group.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "环境组名", required = true, allowableValues = "range[1, 50]")
    private String name;

    @Size(min = 1, max = 64, message = "{environment_group.workspace_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{environment_group.workspace_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属工作空间", required = true, allowableValues = "range[1, 64]")
    private String workspaceId;


    @ApiModelProperty(name = "环境组描述", required = false, allowableValues = "range[1, 255]")
    private String description;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "创建时间", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = false, allowableValues = "range[1, ]")
    private Long updateTime;


}