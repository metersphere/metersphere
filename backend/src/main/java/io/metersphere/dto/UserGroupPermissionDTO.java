package io.metersphere.dto;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.UserGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserGroupPermissionDTO {
    List<GroupResourceDTO> list = new ArrayList<>();
    List<Group> groups = new ArrayList<>();
    List<UserGroup> userGroups = new ArrayList<>();
}
