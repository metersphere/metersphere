package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "场景步骤引用CASE关系记录")
@Table("api_scenario_reference")
@Data
public class ApiScenarioReference implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_scenario_reference.id.not_blank}", groups = {Updated.class})
    @Schema(title = "引用关系pk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_scenario_reference.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_reference.api_scenario_id.not_blank}", groups = {Created.class})
    @Schema(title = "场景fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String apiScenarioId;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 50, message = "{api_scenario_reference.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_reference.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_scenario_reference.reference_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_scenario_reference.reference_id.not_blank}", groups = {Created.class})
    @Schema(title = "引用步骤fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String referenceId;

    @Schema(title = "引用步骤类型/REF/COPY", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 20]")
    private String referenceType;

    @Schema(title = "步骤类型/CASE/API", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 20]")
    private String dataType;

}