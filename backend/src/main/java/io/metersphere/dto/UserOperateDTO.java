package io.metersphere.dto;

import io.metersphere.base.domain.Role;
import io.metersphere.base.domain.User;

import java.util.List;

public class UserOperateDTO extends User {

    private List<Role> roleList;

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }
}
