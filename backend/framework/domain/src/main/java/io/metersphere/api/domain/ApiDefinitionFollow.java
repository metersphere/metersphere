package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "接口定义关注人")
@Table("api_definition_follow")
@Data
public class ApiDefinitionFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 50, message = "{api_definition_follow.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_follow.api_definition_id.not_blank}", groups = {Created.class})
    @Schema(title = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String apiDefinitionId;

    @Size(min = 1, max = 50, message = "{api_definition_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_follow.follow_id.not_blank}", groups = {Created.class})
    @Schema(title = "关注人/用户fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String followId;

}