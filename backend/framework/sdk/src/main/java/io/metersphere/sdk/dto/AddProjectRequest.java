package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class AddProjectRequest extends ProjectBaseRequest {

    @Schema(title = "成员数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> userIds;
}
