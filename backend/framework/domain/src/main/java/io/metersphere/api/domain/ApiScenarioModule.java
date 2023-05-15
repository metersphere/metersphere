
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

@ApiModel(value = "场景模块")
@TableName("api_scenario_module")
@Data
public class ApiScenarioModule implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_scenario_module.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "场景模块pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 64, message = "{api_scenario_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模块名称", required = true, allowableValues = "range[1, 64]")
    private String name;

    @ApiModelProperty(name = "模块级别", required = true, dataType = "Integer")
    private Integer level;

    @ApiModelProperty(name = "排序", required = true, dataType = "Integer")
    private Integer pos;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_scenario_module.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "更新人", required = true, allowableValues = "range[1, 50]")
    private String updateUser;

    @Size(min = 1, max = 50, message = "{api_scenario_module.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_scenario_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_scenario_module.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_module.parent_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "父级fk", required = true, allowableValues = "range[1, 50]")
    private String parentId;

}