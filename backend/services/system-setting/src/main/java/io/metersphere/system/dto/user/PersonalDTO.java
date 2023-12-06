package io.metersphere.system.dto.user;

import io.metersphere.project.domain.Project;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonalDTO extends User {

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "用户所属组织和项目")
    private Map<Organization, List<Project>> organizationProjectMap = new LinkedHashMap<>();
}
