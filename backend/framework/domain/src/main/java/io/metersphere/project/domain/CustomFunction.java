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

@ApiModel(value = "自定义函数-代码片段")
@Table("custom_function")
@Data
public class CustomFunction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{custom_function.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 255, message = "{custom_function.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_function.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "函数名", required = true, allowableValues = "range[1, 255]")
    private String name;


    @ApiModelProperty(name = "标签", required = false, allowableValues = "range[1, 1000]")
    private String tags;


    @ApiModelProperty(name = "函数描述", required = false, allowableValues = "range[1, 1000]")
    private String description;

    @Size(min = 1, max = 255, message = "{custom_function.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_function.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "脚本语言类型", required = true, allowableValues = "range[1, 255]")
    private String type;

    @Size(min = 1, max = 100, message = "{custom_function.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_function.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{custom_function.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{custom_function.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;


}