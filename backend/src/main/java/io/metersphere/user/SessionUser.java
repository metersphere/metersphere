package io.metersphere.user;

import io.metersphere.dto.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

import static io.metersphere.commons.constants.RoleConstants.*;

public class SessionUser extends UserDTO implements Serializable {

    private static final long serialVersionUID = -7149638440406959033L;

    private String workspaceId;
    private String organizationId;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public static SessionUser fromUser(UserDTO user) {
        SessionUser sessionUser = new SessionUser();
        BeanUtils.copyProperties(user, sessionUser);
        String lastSourceId = sessionUser.getLastSourceId();
        user.getUserRoles().forEach(ur -> {
            if (StringUtils.equals(ur.getSourceId(), lastSourceId)) {
                if (StringUtils.equals(ur.getRoleId(), ORG_ADMIN)) {
                    sessionUser.organizationId = lastSourceId;
                    return;
                }
                if (StringUtils.equalsAny(ur.getRoleId(), TEST_MANAGER, TEST_USER, TEST_VIEWER)) {
                    sessionUser.workspaceId = lastSourceId;
                }
            }
        });

        return sessionUser;
    }

}
