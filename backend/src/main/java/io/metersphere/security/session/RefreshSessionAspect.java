package io.metersphere.security.session;

import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
public class RefreshSessionAspect {

    @Resource
    private UserService userService;

    @Pointcut("@annotation(io.metersphere.security.session.RefreshSession)")
    public void pointcut() {
    }


    @AfterReturning(value = "pointcut()", returning = "retValue")
    public void sendNotice(JoinPoint joinPoint, Object retValue) {
        try {
            UserDTO userDTO = userService.getUserDTO(SessionUtils.getUserId());
            SessionUtils.putUser(SessionUser.fromUser(userDTO));
        } catch (Exception ignore) {
            LogUtil.warn(ignore);
        }
    }
}
