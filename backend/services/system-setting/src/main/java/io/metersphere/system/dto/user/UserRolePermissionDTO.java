package io.metersphere.system.dto.user;

import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserRolePermissionDTO {
    List<UserRoleResourceDTO> list = new ArrayList<>();
    List<UserRole> userRoles = new ArrayList<>();
    List<UserRoleRelation> userRoleRelations = new ArrayList<>();
}
