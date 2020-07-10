package io.metersphere.dto;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO {
    private String id;

    private String name;

    private String email;

    private String phone;

    private String status;

    private String source;

    private Long createTime;

    private Long updateTime;

    private String language;

    private String lastWorkspaceId;

    private String lastOrganizationId;

    private List<Role> roles = new ArrayList<>();

    private List<UserRole> userRoles = new ArrayList<>();

    private static final long serialVersionUID = 1L;

}
