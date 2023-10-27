package io.metersphere.system.dto.user;

import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRolePermission;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserRoleResourceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserRoleResource resource;
    private List<UserRolePermission> permissions;
    private String type;

    private UserRole userRole;
    private List<UserRolePermission> userRolePermissions;
}
