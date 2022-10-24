package io.metersphere.commons.utils;

import io.metersphere.base.domain.User;
import io.metersphere.base.domain.UserKey;
import io.metersphere.commons.constants.ApiKeyConstants;
import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.UserKeyService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

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
        if (user != null) {
            UserKey userKey = getUserKey(user);
            accessKey = userKey.getAccessKey();
            String secretKey = userKey.getSecretKey();
            headers.add(SessionConstants.ACCESS_KEY, accessKey);
            headers.add(SessionConstants.SIGNATURE, CodingUtil.aesDecrypt(accessKey + "|" + System.currentTimeMillis(), secretKey, accessKey));
            headers.remove(HttpHeaders.COOKIE);
        }

        return headers;
    }

    private static UserKey getUserKey(User user) {
        UserKeyService userKeyService = CommonBeanFactory.getBean(UserKeyService.class);
        List<UserKey> userKeys = userKeyService.getUserKeysInfo(user.getId());
        UserKey userKey;
        if (CollectionUtils.isEmpty(userKeys)) {
            userKey = userKeyService.generateUserKey(user.getId());
        } else {
            Optional<UserKey> ukOp = userKeys.stream().filter(uk -> StringUtils.equals(uk.getStatus(), ApiKeyConstants.ACTIVE.name())).findAny();
            if (ukOp.isEmpty()) {
                MSException.throwException("用户[" + user.getId() + "]至少需要开启一个ApiKey");
            }
            userKey = ukOp.get();
        }
        return userKey;
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
