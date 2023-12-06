package io.metersphere.system.dto.user;

import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRoleRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDTO extends User {

    private List<UserRole> userRoles = new ArrayList<>();
    private List<UserRoleRelation> userRoleRelations = new ArrayList<>();
    private List<UserRoleResourceDTO> userRolePermissions = new ArrayList<>();

    @Schema(description =  "其他平台对接信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] platformInfo;

    @Schema(description = "头像")
    private String avatar;
}
