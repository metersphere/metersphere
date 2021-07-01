package io.metersphere.dto;

import io.metersphere.base.domain.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO extends User {

    private List<Role> roles = new ArrayList<>();

    private List<UserRole> userRoles = new ArrayList<>();

    private List<UserGroup> userGroups = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<GroupResourceDTO> groupPermissions = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    public static class PlatformInfo {
        private String  jiraAccount;
        private String  jiraPassword;
        private String  tapdUserName;
        private String  zentaoUserName;
        private String  zentaoPassword;
    }

}
