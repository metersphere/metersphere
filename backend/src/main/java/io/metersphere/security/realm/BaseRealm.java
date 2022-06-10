package io.metersphere.security.realm;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.UserGroupPermission;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseRealm extends AuthorizingRealm {
    @Resource
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;

        String userId = token.getUsername();
        String password = String.valueOf(token.getPassword());
        UserDTO user = userService.getUserDTO(userId);
        if (user == null) {
            throw new UnknownAccountException(Translator.get("user_not_exist"));
        }
        SessionUser sessionUser = SessionUser.fromUser(user);
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        Map<String, List<UserGroupPermission>> userGroupPermissions = new HashMap<>();
        Map<String, Group> group = new HashMap<>();
        SessionUser user = Objects.requireNonNull(SessionUtils.getUser());
        user.getUserGroups().forEach(ug -> user.getGroupPermissions().forEach(gp -> {
            if (StringUtils.equals(gp.getGroup().getId(), ug.getGroupId())) {
                userGroupPermissions.put(ug.getId(), gp.getUserGroupPermissions());
                group.put(ug.getId(), gp.getGroup());
            }
        }));


        Set<String> currentProjectPermissions = getCurrentProjectPermissions(userGroupPermissions, group, user);
        if (currentProjectPermissions.contains(permission)) {
            return true;
        }

        Set<String> currentWorkspacePermissions = getCurrentWorkspacePermissions(userGroupPermissions, group, user);
        if (currentWorkspacePermissions.contains(permission)) {
            return true;
        }

        Set<String> systemPermissions = getSystemPermissions(userGroupPermissions, group, user);
        return systemPermissions.contains(permission);
    }

    private Set<String> getSystemPermissions(Map<String, List<UserGroupPermission>> userGroupPermissions, Map<String, Group> group, SessionUser user) {
        return user.getUserGroups().stream()
                .filter(ug -> group.get(ug.getId()) != null && StringUtils.equals(group.get(ug.getId()).getType(), "SYSTEM"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), "system") || StringUtils.equals(ug.getSourceId(), "'adminSourceId'"))
                .flatMap(ug -> userGroupPermissions.get(ug.getId()).stream())
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    private Set<String> getCurrentWorkspacePermissions(Map<String, List<UserGroupPermission>> userGroupPermissions, Map<String, Group> group, SessionUser user) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        return user.getUserGroups().stream()
                .filter(ug -> group.get(ug.getId()) != null && StringUtils.equals(group.get(ug.getId()).getType(), "WORKSPACE"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), currentWorkspaceId))
                .flatMap(ug -> userGroupPermissions.get(ug.getId()).stream())
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    private Set<String> getCurrentProjectPermissions(Map<String, List<UserGroupPermission>> userGroupPermissions, Map<String, Group> group, SessionUser user) {
        String currentProjectId = SessionUtils.getCurrentProjectId();
        return user.getUserGroups().stream()
                .filter(ug -> group.get(ug.getId()) != null && StringUtils.equals(group.get(ug.getId()).getType(), "PROJECT"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), currentProjectId))
                .flatMap(ug -> userGroupPermissions.get(ug.getId()).stream())
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());
    }
}
