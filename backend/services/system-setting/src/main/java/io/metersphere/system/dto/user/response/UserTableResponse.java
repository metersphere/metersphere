package io.metersphere.system.dto.user.response;

import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserTableResponse extends User {
    @Schema(description =  "用户所属组织")
    private List<Organization> organizationList = new ArrayList<>();
    @Schema(description =  "用户所属用户组")
    private List<UserRole> userRoleList = new ArrayList<>();

    public void setOrganization(Organization organization) {
        if (!organizationList.contains(organization)) {
            organizationList.add(organization);
        }
    }

    public void setUserRole(UserRole userRole) {
        if (!userRoleList.contains(userRole)) {
            userRoleList.add(userRole);
        }
    }
}
