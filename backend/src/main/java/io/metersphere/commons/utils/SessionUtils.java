package io.metersphere.commons.utils;

import io.metersphere.commons.user.SessionUser;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.core.env.Environment;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

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

    private static Session getSessionByUsername(String username) {
        DefaultSessionManager sessionManager = CommonBeanFactory.getBean(DefaultSessionManager.class);
        Collection<Session> sessions = sessionManager.getSessionDAO().getActiveSessions();
        for (Session session : sessions) {
            if (null != session && org.apache.commons.lang3.StringUtils.equals(String.valueOf(session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)), username)) {
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
        // local session
        String storeType = CommonBeanFactory.getBean(Environment.class).getProperty("spring.session.store-type");
        if (StringUtils.equalsIgnoreCase(storeType, "none")) {
            Session session = getSessionByUsername(username);
            if (session != null) {
                DefaultSessionManager sessionManager = CommonBeanFactory.getBean(DefaultSessionManager.class);
                sessionManager.getSessionDAO().delete(session);
            }
            return;
        }
        // redis session
        RedisIndexedSessionRepository sessionRepository = CommonBeanFactory.getBean(RedisIndexedSessionRepository.class);
        if (sessionRepository == null) {
            return;
        }
        Map<String, ?> users = sessionRepository.findByPrincipalName(username);
        if (MapUtils.isNotEmpty(users)) {
            users.keySet().forEach(sessionRepository::deleteById);
        }
    }

    //
    public static void putUser(SessionUser sessionUser) {
        SecurityUtils.getSubject().getSession().setAttribute(ATTR_USER, sessionUser);
        SecurityUtils.getSubject().getSession().setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, sessionUser.getId());
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
