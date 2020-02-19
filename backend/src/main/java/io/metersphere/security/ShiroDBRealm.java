package io.metersphere.security;


import io.metersphere.base.domain.Role;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.UserService;
import io.metersphere.user.SessionUser;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 自定义Realm 注入service 可能会导致在 service的aop 失效，例如@Transactional,
 * 解决方法：
 * <p>
 * 1. 这里改成注入mapper，这样mapper 中的事务失效<br/>
 * 2. 这里仍然注入service，在配置ShiroConfig 的时候不去set realm, 等到spring 初始化完成之后
 * set realm
 * </p>
 */
public class ShiroDBRealm extends AuthorizingRealm {

    private Logger logger = LoggerFactory.getLogger(ShiroDBRealm.class);
    @Resource
    private UserService userService;

    /**
     * 权限认证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userName = (String) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        // roles 内容填充
        UserDTO userDTO = userService.getUserDTO(userName);
        Set<String> roles = userDTO.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        authorizationInfo.setRoles(roles);

        return authorizationInfo;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String userId = token.getUsername();
        String password = String.valueOf(token.getPassword());
        UserDTO user = userService.getUserDTO(userId);
        String msg;
        if (user == null) {
            msg = "not exist user is trying to login, user:" + userId;
            logger.warn(msg);
            throw new UnknownAccountException(msg);
        }
        // TODO 密码验证

        SessionUser sessionUser = SessionUser.fromUser(user);
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        return true;
    }
}
