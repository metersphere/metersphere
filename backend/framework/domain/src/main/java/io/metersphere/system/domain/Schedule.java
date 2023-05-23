package io.metersphere.system.domain;

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

@ApiModel(value = "定时任务")
@Table("schedule")
@Data
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{schedule.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "qrtz UUID", required = false, allowableValues = "range[1, 50]")
    private String key;

    @Size(min = 1, max = 50, message = "{schedule.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{schedule.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源类型", required = true, allowableValues = "range[1, 50]")
    private String type;

    @Size(min = 1, max = 255, message = "{schedule.value.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{schedule.value.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Schedule value", required = true, allowableValues = "range[1, 255]")
    private String value;

    @Size(min = 1, max = 64, message = "{schedule.job.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{schedule.job.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "Schedule Job Class Name", required = true, allowableValues = "range[1, 64]")
    private String job;


    @ApiModelProperty(name = "Schedule Eable", required = false, allowableValues = "range[1, 1]")
    private Boolean enable;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String resourceId;

    @Size(min = 1, max = 50, message = "{schedule.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{schedule.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "Create timestamp", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "Update timestamp", required = false, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "项目ID", required = false, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "名称", required = false, allowableValues = "range[1, 100]")
    private String name;


    @ApiModelProperty(name = "配置", required = false, allowableValues = "range[1, 1000]")
    private String config;


}