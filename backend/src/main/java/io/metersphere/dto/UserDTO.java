package io.metersphere.dto;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.User;
import io.metersphere.base.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO extends User {

    private List<Role> roles = new ArrayList<>();

    private List<UserRole> userRoles = new ArrayList<>();

    private static final long serialVersionUID = 1L;

}
