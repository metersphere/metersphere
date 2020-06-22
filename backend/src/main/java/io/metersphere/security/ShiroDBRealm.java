package io.metersphere.security;


import io.metersphere.base.domain.Role;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

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

    @Value("${run.mode:release}")
    private String runMode;

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
            user = userService.getUserDTOByEmail(userId);
            if (user == null) {
                msg = "The user does not exist: " + userId;
                logger.warn(msg);
                throw new UnknownAccountException(Translator.get("user_not_exist") + userId);
            }
            userId = user.getId();
        }

        // local test
        if (StringUtils.equals("local", runMode)) {
            SessionUser sessionUser = SessionUser.fromUser(user);
            SessionUtils.putUser(sessionUser);
            return new SimpleAuthenticationInfo(userId, password, getName());
        }
        // apikey 校验不验证密码
        if (ApiKeySessionHandler.random.equalsIgnoreCase(password)) {
            SessionUser sessionUser = SessionUser.fromUser(user);
            SessionUtils.putUser(sessionUser);
            return new SimpleAuthenticationInfo(userId, password, getName());
        }

        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");
        if (StringUtils.equals(login, "ldap")) {
            SessionUser sessionUser = SessionUser.fromUser(user);
            SessionUtils.putUser(sessionUser);
            return new SimpleAuthenticationInfo(userId, password, getName());
        }

        // 密码验证
        if (!userService.checkUserPassword(userId, password)) {
            throw new IncorrectCredentialsException(Translator.get("password_is_incorrect"));
        }
        //
        SessionUser sessionUser = SessionUser.fromUser(user);
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        return true;
    }
}
