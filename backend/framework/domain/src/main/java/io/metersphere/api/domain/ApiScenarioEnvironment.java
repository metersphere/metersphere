
package io.metersphere.api.domain;

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

@ApiModel(value = "场景环境")
@Table("api_scenario_environment")
@Data
public class ApiScenarioEnvironment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_environment.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "场景环境pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_scenario_environment.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_environment.api_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景fk", required = true, allowableValues = "range[1, 50]")
    private String apiScenarioId;

    @Size(min = 1, max = 50, message = "{api_scenario_environment.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_environment.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "环境fk", required = false, allowableValues = "range[1, 50]")
    private String environmentId;

    @ApiModelProperty(name = "环境组fk", required = false, allowableValues = "range[1, 50]")
    private String environmentGroupId;

}