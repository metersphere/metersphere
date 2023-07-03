package io.metersphere.sdk.dto;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateProjectRequest extends ProjectBaseRequest {

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{project.id.length_range}", groups = {Updated.class})
    private String id;
    @Schema(title = "成员数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.ids.not_blank}", groups = {Updated.class})
    private List<String> userIds;
}
