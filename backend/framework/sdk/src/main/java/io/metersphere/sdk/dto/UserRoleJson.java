package io.metersphere.sdk.dto;


import io.metersphere.system.domain.UserRolePermission;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserRoleJson implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<UserRoleResource> resource;
    private List<UserRolePermission> permissions;
}
