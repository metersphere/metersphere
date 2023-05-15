
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

@ApiModel(value = "接口定义环境")
@TableName("api_definition_env")
@Data
public class ApiDefinitionEnv implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_definition_env.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "修改时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_definition_env.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_env.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户fk", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_definition_env.environment_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_env.environment_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "环境fk", required = true, allowableValues = "range[1, 50]")
    private String environmentId;
}