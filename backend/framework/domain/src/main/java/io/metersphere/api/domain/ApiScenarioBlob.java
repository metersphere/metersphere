package io.metersphere.api.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioBlob implements Serializable {
    @Schema(description =  "场景pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_scenario_blob.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_scenario_blob.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "场景步骤内容")
    private byte[] content;

    private static final long serialVersionUID = 1L;
}