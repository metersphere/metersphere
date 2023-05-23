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

@ApiModel(value = "版本管理")
@Table("project_version")
@Data
public class ProjectVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{project_version.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "版本ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{project_version.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project_version.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 100, message = "{project_version.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project_version.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "版本名称", required = true, allowableValues = "range[1, 100]")
    private String name;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 200]")
    private String description;


    @ApiModelProperty(name = "状态", required = false, allowableValues = "range[1, 20]")
    private String status;

    @Size(min = 1, max = 1, message = "{project_version.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project_version.latest.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是最新版", required = true, allowableValues = "range[1, 1]")
    private Boolean latest;


    @ApiModelProperty(name = "发布时间", required = false, allowableValues = "range[1, ]")
    private Long publishTime;


    @ApiModelProperty(name = "开始时间", required = false, allowableValues = "range[1, ]")
    private Long startTime;


    @ApiModelProperty(name = "结束时间", required = false, allowableValues = "range[1, ]")
    private Long endTime;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;

    @Size(min = 1, max = 100, message = "{project_version.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project_version.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


}