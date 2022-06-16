package io.metersphere.commons.utils;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.UserGroupPermission;
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
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.commons.constants.SessionConstants.ATTR_USER;

public class SessionUtils {

    private static final ThreadLocal<String> projectId = new ThreadLocal<>();
    private static final ThreadLocal<String> workspaceId = new ThreadLocal<>();

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

    /**
     * 权限验证时从 controller 参数列表中找到 workspaceId 传入
     */
    public static void setCurrentWorkspaceId(String workspaceId) {
        SessionUtils.workspaceId.set(workspaceId);
    }

    /**
     * 权限验证时从 controller 参数列表中找到 projectId 传入
     */
    public static void setCurrentProjectId(String projectId) {
        SessionUtils.projectId.set(projectId);
    }

    public static String getCurrentWorkspaceId() {
        if (StringUtils.isNotEmpty(workspaceId.get())) {
            return workspaceId.get();
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            LogUtil.debug("WORKSPACE: {}", request.getHeader("WORKSPACE"));
            if (request.getHeader("WORKSPACE") != null) {
                return request.getHeader("WORKSPACE");
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return getUser().getLastWorkspaceId();
    }

    public static String getCurrentProjectId() {
        if (StringUtils.isNotEmpty(projectId.get())) {
            return projectId.get();
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            LogUtil.debug("PROJECT: {}", request.getHeader("PROJECT"));
            if (request.getHeader("PROJECT") != null) {
                return request.getHeader("PROJECT");
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return getUser().getLastProjectId();
    }

    public static boolean hasPermission(String workspaceId, String projectId, String permission) {
        Map<String, List<UserGroupPermission>> userGroupPermissions = new HashMap<>();
        Map<String, Group> group = new HashMap<>();
        SessionUser user = Objects.requireNonNull(SessionUtils.getUser());
        user.getUserGroups().forEach(ug -> user.getGroupPermissions().forEach(gp -> {
            if (org.apache.commons.lang3.StringUtils.equals(gp.getGroup().getId(), ug.getGroupId())) {
                userGroupPermissions.put(ug.getId(), gp.getUserGroupPermissions());
                group.put(ug.getId(), gp.getGroup());
            }
        }));


        Set<String> currentProjectPermissions = getCurrentProjectPermissions(userGroupPermissions, projectId, group, user);
        if (currentProjectPermissions.contains(permission)) {
            return true;
        }

        Set<String> currentWorkspacePermissions = getCurrentWorkspacePermissions(userGroupPermissions, workspaceId, group, user);
        if (currentWorkspacePermissions.contains(permission)) {
            return true;
        }

        Set<String> systemPermissions = getSystemPermissions(userGroupPermissions, group, user);
        return systemPermissions.contains(permission);
    }

    private static Set<String> getSystemPermissions(Map<String, List<UserGroupPermission>> userGroupPermissions, Map<String, Group> group, SessionUser user) {
        return user.getUserGroups().stream()
                .filter(ug -> group.get(ug.getId()) != null && StringUtils.equals(group.get(ug.getId()).getType(), "SYSTEM"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), "system") || StringUtils.equals(ug.getSourceId(), "'adminSourceId'"))
                .flatMap(ug -> userGroupPermissions.get(ug.getId()).stream())
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    private static Set<String> getCurrentWorkspacePermissions(Map<String, List<UserGroupPermission>> userGroupPermissions, String workspaceId, Map<String, Group> group, SessionUser user) {
        return user.getUserGroups().stream()
                .filter(ug -> group.get(ug.getId()) != null && StringUtils.equals(group.get(ug.getId()).getType(), "WORKSPACE"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), workspaceId))
                .flatMap(ug -> userGroupPermissions.get(ug.getId()).stream())
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    private static Set<String> getCurrentProjectPermissions(Map<String, List<UserGroupPermission>> userGroupPermissions, String projectId, Map<String, Group> group, SessionUser user) {
        return user.getUserGroups().stream()
                .filter(ug -> group.get(ug.getId()) != null && StringUtils.equals(group.get(ug.getId()).getType(), "PROJECT"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), projectId))
                .flatMap(ug -> userGroupPermissions.get(ug.getId()).stream())
                .map(UserGroupPermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    public static void clearCurrentWorkspaceId() {
        workspaceId.remove();
    }

    public static void clearCurrentProjectId() {
        projectId.remove();
    }
}
