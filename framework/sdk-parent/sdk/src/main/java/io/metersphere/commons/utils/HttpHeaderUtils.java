package io.metersphere.commons.utils;

import io.metersphere.base.domain.User;
import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.BaseUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

/**
 * 服务之间调用，需要添加HttpHeader,获取的时候注意当前线程的位置
 */
public class HttpHeaderUtils {

    private static final ThreadLocal<User> sessionUserThreadLocal = new ThreadLocal<>();

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        headers.add(HttpHeaders.COOKIE, SessionUtils.getHttpHeader(HttpHeaders.COOKIE));
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, SessionUtils.getHttpHeader(HttpHeaders.ACCEPT_LANGUAGE));

        String headerToken = SessionUtils.getHttpHeader(SessionConstants.HEADER_TOKEN);
        if (StringUtils.isNotBlank(headerToken)) {
            headers.add(SessionConstants.HEADER_TOKEN, headerToken);
        }
        String csrfToken = SessionUtils.getHttpHeader(SessionConstants.CSRF_TOKEN);
        if (StringUtils.isNotBlank(csrfToken)) {
            headers.add(SessionConstants.CSRF_TOKEN, csrfToken);
        }
        String currentProject = SessionUtils.getHttpHeader(SessionConstants.CURRENT_PROJECT);
        if (StringUtils.isNotBlank(currentProject)) {
            headers.add(SessionConstants.CURRENT_PROJECT, currentProject);
        }
        String currentWorkspace = SessionUtils.getHttpHeader(SessionConstants.CURRENT_WORKSPACE);
        if (StringUtils.isNotBlank(currentWorkspace)) {
            headers.add(SessionConstants.CURRENT_WORKSPACE, currentWorkspace);
        }
        String accessKey = SessionUtils.getHttpHeader(SessionConstants.ACCESS_KEY);
        if (StringUtils.isNotBlank(accessKey)) {
            headers.add(SessionConstants.ACCESS_KEY, accessKey);
        }
        String signature = SessionUtils.getHttpHeader(SessionConstants.SIGNATURE);
        if (StringUtils.isNotBlank(signature)) {
            headers.add(SessionConstants.SIGNATURE, signature);
        }


        User user = sessionUserThreadLocal.get();
        if (user != null && SessionUtils.getUser() == null) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            SessionUser sessionUser = SessionUser.fromUser(userDTO, UUID.randomUUID().toString());

            headers.add(SessionConstants.HEADER_TOKEN, sessionUser.getSessionId());
            headers.add(SessionConstants.CSRF_TOKEN, sessionUser.getCsrfToken());
            headers.add(SessionConstants.SSO_TOKEN, sessionUser.getId());
            headers.add(SessionConstants.CURRENT_PROJECT, sessionUser.getLastProjectId());
            headers.add(SessionConstants.CURRENT_WORKSPACE, sessionUser.getLastWorkspaceId());
        }

        return headers;
    }

    public static void runAsUser(User user) {
        if (user != null) {
            if (StringUtils.isBlank(user.getId())) {
                throw new IllegalArgumentException("User ID can't be null or empty.");
            }
            sessionUserThreadLocal.set(user);
        } else {
            sessionUserThreadLocal.remove();
        }
    }

    public static void runAsUser(String userId) {
        BaseUserService baseUserService = CommonBeanFactory.getBean(BaseUserService.class);
        runAsUser(baseUserService.getUserDTO(userId));
    }

    public static void clearUser() {
        sessionUserThreadLocal.remove();
    }
}
