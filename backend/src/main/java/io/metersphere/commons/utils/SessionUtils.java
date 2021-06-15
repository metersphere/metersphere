package io.metersphere.commons.utils;

import io.metersphere.commons.user.SessionUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;

import java.util.Collection;

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
            return null;
        }
    }

    public static String getSessionId() {
        return (String) SecurityUtils.getSubject().getSession().getId();
    }

    private static Session getSessionByUsername(String username) {
        DefaultSessionManager sessionManager = CommonBeanFactory.getBean(DefaultSessionManager.class);
        Collection<Session> sessions = sessionManager.getSessionDAO().getActiveSessions();
        for (Session session : sessions) {
            if (null != session && StringUtils.equals(String.valueOf(session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)), username)) {
                return session;
            }
        }
        return null;
    }

    /**
     * 踢除用户
     *
     * @param username
     */
    public static void kickOutUser(String username) {
        Session session = getSessionByUsername(username);
        if (session != null) {
            DefaultSessionManager sessionManager = CommonBeanFactory.getBean(DefaultSessionManager.class);
            sessionManager.getSessionDAO().delete(session);
        }
    }

    //
    public static void putUser(SessionUser sessionUser) {
        SecurityUtils.getSubject().getSession().setAttribute(ATTR_USER, sessionUser);
    }

    public static String getCurrentWorkspaceId() {
        return getUser().getLastWorkspaceId();
    }

    public static String getCurrentOrganizationId() {
        return getUser().getLastOrganizationId();
    }

    public static String getCurrentProjectId() {
        return getUser().getLastProjectId();
    }
}
