
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

@ApiModel(value = "场景步骤引用CASE关系记录")
@Table("api_scenario_reference")
@Data
public class ApiScenarioReference implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_reference.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "引用关系pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_scenario_reference.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_reference.api_scenario_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "场景fk", required = true, allowableValues = "range[1, 50]")
    private String apiScenarioId;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_scenario_reference.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_reference.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_scenario_reference.reference_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_reference.reference_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "引用步骤fk", required = true, allowableValues = "range[1, 50]")
    private String referenceId;

    @ApiModelProperty(name = "引用步骤类型/REF/COPY", required = false, allowableValues = "range[1, 20]")
    private String referenceType;

    @ApiModelProperty(name = "步骤类型/CASE/API", required = false, allowableValues = "range[1, 20]")
    private String dataType;

}