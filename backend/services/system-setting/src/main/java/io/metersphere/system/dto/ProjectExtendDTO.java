package io.metersphere.system.dto;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.dto.ModuleSettingDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectExtendDTO extends Project {

    @Schema(description =  "模块设置")
    private ModuleSettingDTO moduleSetting;
}
