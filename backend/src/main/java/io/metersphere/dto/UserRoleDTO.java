package io.metersphere.dto;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRoleDTO {

    private String userId;
    private List<Role> roles;
    private List<UserRole> userRoles;
    private static final long serialVersionUID = 1L;

}
