package io.metersphere.commons.utils;

import io.metersphere.commons.user.SessionUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

public class SessionUtils {

    public static String getUserId() {
        SessionUser user = getUser();
        return user == null ? null : user.getId();
    }

    public static SessionUser getUser() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            SessionUser user = (SessionUser) session.getAttribute(ATTR_USER);
            assert user != null;
            return user;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getSessionId() {
        return (String) SecurityUtils.getSubject().getSession().getId();
    }

    //
    public static void putUser(SessionUser sessionUser) {
        SecurityUtils.getSubject().getSession().setAttribute(ATTR_USER, sessionUser);
    }

    public static String getCurrentWorkspaceId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            LogUtil.info("WORKSPACE: {}", request.getHeader("WORKSPACE"));
            if (request.getHeader("WORKSPACE") != null) {
                return request.getHeader("WORKSPACE");
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return getUser().getLastWorkspaceId();
    }

    public static String getCurrentProjectId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            LogUtil.info("PROJECT: {}", request.getHeader("PROJECT"));
            if (request.getHeader("PROJECT") != null) {
                return request.getHeader("PROJECT");
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return getUser().getLastProjectId();
    }
}
