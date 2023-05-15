
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "关注记录")
@TableName("api_scenario_follow")
@Data
public class ApiScenarioFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 50, message = "{api_scenario_follow.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_follow.api_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景fk", required = true, allowableValues = "range[1, 50]")
    private String apiScenarioId;

    @Size(min = 1, max = 50, message = "{api_scenario_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_follow.follow_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "关注人/用户fk", required = true, allowableValues = "range[1, 50]")
    private String followId;

}