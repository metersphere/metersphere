package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReference implements Serializable {
    @Schema(description =  "引用关系pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_reference.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_reference.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "场景fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_reference.api_scenario_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_reference.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    private String apiScenarioId;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "引用步骤fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_reference.reference_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_scenario_reference.reference_id.length_range}", groups = {Created.class, Updated.class})
    private String referenceId;

    @Schema(description =  "引用步骤类型/REF/COPY")
    private String referenceType;

    @Schema(description =  "步骤类型/CASE/API")
    private String dataType;

    private static final long serialVersionUID = 1L;
}