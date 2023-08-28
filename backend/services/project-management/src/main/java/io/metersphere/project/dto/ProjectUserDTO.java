package io.metersphere.project.dto;

import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserDTO extends User {

    @Schema(description =  "用户组集合")
    private List<UserRole> userRoles;
}
