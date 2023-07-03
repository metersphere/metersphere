package io.metersphere.sdk.dto;

import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class AddProjectDTO extends ProjectBaseDTO{

    @Schema(title = "成员数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.ids.not_blank}", groups = {Created.class})
    private List<String> userIds;
}
