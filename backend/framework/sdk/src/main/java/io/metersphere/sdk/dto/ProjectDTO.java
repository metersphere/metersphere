package io.metersphere.sdk.dto;

import io.metersphere.project.domain.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectDTO extends Project {
    @Schema(title = "项目成员数量", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String number;
    @Schema(title = "所属组织", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String organizationName;
}
