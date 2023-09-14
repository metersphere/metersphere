package io.metersphere.project.dto;

import io.metersphere.system.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 项目用户组列表DTO
 *
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectUserRoleDTO extends UserRole {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成员数目
     */
    @Schema(description = "成员数目")
    private Integer memberCount;
}
