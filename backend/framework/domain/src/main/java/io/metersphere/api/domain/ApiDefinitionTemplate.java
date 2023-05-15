
package io.metersphere.api.domain;

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
@TableName("api_definition_template")
@Data
public class ApiDefinitionTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_definition_template.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "模版主键", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 100, message = "{api_definition_template.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_template.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "API模版名称", required = true, allowableValues = "range[1, 100]")
    private String name;

    @Size(min = 1, max = 1, message = "{api_definition_template.system.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_template.system.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是系统模版", required = true, allowableValues = "range[1, 1]")
    private Boolean system;

    @Size(min = 1, max = 1, message = "{api_definition_template.global.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_template.global.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是公共模版", required = true, allowableValues = "range[1, 1]")
    private Boolean global;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_definition_template.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_template.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_definition_template.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_template.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "API模版描述", required = false, allowableValues = "range[1, 500]")
    private String description;

}