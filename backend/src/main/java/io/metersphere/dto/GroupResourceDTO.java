package io.metersphere.dto;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.UserGroupPermission;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GroupResourceDTO implements Serializable {
    private GroupResource resource;
    private List<GroupPermission> permissions;
    private String type;

    private Group group;
    private List<UserGroupPermission> userGroupPermissions;
}
