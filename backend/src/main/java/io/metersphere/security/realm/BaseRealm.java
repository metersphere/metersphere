package io.metersphere.security.realm;

import io.metersphere.base.domain.UserGroupPermission;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.GroupResourceDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        Set<String> permissions = Objects.requireNonNull(SessionUtils.getUser()).getGroupPermissions().stream()
                .map(GroupResourceDTO::getUserGroupPermissions)
                .flatMap(List::stream)
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());

        return permissions.contains(permission);
    }
}
